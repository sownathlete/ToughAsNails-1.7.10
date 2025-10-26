package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;
import toughasnails.util.BiomeUtils;

/**
 * Temperature modifier driven by surrounding biome (1.7.10 backport).
 * - Stronger, clearer biome deltas (snowy << temperate << desert << Nether)
 * - Altitude cools you
 * - Humidity slows change (rate never goes below a sane floor)
 */
public class BiomeModifier extends TemperatureModifier {

    /** Max absolute delta this modifier can contribute from midpoint. */
    private static final int MAX_TEMP_OFFSET = 12; // ±12 levels => very noticeable

    /** Humidity --> extra ticks added to rate (slows change). */
    private static final int HUMIDITY_RATE_MAX = 220; // up to +220 ticks at humidity=1

    public BiomeModifier(TemperatureDebugger debugger) {
        super(debugger);
    }

    /* ------------------------------------------------------------------
       Change-rate: humidity slows how fast you move toward target.
       Never let the rate drop below 20 ticks (1s) total after stacking.
       ------------------------------------------------------------------ */
    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int x = MathHelper.floor_double(player.posX);
        int z = MathHelper.floor_double(player.posZ);
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

        float humidity = biome.rainfall;                // 0..1 in vanilla 1.7.10
        humidity = MathHelper.clamp_float(humidity, 0F, 1F);
        int added = MathHelper.floor_float(humidity * HUMIDITY_RATE_MAX);

        int out = changeRate + added;                   // humidity slows => *adds* ticks

        // Clamp to a sane lower bound so nothing goes negative upstream
        if (out < 20) out = 20;

        debugger.start(TemperatureDebugger.Modifier.BIOME_HUMIDITY_RATE, changeRate);
        debugger.end(out);
        return out;
    }

    /* ------------------------------------------------------------------
       Target-temperature: average nearby biome temps, then bias by
       snow/desert and altitude; hard overrides for Nether/End.
       Called first in the handler (starting from midpoint), so
       adding/subtracting here is “absolute” per biome feel.
       ------------------------------------------------------------------ */
    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int base = temperature.getRawValue();

        // Dimension first (strongest)
        if (world.provider.isHellWorld) {               // Nether = hot
            debugger.start(TemperatureDebugger.Modifier.BIOME_TEMPERATURE_TARGET, base);
            int netherOut = base + (MAX_TEMP_OFFSET + 4);
            debugger.end(netherOut);
            return new Temperature(netherOut);
        }
        String dimName = world.provider.getDimensionName();
        if (dimName != null && "the_end".equalsIgnoreCase(dimName)) { // End = mildly cool
            debugger.start(TemperatureDebugger.Modifier.BIOME_TEMPERATURE_TARGET, base);
            int endOut = base - 2;
            debugger.end(endOut);
            return new Temperature(endOut);
        }

        // Sample center + 4 neighbors (10 blocks away) for smoother transitions
        int x = MathHelper.floor_double(player.posX);
        int z = MathHelper.floor_double(player.posZ);
        BiomeGenBase b0 = world.getBiomeGenForCoords(x,      z);
        BiomeGenBase bN = world.getBiomeGenForCoords(x,      z - 10);
        BiomeGenBase bS = world.getBiomeGenForCoords(x,      z + 10);
        BiomeGenBase bE = world.getBiomeGenForCoords(x + 10, z);
        BiomeGenBase bW = world.getBiomeGenForCoords(x - 10, z);

        float n0 = BiomeUtils.getBiomeTempNorm(b0);
        float nN = BiomeUtils.getBiomeTempNorm(bN);
        float nS = BiomeUtils.getBiomeTempNorm(bS);
        float nE = BiomeUtils.getBiomeTempNorm(bE);
        float nW = BiomeUtils.getBiomeTempNorm(bW);

        float avgNorm = (n0 + nN + nS + nE + nW) / 5.0F; // 0..1 (0=cold, 1=hot)
        float centered = (avgNorm * 2.0F) - 1.0F;        // -1..+1 around midpoint
        int   offset   = MathHelper.floor_float(centered * MAX_TEMP_OFFSET);

        // Bias for snow/desert flavor
        if (b0.getEnableSnow()) offset -= 2;
        if (b0.biomeName != null && b0.biomeName.toLowerCase().contains("desert")) offset += 3;

        // Altitude: >90 cools, >120 cools more
        int y = MathHelper.floor_double(player.posY);
        if (y > 90)  offset -= 2;
        if (y > 120) offset -= 2;

        int out = base + offset;

        debugger.start(TemperatureDebugger.Modifier.BIOME_TEMPERATURE_TARGET, base);
        debugger.end(out);

        return new Temperature(out);
    }
}
