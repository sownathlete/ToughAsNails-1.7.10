package toughasnails.command;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.handler.season.SeasonHandler;
import toughasnails.season.SeasonSavedData;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;

/**
 * Forge 1.7.10 backport of the Tough As Nails command.
 * Handles /toughasnails, /tan settemp, /tan tempinfo, /tan setseason
 */
public class TANCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "toughasnails";
    }

    @Override
    public List getCommandAliases() {
        return Arrays.asList("tan");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/toughasnails <settemp|tempinfo|setseason>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        if (args[0].equalsIgnoreCase("tempinfo")) {
            displayTemperatureInfo(sender);
        } else if (args[0].equalsIgnoreCase("settemp")) {
            setTemperature(sender, args);
        } else if (args[0].equalsIgnoreCase("setseason")) {
            setSeason(sender, args);
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private void displayTemperatureInfo(ICommandSender sender) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        TemperatureHandler temperatureStats = TemperatureHandler.get(player);
        TemperatureDebugger debugger = temperatureStats.debugger;

        if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
            debugger.setGuiVisible(!debugger.isGuiVisible(), player);
        } else {
            sender.addChatMessage(new ChatComponentTranslation("commands.toughasnails.settemp.disabled"));
        }
    }

    private void setTemperature(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException("/toughasnails settemp <value>");
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        TemperatureHandler temperatureStats = TemperatureHandler.get(player);
        int newTemp = parseIntBounded(sender, args[1], 0, TemperatureScale.getScaleTotal());
        Temperature playerTemp = temperatureStats.getTemperature();

        if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
            player.removePotionEffect(TANPotions.hypothermia.id);
            player.removePotionEffect(TANPotions.hyperthermia.id);
            temperatureStats.setChangeTime(0);
            temperatureStats.setTemperature(new Temperature(newTemp));
            sender.addChatMessage(new ChatComponentTranslation("commands.toughasnails.settemp.success", newTemp));
        } else {
            sender.addChatMessage(new ChatComponentTranslation("commands.toughasnails.settemp.disabled"));
        }
    }

    private void setSeason(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException("/toughasnails setseason <subseason>");
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        String seasonArg = args[1].toLowerCase();
        Season.SubSeason newSeason = null;

        for (Season.SubSeason season : Season.SubSeason.values()) {
            if (season.toString().toLowerCase().equals(seasonArg)) {
                newSeason = season;
                break;
            }
        }

        if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            if (newSeason != null) {
                World world = player.worldObj;
                SeasonSavedData seasonData = SeasonHandler.getSeasonSavedData(world);
                seasonData.seasonCycleTicks = 120000 * newSeason.ordinal();
                seasonData.markDirty();
                SeasonHandler.sendSeasonUpdate(world);
                sender.addChatMessage(new ChatComponentTranslation("commands.toughasnails.setseason.success", args[1]));
            } else {
                sender.addChatMessage(new ChatComponentTranslation("commands.toughasnails.setseason.fail", args[1]));
            }
        } else {
            sender.addChatMessage(new ChatComponentTranslation("commands.toughasnails.setseason.disabled"));
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "settemp", "tempinfo", "setseason");
        }
        return null;
    }
}
