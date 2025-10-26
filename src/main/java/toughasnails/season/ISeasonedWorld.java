package toughasnails.season;

import toughasnails.api.season.Season;

/**
 * Backported ISeasonedWorld for Forge 1.7.10.
 * Replaces BlockPos with (x, y, z) integer coordinates.
 */
public interface ISeasonedWorld {

    /**
     * Determines if snow can form at the specified coordinates during a given season.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @param checkLight Whether to check light level (vanilla snow behavior)
     * @param season The current season
     * @return True if snow can form here this season
     */
    public boolean canSnowAtInSeason(int x, int y, int z, boolean checkLight, Season season);

    /**
     * Determines if a block can freeze at the specified coordinates during a given season.
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @param noWaterAdj Whether to skip adjacent water checks
     * @param season The current season
     * @return True if this block can freeze in this season
     */
    public boolean canBlockFreezeInSeason(int x, int y, int z, boolean noWaterAdj, Season season);
}
