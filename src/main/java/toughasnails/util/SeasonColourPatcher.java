package toughasnails.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import toughasnails.api.season.Season;
import toughasnails.season.SeasonTime;

/**
 * 1.7.10-safe seasonal color patcher.
 * We snapshot the vanilla color tables once, then (re)build tinted tables
 * when the sub-season changes and push them via the official setters:
 *   - ColorizerGrass.setGrassBiomeColorizer(int[])
 *   - ColorizerFoliage.setFoliageBiomeColorizer(int[])
 */
@SideOnly(Side.CLIENT)
public final class SeasonColourPatcher {

    private static final int TABLE_SIZE = 256;               // vanilla 256x256 LUT
    private static final int ENTRIES    = TABLE_SIZE * TABLE_SIZE;

    // Snapshots of the original vanilla LUTs (captured on first init)
    private static int[] ORIGINAL_GRASS  = null;
    private static int[] ORIGINAL_FOLIAGE = null;

    // Track last applied sub-season to avoid unnecessary rebuilds
    private static Season.SubSeason lastApplied = null;

    private SeasonColourPatcher() {}

    /** Call once at client init. */
    public static void initOnce(Season.SubSeason current) {
        if (ORIGINAL_GRASS == null || ORIGINAL_FOLIAGE == null) {
            ORIGINAL_GRASS   = sampleVanillaGrass();
            ORIGINAL_FOLIAGE = sampleVanillaFoliage();
        }
        applyFor(current);
    }

    /** Call when the client sub-season may have changed. */
    public static void applyFor(Season.SubSeason current) {
        if (current == null || current == lastApplied) return;

        int[] grass   = tintedFromOriginal(ORIGINAL_GRASS,   current, true);
        int[] foliage = tintedFromOriginal(ORIGINAL_FOLIAGE, current, false);

        ColorizerGrass.setGrassBiomeColorizer(grass);
        ColorizerFoliage.setFoliageBiomeColorizer(foliage);

        lastApplied = current;
    }

    // ----- internals -----

    private static int[] sampleVanillaGrass() {
        int[] table = new int[ENTRIES];
        int idx = 0;
        for (int y = 0; y < TABLE_SIZE; y++) {
            for (int x = 0; x < TABLE_SIZE; x++, idx++) {
                // Vanilla samples humidity first param (temp) and rainfall second (rain)
                double temp = x / 255.0;
                double rain = y / 255.0;
                table[idx] = ColorizerGrass.getGrassColor(temp, rain);
            }
        }
        return table;
    }

    private static int[] sampleVanillaFoliage() {
        int[] table = new int[ENTRIES];
        int idx = 0;
        for (int y = 0; y < TABLE_SIZE; y++) {
            for (int x = 0; x < TABLE_SIZE; x++, idx++) {
                double temp = x / 255.0;
                double rain = y / 255.0;
                table[idx] = ColorizerFoliage.getFoliageColor(temp, rain);
            }
        }
        return table;
    }

    private static int[] tintedFromOriginal(int[] original, Season.SubSeason subSeason, boolean isGrass) {
        int[] out = new int[ENTRIES];
        for (int i = 0; i < ENTRIES; i++) {
            int base = original[i];
            int tinted = isGrass
                    ? SeasonColourUtil.applySeasonalGrassColouring(subSeason, base)
                    : SeasonColourUtil.applySeasonalFoliageColouring(subSeason, base);
            out[i] = tinted;
        }
        return out;
    }
}
