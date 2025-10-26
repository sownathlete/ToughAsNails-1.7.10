package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/** Warmer at midday, cooler at midnight, scaled a bit by biome extremity if available. */
public class TimeModifier extends TemperatureModifier {

    private static final int TIME_TARGET_RANGE = 5; // ±5 levels

    public TimeModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        return changeRate; // rate unchanged
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int newLevel = temperature.getRawValue();

        // 0..23999; peak warmth at 6000 (noon), coolest at 18000 (midnight)
        long t = world.getWorldTime() % 24000L;
        float timeNorm = (-Math.abs((t + 6000L) % 24000L - 12000L) + 6000F) / 6000F; // -1..+1

        debugger.start(TemperatureDebugger.Modifier.TIME_TARGET, newLevel);
        newLevel += Math.round(TIME_TARGET_RANGE * timeNorm);
        debugger.end(newLevel);

        return new Temperature(newLevel);
    }
}
