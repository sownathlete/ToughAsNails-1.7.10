package toughasnails.api.stat;

import net.minecraft.entity.player.EntityPlayer;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.thirst.ThirstHandler;

/**
 * 1.7.10 compatibility shim to resolve per-player TAN stat handlers by identifier.
 */
public final class PlayerStatRegistryCompat {

    private PlayerStatRegistryCompat() {}

    public static StatHandlerBase getStat(EntityPlayer player, String identifier) {
        if (player == null || identifier == null) return null;

        // Normalize
        String id = identifier.toLowerCase();

        if ("temperature".equals(id)) {
            return TemperatureHandler.get(player);
        }
        if ("thirst".equals(id)) {
            // IMPORTANT: return the SAME handler class the HUD uses
            return ThirstHandler.get(player);
        }
        return null;
    }
}
