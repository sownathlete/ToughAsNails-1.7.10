package toughasnails.config;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import toughasnails.api.config.SyncedConfig;
import toughasnails.core.ToughAsNails;
import toughasnails.handler.PacketHandler;
import toughasnails.network.message.MessageSyncConfigs;

/**
 * Syncs TAN config values to clients (Forge 1.7.10).
 */
public class SyncedConfigHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        World world = player.worldObj;

        // Server side only
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            NBTTagCompound nbtOptions = new NBTTagCompound();

            for (Map.Entry<String, SyncedConfig.SyncedConfigEntry> e : SyncedConfig.optionsToSync.entrySet()) {
                nbtOptions.setString(e.getKey(), e.getValue().value);
            }

            // PacketHandler wraps the SimpleNetworkWrapper; no need to import it here.
            PacketHandler.instance.sendTo(new MessageSyncConfigs(nbtOptions), (EntityPlayerMP) player);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Only act on client-side world unloads
        if (event.world.isRemote) {
            Minecraft mc = Minecraft.getMinecraft();
            // If the net handler or its network manager is gone, we’re disconnecting to menu.
            if (mc.theWorld == null || mc.getNetHandler() == null || mc.getNetHandler().getNetworkManager() == null) {
                SyncedConfig.restoreDefaults();
                ToughAsNails.logger.info("TAN configuration restored to local values");
            }
        }
    }
}
