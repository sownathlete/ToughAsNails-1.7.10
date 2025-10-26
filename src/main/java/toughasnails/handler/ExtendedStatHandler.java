package toughasnails.handler;

import java.util.Map;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import toughasnails.api.stat.IPlayerStat;
import toughasnails.api.stat.PlayerStatRegistry;
import toughasnails.api.stat.StatHandlerBase;
import toughasnails.network.message.MessageUpdateStat;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.thirst.ThirstHandler;

/**
 * 1.7.10-compatible stat attach/sync loop using the per-player registry,
 * with explicit syncs for THIRST (and temperature at login for completeness).
 */
public class ExtendedStatHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;
        if (world.isRemote) return;

        // Keep existing registry-driven syncs (temperature, etc. if registered)
        Map<String, StatHandlerBase> stats = PlayerStatRegistry.getStatHandlers(player);
        for (Map.Entry<String, StatHandlerBase> entry : stats.entrySet()) {
            String id = entry.getKey();
            StatHandlerBase stat = entry.getValue();

            NBTTagCompound tag = player.getEntityData().getCompoundTag(id);
            stat.readFromNBT(tag);
            stat.onSendClientUpdate(); // mark as sent
            PacketHandler.instance.sendTo(new MessageUpdateStat(id, tag), (EntityPlayerMP) player);
        }

        // --- Explicit THIRST sync (always present in this backport) ---
        {
            ThirstHandler thirst = ThirstHandler.getOrCreate(player);
            NBTTagCompound tag = new NBTTagCompound();
            thirst.writeToNBT(tag);
            // mark client copy as up-to-date
            thirst.onSendClientUpdate();
            PacketHandler.instance.sendTo(new MessageUpdateStat("thirst", tag), (EntityPlayerMP) player);
        }

        // (Optional) also push temperature once on login to seed the client
        {
            TemperatureHandler temp = TemperatureHandler.get(player);
            if (temp != null) {
                NBTTagCompound tag = new NBTTagCompound();
                temp.writeToNBT(tag);
                temp.onSendClientUpdate();
                PacketHandler.instance.sendTo(new MessageUpdateStat("temperature", tag), (EntityPlayerMP) player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;
        if (world.isRemote) return;

        // Keep existing registry loop (no change)
        Map<String, StatHandlerBase> stats = PlayerStatRegistry.getStatHandlers(player);
        for (Map.Entry<String, StatHandlerBase> entry : stats.entrySet()) {
            String id = entry.getKey();
            IPlayerStat statIface = entry.getValue();
            StatHandlerBase statBase = entry.getValue();

            statIface.update(player, world, event.phase);

            if (event.phase == TickEvent.Phase.START && statIface.hasChanged()) {
                NBTTagCompound tag = new NBTTagCompound();
                statBase.writeToNBT(tag);
                player.getEntityData().setTag(id, tag);
                statBase.onSendClientUpdate();
                PacketHandler.instance.sendTo(new MessageUpdateStat(id, tag), (EntityPlayerMP) player);
            }
        }

        // --- Explicit thirst save+sync on END when it actually changes ---
        if (event.phase == TickEvent.Phase.END) {
            ThirstHandler thirst = ThirstHandler.getOrCreate(player);
            // Persist to the player's entity NBT
            ThirstHandler.save(player, thirst);

            // If the visible value (level) changed, push to client now
            if (thirst.hasChanged()) {
                NBTTagCompound tag = new NBTTagCompound();
                thirst.writeToNBT(tag);
                thirst.onSendClientUpdate();
                PacketHandler.instance.sendTo(new MessageUpdateStat("thirst", tag), (EntityPlayerMP) player);
            }
        }
    }
}
