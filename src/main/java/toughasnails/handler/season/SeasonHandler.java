package toughasnails.handler.season;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.MapStorage;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.ISeasonData;
import toughasnails.api.season.Season;
import toughasnails.handler.PacketHandler;
import toughasnails.network.message.MessageSyncSeasonCycle;
import toughasnails.season.SeasonSavedData;
import toughasnails.season.SeasonTime;

public class SeasonHandler {
    private Season.SubSeason lastSeason = null;
    public static int clientSeasonCycleTicks = 0;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase == TickEvent.Phase.END && !world.isRemote &&
            world.provider.dimensionId == 0 &&
            SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {

            SeasonSavedData savedData = getSeasonSavedData(world);
            if (savedData.seasonCycleTicks++ > SeasonTime.TOTAL_CYCLE_TICKS) {
                savedData.seasonCycleTicks = 0;
            }

            if (savedData.seasonCycleTicks % 20 == 0) {
                sendSeasonUpdate(world);
            }

            savedData.markDirty();
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;
        sendSeasonUpdate(world);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        int dimension = mc.thePlayer.dimension;
        if (event.phase == TickEvent.Phase.END && dimension == 0 &&
            SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {

            if (clientSeasonCycleTicks++ > SeasonTime.TOTAL_CYCLE_TICKS) {
                clientSeasonCycleTicks = 0;
            }

            SeasonTime calendar = new SeasonTime(clientSeasonCycleTicks);
            if (calendar.getSubSeason() != this.lastSeason) {
                mc.renderGlobal.loadRenderers();
                this.lastSeason = calendar.getSubSeason();
            }
        }
    }

    public static void sendSeasonUpdate(World world) {
        if (!world.isRemote && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            SeasonSavedData savedData = getSeasonSavedData(world);
            PacketHandler.instance.sendToAll((IMessage)new MessageSyncSeasonCycle(savedData.seasonCycleTicks));
        }
    }

    public static SeasonSavedData getSeasonSavedData(World world) {
        // 1.7.10: access field directly instead of method
        MapStorage storage = world.perWorldStorage;
        SeasonSavedData savedData = (SeasonSavedData) storage.loadData(SeasonSavedData.class, "seasons");
        if (savedData == null) {
            savedData = new SeasonSavedData("seasons");
            storage.setData("seasons", savedData);
            savedData.markDirty();
        }
        return savedData;
    }

    public static ISeasonData getServerSeasonData(World world) {
        SeasonSavedData savedData = getSeasonSavedData(world);
        return new SeasonTime(savedData.seasonCycleTicks);
    }

    public static ISeasonData getClientSeasonData() {
        return new SeasonTime(clientSeasonCycleTicks);
    }
}
