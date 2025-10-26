package toughasnails.api.stat;

import net.minecraft.entity.player.EntityPlayer;
import toughasnails.api.stat.StatHandlerBase;
import toughasnails.temperature.TemperatureHandler;

/**
 * 1.7.10 compatibility shim to resolve per-player TAN stat handlers by identifier.
 * Add additional mappings here (e.g., "thirst") as you port them.
 */
public final class PlayerStatRegistryCompat {

    private PlayerStatRegistryCompat() {}

    /**
     * Resolve the concrete StatHandler for a player by its string identifier.
     * @param player the player
     * @param identifier e.g., "temperature"
     * @return the StatHandlerBase instance, or null if not found
     */
    public static StatHandlerBase getStat(EntityPlayer player, String identifier) {
        if (player == null || identifier == null) return null;

        // Map known stats by their IDs. Extend as you add more.
        if ("temperature".equalsIgnoreCase(identifier)) {
            return TemperatureHandler.get(player); // ensure your TemperatureHandler defines this
        }

        // Example stub for future port:
        // if ("thirst".equalsIgnoreCase(identifier)) {
        //     return ThirstHandler.get(player);
        // }

        return null;
    }
}
