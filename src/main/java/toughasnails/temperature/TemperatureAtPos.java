// File: toughasnails/temperature/TemperatureAtPos.java
package toughasnails.temperature;

import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;

/**
 * Computes the *environment* temperature target for a specific block position.
 * Mirrors the same influences the player target uses, but excludes player-only
 * factors (armor, sprinting, health). Used by the Temperature Gauge.
 */
public final class TemperatureAtPos {

    // how strongly altitude alone affects temp at block positions
    private static final int ALTITUDE_MAX_DELTA = 3; // ±3 across large height changes

    private TemperatureAtPos() {}

    public static Temperature compute(World world, int x, int y, int z) {
        final int total     = TemperatureScale.getScaleTotal();
        final int midpoint  = total / 2;
        int out = midpoint;

        // --- 1) Dimension nudges (kept modest — big swings are handled by time-of-day) ---
        if (world.provider != null && world.provider.isHellWorld) {
            out += 6; // strong warm
        } else if ("the_end".equalsIgnoreCase(String.valueOf(world.provider.getDimensionName()))) {
            out -= 2; // slightly cool
        }

        // --- 2) Biome baseline (tempered) ---
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        float biomeNorm = getBiomeTempNorm(biome);   // 0..1 (cold..hot)
        float centered  = (biomeNorm * 2f) - 1f;     // -1..+1
        int biomeDelta  = MathHelper.floor_float(centered * 6f); // ±6 baseline
        // small flavor: snow biomes -1; deserts +1
        if (biome != null && biome.getEnableSnow()) biomeDelta -= 1;
        if (biome != null && biome.biomeName != null &&
            biome.biomeName.toLowerCase().contains("desert")) biomeDelta += 1;
        out += biomeDelta;

        // --- 3) Altitude (cooler high, warmer below sea level) ---
        double dy = y - 64.0;
        int alt = MathHelper.floor_double((Math.abs(dy) / 64.0) * ALTITUDE_MAX_DELTA);
        if (dy > 0) out -= alt; else if (dy < 0) out += alt;

        // --- 4) Time-of-day swing scaled by biome (deserts swing big at night) ---
        out += dayNightSwing(world, x, y, z, biomeNorm);

        // --- 5) Weather (cooling when wet/snow exposed) ---
        out += weatherDelta(world, x, y, z, biome);

        // clamp to valid scale
        out = MathHelper.clamp_int(out, 0, total);
        return new Temperature(out);
    }

    /** Biome base temperature mapped to 0..1 (cold..hot). */
    private static float getBiomeTempNorm(BiomeGenBase b) {
        if (b == null) return 0.5f;
        // vanilla 1.7 “temperature” is ~0.0..2.0 with 0.15 snow threshold
        float raw = MathHelper.clamp_float(b.temperature, 0.0F, 2.0F);
        // map: <0.15 -> ~0.0, 0.45->~0.33, 0.85->~0.66, >=1.0->~0.75+
        // simple normalize to ~0..1
        return MathHelper.clamp_float(raw / 2.0F, 0.0F, 1.0F);
    }

    /** Big day/night swing in hot biomes, moderate in temperate, small in cold. */
    private static int dayNightSwing(World world, int x, int y, int z, float biomeNorm) {
        // -1..+1 where +1 ≈ noon, -1 ≈ midnight (same core shape as your TimeModifier)
        long t = world.getWorldTime() % 24000L;
        float timeNorm = (-Math.abs((t + 6000L) % 24000L - 12000L) + 6000F) / 6000F; // -1..+1

        // amplitude: deserts/hot biomes swing harder
        // biomeNorm 0..1 -> centered -1..+1 -> amplitude 4..12
        float centered = (biomeNorm * 2f) - 1f;
        float amp = 4f + 8f * Math.abs(centered); // 4 (cold biomes) .. 12 (hot biomes)

        // indoor/covered reduces swing a bit (less sky heating/cooling)
        boolean sky = world.canBlockSeeTheSky(x, y, z);
        if (!sky) amp *= 0.7f;

        // convert to integer delta
        return MathHelper.floor_float(amp * timeNorm);
        // result: hot desert ~+12 day / -12 night -> nights will actually feel cold
    }

    private static int weatherDelta(World world, int x, int y, int z, BiomeGenBase biome) {
        if (!world.isRaining()) return 0;

        // exposed?
        boolean exposed = world.canBlockSeeTheSky(x, y, z);
        if (!exposed) return 0;

        if (biome != null && biome.getEnableSnow()) {
            return -10; // snowing
        }
        return -6; // raining
    }
}
