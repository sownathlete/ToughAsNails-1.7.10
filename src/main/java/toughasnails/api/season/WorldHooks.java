package toughasnails.api.season;

import net.minecraft.world.World;
import toughasnails.api.season.Season;

/**
 * Backported version of Tough As Nails WorldHooks for Forge 1.7.10.
 * Replaces BlockPos usage with integer coordinates.
 */
public class WorldHooks {

    public static boolean canSnowAtInSeason(World world, int x, int y, int z, boolean checkLight, Season season) {
        try {
            Class<?> helper = Class.forName("toughasnails.season.SeasonASMHelper");
            return (Boolean) helper
                    .getMethod("canSnowAtInSeason", World.class, int.class, int.class, int.class, boolean.class, Season.class)
                    .invoke(null, world, x, y, z, checkLight, season);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred calling canSnowAtInSeason", e);
        }
    }

    public static boolean canBlockFreezeInSeason(World world, int x, int y, int z, boolean noWaterAdj, Season season) {
        try {
            Class<?> helper = Class.forName("toughasnails.season.SeasonASMHelper");
            return (Boolean) helper
                    .getMethod("canBlockFreezeInSeason", World.class, int.class, int.class, int.class, boolean.class, Season.class)
                    .invoke(null, world, x, y, z, noWaterAdj, season);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred calling canBlockFreezeInSeason", e);
        }
    }

    public static boolean isRainingAtInSeason(World world, int x, int y, int z, Season season) {
        try {
            Class<?> helper = Class.forName("toughasnails.season.SeasonASMHelper");
            return (Boolean) helper
                    .getMethod("isRainingAtInSeason", World.class, int.class, int.class, int.class, Season.class)
                    .invoke(null, world, x, y, z, season);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred calling isRainingAtInSeason", e);
        }
    }
}
