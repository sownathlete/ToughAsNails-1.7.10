/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  org.lwjgl.util.Color
 */
package toughasnails.util;

import org.lwjgl.util.Color;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;

public class SeasonColourUtil {
    public static int multiplyColours(int colour1, int colour2) {
        return (int)((float)colour1 / 255.0f * ((float)colour2 / 255.0f) * 255.0f);
    }

    public static int overlayBlendChannel(int underColour, int overColour) {
        int retVal;
        if (underColour < 128) {
            retVal = SeasonColourUtil.multiplyColours(2 * underColour, overColour);
        } else {
            retVal = SeasonColourUtil.multiplyColours(2 * (255 - underColour), 255 - overColour);
            retVal = 255 - retVal;
        }
        return retVal;
    }

    public static int overlayBlend(int underColour, int overColour) {
        int r = SeasonColourUtil.overlayBlendChannel(underColour >> 16 & 0xFF, overColour >> 16 & 0xFF);
        int g = SeasonColourUtil.overlayBlendChannel(underColour >> 8 & 0xFF, overColour >> 8 & 0xFF);
        int b = SeasonColourUtil.overlayBlendChannel(underColour & 0xFF, overColour & 0xFF);
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static int saturateColour(int colour, float saturationMultiplier) {
        Color newColour = SeasonColourUtil.getColourFromInt(colour);
        float[] hsb = newColour.toHSB(null);
        hsb[1] = hsb[1] * saturationMultiplier;
        newColour.fromHSB(hsb[0], hsb[1], hsb[2]);
        return SeasonColourUtil.getIntFromColour(newColour);
    }

    public static int applySeasonalGrassColouring(Season.SubSeason season, int originalColour) {
        int overlay = season.getGrassOverlay();
        float saturationMultiplier = season.getGrassSaturationMultiplier();
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            overlay = Season.SubSeason.MID_SUMMER.getGrassOverlay();
            saturationMultiplier = Season.SubSeason.MID_SUMMER.getGrassSaturationMultiplier();
        }
        int newColour = overlay == 16777215 ? originalColour : SeasonColourUtil.overlayBlend(originalColour, overlay);
        return saturationMultiplier != -1.0f ? SeasonColourUtil.saturateColour(newColour, saturationMultiplier) : newColour;
    }

    public static int applySeasonalFoliageColouring(Season.SubSeason season, int originalColour) {
        int overlay = season.getFoliageOverlay();
        float saturationMultiplier = season.getFoliageSaturationMultiplier();
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            overlay = Season.SubSeason.MID_SUMMER.getFoliageOverlay();
            saturationMultiplier = Season.SubSeason.MID_SUMMER.getFoliageSaturationMultiplier();
        }
        int newColour = overlay == 16777215 ? originalColour : SeasonColourUtil.overlayBlend(originalColour, overlay);
        return saturationMultiplier != -1.0f ? SeasonColourUtil.saturateColour(newColour, saturationMultiplier) : newColour;
    }

    private static Color getColourFromInt(int colour) {
        return new Color(colour >> 16 & 0xFF, colour >> 8 & 0xFF, colour & 0xFF);
    }

    private static int getIntFromColour(Color colour) {
        return (colour.getRed() & 0xFF) << 16 | (colour.getGreen() & 0xFF) << 8 | colour.getBlue() & 0xFF;
    }
}

