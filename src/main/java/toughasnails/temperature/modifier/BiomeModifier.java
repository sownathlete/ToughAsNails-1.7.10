// File: toughasnails/temperature/modifier/BiomeModifier.java
package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;
import toughasnails.util.BiomeUtils;

/**
 * Biome-driven temperature modifier tuned to feel stable rather than extreme.
 *
 * Goals:
 *  • Biomes nudge you warmer/colder, but you settle near the middle instead of railroading to the ends.
 *  • Nether/End are strong nudges, not hard clamps.
 *  • Humidity still slows your rate of change.
 */
public class BiomeModifier extends TemperatureModifier {

    /** Max absolute delta this modifier contributes from midpoint (after all bias). */
    private static final int MAX_TEMP_OFFSET = 8;         // ±8 levels (was ±12)

    /** Soft pull back to the global midpoint, reduces runaway to extremes. 0..1 */
    private static final float EQUILIBRIUM_PULL = 0.45f;  // 45% pull toward midpoint

    /** Small “dead zone” where we don’t chase tiny offsets, helps the settle feel. */
    private static final int COMFORT_BAND = 2;

    /** Humidity --> extra ticks added to rate (slows change). */
    private static final int HUMIDITY_RATE_MAX = 220;     // up to +220 ticks at humidity=1

    /** Nether/End target offsets vs midpoint (kept within MAX_TEMP_OFFSET). */
    private static final int NETHER_OFFSET = Math.min(MAX_TEMP_OFFSET, 8); // strong warm
    private static final int END_OFFSET    = -2;                            // mildly cool

    public BiomeModifier(TemperatureDebugger debugger) {
        super(debugger);
    }

    /* ------------------------------------------------------------------
       Change-rate: humidity slows how fast you move toward target.
       Never let the rate drop below 20 ticks total after stacking.
       ------------------------------------------------------------------ */
    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int x = MathHelper.floor_double(player.posX);
        int z = MathHelper.floor_double(player.posZ);
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

        float humidity = MathHelper.clamp_float(biome.rainfall, 0F, 1F);
        int added = MathHelper.floor_float(humidity * HUMIDITY_RATE_MAX);

        int out = changeRate + added;
        if (out < 20) out = 20;

        debugger.start(TemperatureDebugger.Modifier.BIOME_HUMIDITY_RATE, changeRate);
        debugger.end(out);
        return out;
    }

    /* ------------------------------------------------------------------
       Target temperature:
       - Start from global midpoint (handler does) then add our *tempered* biome offset.
       - Offsets are softened by EQUILIBRIUM_PULL and a small comfort band to avoid ping-ponging.
       - Nether/End are strong nudges, not extremes.
       ------------------------------------------------------------------ */
    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        final int base     = temperature.getRawValue();
        final int midpoint = TemperatureScale.getScaleTotal() / 2;

        // Dimension nudges first (strongest, but not maxed)
        if (world.provider.isHellWorld) {
            int out = midpoint + NETHER_OFFSET;
            debugger.start(TemperatureDebugger.Modifier.BIOME_TEMPERATURE_TARGET, base);
            debugger.end(out);
            return new Temperature(out);
        }
        String dimName = world.provider.getDimensionName();
        if (dimName != null && "the_end".equalsIgnoreCase(dimName)) {
            int out = midpoint + END_OFFSET;
            debugger.start(TemperatureDebugger.Modifier.BIOME_TEMPERATURE_TARGET, base);
            debugger.end(out);
            return new Temperature(out);
        }

        // Sample center + 4 neighbors (10 blocks away) for smoother transitions
        final int x = MathHelper.floor_double(player.posX);
        final int z = MathHelper.floor_double(player.posZ);
        BiomeGenBase b0 = world.getBiomeGenForCoords(x,      z);
        BiomeGenBase bN = world.getBiomeGenForCoords(x,      z - 10);
        BiomeGenBase bS = world.getBiomeGenForCoords(x,      z + 10);
        BiomeGenBase bE = world.getBiomeGenForCoords(x + 10, z);
        BiomeGenBase bW = world.getBiomeGenForCoords(x - 10, z);

        float n0 = BiomeUtils.getBiomeTempNorm(b0); // 0..1 cold..hot
        float nN = BiomeUtils.getBiomeTempNorm(bN);
        float nS = BiomeUtils.getBiomeTempNorm(bS);
        float nE = BiomeUtils.getBiomeTempNorm(bE);
        float nW = BiomeUtils.getBiomeTempNorm(bW);

        float avgNorm  = (n0 + nN + nS + nE + nW) / 5.0F; // 0..1
        float centered = (avgNorm * 2.0F) - 1.0F;          // -1..+1 around midpoint
        int rawOffset  = MathHelper.floor_float(centered * MAX_TEMP_OFFSET);

        // Small biome flavor, but toned down
        if (b0.getEnableSnow()) rawOffset -= 1;                        // was -2
        if (b0.biomeName != null && b0.biomeName.toLowerCase().contains("desert")) rawOffset += 2; // was +3

        // Altitude: cool as you go high, also toned down
        int y = MathHelper.floor_double(player.posY);
        if (y >  90) rawOffset -= 1;
        if (y > 120) rawOffset -= 1;

        // Comfort band: ignore tiny offsets to let you settle near center
        if (Math.abs(rawOffset) <= COMFORT_BAND) rawOffset = 0;

        // Equilibrium pull: soften the push toward extremes so you stabilize
        float softened = rawOffset * (1.0f - EQUILIBRIUM_PULL);
        int   finalOffset = MathHelper.floor_float(softened);

        int out = midpoint + finalOffset;

        // Safety clamp to scale
        int min = 0;
        int max = TemperatureScale.getScaleTotal();
        out = MathHelper.clamp_int(out, min, max);

        debugger.start(TemperatureDebugger.Modifier.BIOME_TEMPERATURE_TARGET, base);
        debugger.end(out);
        return new Temperature(out);
    }
}
