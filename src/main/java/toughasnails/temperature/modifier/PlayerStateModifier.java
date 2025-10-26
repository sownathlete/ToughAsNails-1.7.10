package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/**
 * Player-state temperature modifier (sprinting + health).
 * Fully back-ported to Forge 1.7.10 (no BlockPos / 1.8 calls).
 */
public class PlayerStateModifier extends TemperatureModifier {

    private static final int SPRINTING_RATE_MODIFIER   = 200; // ± 200 ticks
    private static final int SPRINTING_TARGET_MODIFIER = 3;   // ± 3 levels

    public PlayerStateModifier(TemperatureDebugger debugger) {
        super(debugger);
    }

    /* -----------------------------------------------------------
       Change-rate: sprinting and low health influence speed
       ----------------------------------------------------------- */
    @Override
    public int modifyChangeRate(World world, EntityPlayer player,
                                int changeRate, TemperatureTrend trend) {

        int newRate = changeRate;

        /* ----- Sprinting contribution ----- */
        int sprintMod = player.isSprinting() ? SPRINTING_RATE_MODIFIER : 0;
        if (trend == TemperatureTrend.INCREASING)  sprintMod *= -1;
        if (trend == TemperatureTrend.STILL)       sprintMod  = 0;

        debugger.start(TemperatureDebugger.Modifier.SPRINTING_RATE, newRate);
        newRate += sprintMod;
        debugger.end(newRate);

        /* ----- Health contribution ----- */
        debugger.start(TemperatureDebugger.Modifier.HEALTH_RATE, newRate);
        float healthFrac = player.getHealth() / player.getMaxHealth(); // 1.0 = healthy
        newRate -= (int) ((1.0F - healthFrac) * 200.0F);               // injury slows change
        debugger.end(newRate);

        return newRate;
    }

    /* -----------------------------------------------------------
       Target shift: sprinting raises target temperature slightly
       ----------------------------------------------------------- */
    @Override
    public Temperature modifyTarget(World world, EntityPlayer player,
                                    Temperature temperature) {

        int newLevel = temperature.getRawValue();

        debugger.start(TemperatureDebugger.Modifier.SPRINTING_TARGET, newLevel);
        if (player.isSprinting()) newLevel += SPRINTING_TARGET_MODIFIER;
        debugger.end(newLevel);

        return new Temperature(newLevel);
    }
}
