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

/** Tempered biome baseline; lets day/night swing do the heavy lifting. */
public class BiomeModifier extends TemperatureModifier {

    private static final int MAX_TEMP_OFFSET = 6;         // ±6 (reduced)
    private static final float EQUILIBRIUM_PULL = 0.40f;  // soften toward midpoint
    private static final int COMFORT_BAND = 2;
    private static final int HUMIDITY_RATE_MAX = 220;
    private static final int NETHER_OFFSET = 6;
    private static final int END_OFFSET    = -2;

    public BiomeModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        BiomeGenBase biome = world.getBiomeGenForCoords(MathHelper.floor_double(player.posX),
                                                        MathHelper.floor_double(player.posZ));
        float humidity = MathHelper.clamp_float(biome.rainfall, 0F, 1F);
        int added = MathHelper.floor_float(humidity * HUMIDITY_RATE_MAX);
        int out = Math.max(20, changeRate + added);
        debugger.start(TemperatureDebugger.Modifier.BIOME_HUMIDITY_RATE, changeRate);
        debugger.end(out);
        return out;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        final int base     = temperature.getRawValue();
        final int midpoint = TemperatureScale.getScaleTotal() / 2;

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

        final int x = MathHelper.floor_double(player.posX);
        final int z = MathHelper.floor_double(player.posZ);
        BiomeGenBase b0 = world.getBiomeGenForCoords(x, z);
        BiomeGenBase bN = world.getBiomeGenForCoords(x, z - 10);
        BiomeGenBase bS = world.getBiomeGenForCoords(x, z + 10);
        BiomeGenBase bE = world.getBiomeGenForCoords(x + 10, z);
        BiomeGenBase bW = world.getBiomeGenForCoords(x - 10, z);

        float n0 = BiomeUtils.getBiomeTempNorm(b0);
        float nN = BiomeUtils.getBiomeTempNorm(bN);
        float nS = BiomeUtils.getBiomeTempNorm(bS);
        float nE = BiomeUtils.getBiomeTempNorm(bE);
        float nW = BiomeUtils.getBiomeTempNorm(bW);

        float avgNorm  = (n0 + nN + nS + nE + nW) / 5.0F;
        float centered = (avgNorm * 2.0F) - 1.0F;                 // -1..+1
        int rawOffset  = MathHelper.floor_float(centered * MAX_TEMP_OFFSET);

        if (b0 != null && b0.getEnableSnow()) rawOffset -= 1;
        if (b0 != null && b0.biomeName != null &&
            b0.biomeName.toLowerCase().contains("desert")) rawOffset += 1; // smaller desert bias

        int y = MathHelper.floor_double(player.posY);
        if (y >  90) rawOffset -= 1;
        if (y > 120) rawOffset -= 1;

        if (Math.abs(rawOffset) <= COMFORT_BAND) rawOffset = 0;

        float softened = rawOffset * (1.0f - EQUILIBRIUM_PULL);
        int   finalOffset = MathHelper.floor_float(softened);

        int out = MathHelper.clamp_int(midpoint + finalOffset, 0, TemperatureScale.getScaleTotal());
        debugger.start(TemperatureDebugger.Modifier.BIOME_TEMPERATURE_TARGET, base);
        debugger.end(out);
        return new Temperature(out);
    }
}
