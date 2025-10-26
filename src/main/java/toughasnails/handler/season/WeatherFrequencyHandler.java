package toughasnails.handler.season;

import java.util.Random;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;

public class WeatherFrequencyHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;

        // Only run on server end phase
        if (event.phase != TickEvent.Phase.END || world.isRemote) {
            return;
        }

        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            return;
        }

        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();
        WorldInfo info = world.getWorldInfo();
        Random rand = world.rand;

        // SPRING: more frequent rain
        if (season == Season.SPRING) {
            if (!info.isRaining() && info.getRainTime() > 96000) {
                info.setRainTime(rand.nextInt(84000) + 12000);
            }
        }

        // SUMMER: more frequent thunderstorms
        else if (season == Season.SUMMER) {
            if (!info.isThundering() && info.getThunderTime() > 36000) {
                info.setThunderTime(rand.nextInt(24000) + 12000);
            }
        }

        // WINTER: reduce thunder & keep moderate precipitation
        else if (season == Season.WINTER) {
            if (info.isThundering()) {
                info.setThundering(false);
            }
            if (!info.isRaining() && info.getRainTime() > 36000) {
                info.setRainTime(rand.nextInt(24000) + 12000);
            }
        }
    }
}
