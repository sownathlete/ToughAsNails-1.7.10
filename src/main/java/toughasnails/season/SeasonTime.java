/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.season;

import toughasnails.api.season.ISeasonData;
import toughasnails.api.season.Season;

public final class SeasonTime
implements ISeasonData {
    public static final int DAY_TICKS = 24000;
    public static final int SUB_SEASON_DURATION = 5;
    public static final int SEASON_TICKS = 360000;
    public static final int TOTAL_CYCLE_TICKS = 120000 * Season.SubSeason.values().length;
    public final int time;

    public SeasonTime(int time) {
        this.time = time;
    }

    public int getDay() {
        return this.time / 24000;
    }

    @Override
    public int getSeasonCycleTicks() {
        return this.time;
    }

    @Override
    public Season.SubSeason getSubSeason() {
        int index = this.getDay() / 5 % Season.SubSeason.values().length;
        return Season.SubSeason.values()[index];
    }

    public Season getSeason() {
        return this.getSubSeason().getSeason();
    }
}

