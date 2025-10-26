/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.World
 */
package toughasnails.api.season;

import java.lang.reflect.Method;
import net.minecraft.world.World;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.ISeasonData;
import toughasnails.api.season.Season;

public class SeasonHelper {
    public static ISeasonData getSeasonData(World world) {
        ISeasonData data;
        try {
            data = !world.isRemote ? (ISeasonData)Class.forName("toughasnails.handler.season.SeasonHandler").getMethod("getServerSeasonData", World.class).invoke(null, new Object[]{world}) : (ISeasonData)Class.forName("toughasnails.handler.season.SeasonHandler").getMethod("getClientSeasonData", new Class[0]).invoke(null, new Object[0]);
        }
        catch (Exception e) {
            throw new RuntimeException("An error occurred obtaining season data", e);
        }
        return data;
    }

    public static boolean canSnowAtTempInSeason(Season season, float temperature) {
        return temperature < 0.15f || season == Season.WINTER && temperature <= 0.7f && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS);
    }
}

