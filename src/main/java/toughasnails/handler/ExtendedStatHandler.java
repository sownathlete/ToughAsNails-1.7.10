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

/**
 * 1.7.10-compatible stat attach/sync loop using the per-player registry.
 */
public class ExtendedStatHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;

        if (!world.isRemote) {
            // Ensure all stat handlers exist for this player
            Map<String, StatHandlerBase> stats = PlayerStatRegistry.getStatHandlers(player);

            for (Map.Entry<String, StatHandlerBase> entry : stats.entrySet()) {
                String id = entry.getKey();
                StatHandlerBase stat = entry.getValue();

                // Load from the player's persisted NBT (Entity.getEntityData)
                NBTTagCompound tag = player.getEntityData().getCompoundTag(id);
                stat.readFromNBT(tag);

                // Mark as "synced" server-side
                stat.onSendClientUpdate();

                // Push current state to client
                PacketHandler.instance.sendTo(new MessageUpdateStat(id, tag), (EntityPlayerMP) player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;

        if (!world.isRemote) {
            Map<String, StatHandlerBase> stats = PlayerStatRegistry.getStatHandlers(player);

            for (Map.Entry<String, StatHandlerBase> entry : stats.entrySet()) {
                String id = entry.getKey();
                IPlayerStat statIface = entry.getValue();      // for update/hasChanged()
                StatHandlerBase statBase = entry.getValue();   // for read/write NBT

                statIface.update(player, world, event.phase);

                if (event.phase == TickEvent.Phase.START && statIface.hasChanged()) {
                    // Save to player's persistent NBT
                    NBTTagCompound tag = new NBTTagCompound();
                    statBase.writeToNBT(tag);
                    player.getEntityData().setTag(id, tag);

                    // mark as sent
                    statBase.onSendClientUpdate();

                    // Sync to client
                    PacketHandler.instance.sendTo(new MessageUpdateStat(id, tag), (EntityPlayerMP) player);
                }
            }
        }
    }
}
