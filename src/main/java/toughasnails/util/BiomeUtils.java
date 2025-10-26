package toughasnails.util;

import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeUtils {

    /** Returns the biome temperature normalized to [0,1] range. */
    public static float getBiomeTempNorm(BiomeGenBase biome) {
        // 1.7.10: 'temperature' is a public field
        return MathHelper.clamp_float(biome.temperature, 0.0f, 1.35f) / 1.35f;
    }

    /** Returns the biome temperature extremity from 0.5 midpoint. */
    public static float getBiomeTempExtremity(BiomeGenBase biome) {
        return Math.abs(getBiomeTempNorm(biome) * 2.0f - 1.0f);
    }
}
