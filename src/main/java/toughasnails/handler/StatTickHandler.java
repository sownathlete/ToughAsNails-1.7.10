package toughasnails.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.thirst.ThirstHandler;

public final class StatTickHandler {

    /** NBT subtags copied on death/respawn */
    private static final String TEMP_KEY   = "TAN_Temperature";
    private static final String THIRST_KEY = "TAN_Thirst";

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        final EntityPlayer player = e.player;
        final World world = player.worldObj;
        if (world == null || world.isRemote) return; // server only

        // --- TEMPERATURE ---
        if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
            TemperatureHandler temp = TemperatureHelper.getOrCreate(player);
            temp.update(player, world, e.phase);
            TemperatureHelper.save(player, temp);
        }

        // --- THIRST ---
        if (SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) {
            ThirstHandler thirst = ThirstHandler.getOrCreate(player);
            thirst.update(player, world, e.phase);
            ThirstHandler.save(player, thirst);
        }
    }

    /** Copy our sub-NBT from the old player to the new one on respawn. */
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
    }
}
