package toughasnails.api.thirst;

import net.minecraft.entity.player.EntityPlayer;
import toughasnails.api.TANCapabilities;
import toughasnails.api.stat.capability.CapabilityProvider;
import toughasnails.api.stat.capability.CapabilityProvider.Capability;
import toughasnails.api.stat.capability.IThirst;

/**
 * Backported ThirstHelper for Forge 1.7.10.
 *
 * Maintains 1:1 compatibility with the original Tough As Nails API.
 * Uses the local Capability stub and manual lookup.
 */
public class ThirstHelper {

    /**
     * Returns the thirst capability data for the given player.
     * In 1.7.10, capabilities are simulated through stubbed capability providers.
     */
    public static IThirst getThirstData(EntityPlayer player) {
        Capability<IThirst> cap = TANCapabilities.THIRST;
        if (cap == null) {
            return null;
        }

        CapabilityProvider<IThirst> provider = new CapabilityProvider<IThirst>(cap);
        // Use our stub EnumFacing (null in original 1.9.4 call)
        return provider.getCapability(cap, CapabilityProvider.EnumFacing.UP);
    }
}
