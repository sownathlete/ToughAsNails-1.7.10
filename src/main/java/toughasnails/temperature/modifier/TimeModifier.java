package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/**
 * Diurnal (time-of-day) temperature swing:
 *  - Warmest at NOON (t=6000), coldest at MIDNIGHT (t=18000)
 *  - Amplitude scales with biome humidity: arid biomes cool much more at night
 *
 * Keeps change-rate unchanged; only shifts the target.
 */
public class TimeModifier extends TemperatureModifier {

    // Base amplitude in levels at mid humidity; gets scaled by humidity below.
    private static final float BASE_RANGE = 5.0F; // ±5 at mid humidity

    // Amplitude scale by humidity: arid → bigger swing, humid → smaller swing
    private static final float ARID_MULT   = 1.60F; // deserts/mesa (rainfall ~0.0)
    private static final float HUMID_MULT  = 0.80F; // jungles/swamps  (rainfall ~1.0)

    public TimeModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        return changeRate; // rate unchanged
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int out = temperature.getRawValue();

        // --- Biome humidity (0..1). Missing biome falls back to 0.5.
        final int bx = MathHelper.floor_double(player.posX);
        final int bz = MathHelper.floor_double(player.posZ);
        BiomeGenBase biome = world.getBiomeGenForCoords(bx, bz);
        float humidity = 0.5F;
        if (biome != null) {
            humidity = MathHelper.clamp_float(biome.rainfall, 0F, 1F);
        }

        // Scale amplitude: map humidity 0..1 to ARID_MULT..HUMID_MULT (inverse lerp).
        float ampMult = ARID_MULT + (HUMID_MULT - ARID_MULT) * humidity; // arid -> 1.6, humid -> 0.8
        float amplitude = BASE_RANGE * ampMult;

        // --- Correct diurnal curve (cosine, phase-shifted so noon is hottest)
        // Minecraft day length = 24000 ticks. We want:
        //  t=6000  (noon)     => +1
        //  t=18000 (midnight) => -1
        long t = world.getWorldTime() % 24000L;
        // phase shift by -6000 to put noon at phase 0
        double phase = 2.0 * Math.PI * (((t - 6000L) % 24000L + 24000L) % 24000L) / 24000.0;
        float diurnal = (float) Math.cos(phase); // +1 at noon, -1 at midnight

        int before = out;
        debugger.start(TemperatureDebugger.Modifier.TIME_TARGET, out);

        out += Math.round(amplitude * diurnal);

        debugger.end(out);
        return new Temperature(out);
    }
}
