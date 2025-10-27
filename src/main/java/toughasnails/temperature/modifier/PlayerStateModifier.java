// File: toughasnails/temperature/modifier/PlayerStateModifier.java
package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/**
 * Player-state temperature modifier (sprinting + health).
 * Fully back-ported to Forge 1.7.10 (no BlockPos / 1.8 calls).
 */
public class PlayerStateModifier extends TemperatureModifier {

    private static final int SPRINTING_RATE_MODIFIER   = 200; // ± 200 ticks
    private static final int SPRINTING_TARGET_MODIFIER = 3;   // ± 3 levels

    public PlayerStateModifier(TemperatureDebugger debugger) { super(debugger); }

    /* -----------------------------------------------------------
       Change-rate: sprinting and low health influence speed
       ----------------------------------------------------------- */
    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int newRate = changeRate;

        // Sprinting: speeds cooling when trending up (hotter), slows when trending down (cooler)
        int sprintMod = player.isSprinting() ? SPRINTING_RATE_MODIFIER : 0;
        if (trend == TemperatureTrend.INCREASING)  sprintMod *= -1;
        if (trend == TemperatureTrend.STILL)       sprintMod  = 0;

        debugger.start(TemperatureDebugger.Modifier.SPRINTING_RATE, newRate);
        newRate += sprintMod;
        debugger.end(newRate);

        // Health: lower health => slower changes
        debugger.start(TemperatureDebugger.Modifier.HEALTH_RATE, newRate);
        float max = player.getMaxHealth() <= 0 ? 1.0F : player.getMaxHealth();
        float healthFrac = player.getHealth() / max; // 1.0 = healthy
        newRate -= (int)((1.0F - healthFrac) * 200.0F);
        debugger.end(newRate);

        return newRate;
    }

    /* -----------------------------------------------------------
       Target shift: sprinting raises target temperature slightly
       ----------------------------------------------------------- */
    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int out = temperature.getRawValue();

        debugger.start(TemperatureDebugger.Modifier.SPRINTING_TARGET, out);
        if (player.isSprinting()) out += SPRINTING_TARGET_MODIFIER;
        // Clamp to valid range
        out = Math.max(0, Math.min(TemperatureScale.getScaleTotal(), out));
        debugger.end(out);

        return new Temperature(out);
    }
}
