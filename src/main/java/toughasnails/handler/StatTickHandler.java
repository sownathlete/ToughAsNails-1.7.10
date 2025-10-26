package toughasnails.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.core.ToughAsNails;
import toughasnails.network.message.MessageUpdateStat;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.thirst.ThirstHandler;

public final class StatTickHandler {

    private static int logThrottle = 0;

    private static final String TEMP_KEY   = "TAN_Temperature";
    private static final String THIRST_KEY = "TAN_Thirst";

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        final EntityPlayer player = e.player;
        final World world = player.worldObj;
        if (world == null || world.isRemote) return; // server only

        // Debug throttle (optional)
        if (e.phase == TickEvent.Phase.END && (++logThrottle % 40) == 0) {
            ToughAsNails.logger.debug("[TAN Tick] Player tick " + e.phase + " for " + player.getCommandSenderName());
        }

        /* ---------------- Temperature (unchanged path) ---------------- */
        if (e.phase == TickEvent.Phase.END && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
            TemperatureHandler temp = TemperatureHelper.getOrCreate(player);
            temp.update(player, world, e.phase);
            TemperatureHelper.save(player, temp);

            // If you ever need client mirrors for temperature, gate a similar block here.
            // (Left off to avoid extra packets.)
        }

        /* ---------------- Thirst (both phases; sync on change) -------- */
        if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) {
            ThirstHandler thirst = ThirstHandler.getOrCreate(player);
            thirst.update(player, world, e.phase);

            if (e.phase == TickEvent.Phase.END) {
                // Persist to server-side EntityData
                ThirstHandler.save(player, thirst);

                // Only send to the client when it actually changed
                if (thirst.hasChanged()) {
                    NBTTagCompound tag = new NBTTagCompound();
                    thirst.writeToNBT(tag);
                    player.getEntityData().setTag(THIRST_KEY, tag);

                    PacketHandler.instance.sendTo(new MessageUpdateStat("thirst", tag),
                                                  (EntityPlayerMP) player);

                    thirst.onSendClientUpdate(); // clears the "changed" flag
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone e) {
        NBTTagCompound oldTag = e.original.getEntityData();
        NBTTagCompound newTag = e.entityPlayer.getEntityData();

        if (oldTag.hasKey(TEMP_KEY)) {
            newTag.setTag(TEMP_KEY, oldTag.getCompoundTag(TEMP_KEY));
        }
        if (oldTag.hasKey(THIRST_KEY)) {
            newTag.setTag(THIRST_KEY, oldTag.getCompoundTag(THIRST_KEY));
        }
        ToughAsNails.logger.info("[TAN Tick] Copied stat NBT to respawned player.");
    }
}
