// File: toughasnails/temperature/modifier/SeasonModifier.java
package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

public class SeasonModifier extends TemperatureModifier {

    public SeasonModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        return changeRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int out = temperature.getRawValue();

        Season.SubSeason season = SeasonHelper.getSeasonData(world).getSubSeason();
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            season = Season.SubSeason.MID_SUMMER;
        }

        debugger.start(TemperatureDebugger.Modifier.SEASON_TARGET, out);

        if (world.provider != null && world.provider.isSurfaceWorld()) {
            switch (season) {
                case MID_WINTER:
                case LATE_WINTER:
                    out -= 6; break;
                case EARLY_SPRING:
                case EARLY_WINTER:
                    out -= 4; break;
                case MID_SPRING:
                case LATE_AUTUMN:
                    out -= 2; break;
                case MID_SUMMER:
                case EARLY_AUTUMN:
                    out += 2; break;
                case LATE_SUMMER:
                    out += 4; break;
                default:
                    break;
            }
        }

        // Clamp to valid range
        out = Math.max(0, Math.min(TemperatureScale.getScaleTotal(), out));

        debugger.end(out);
        return new Temperature(out);
    }
}
