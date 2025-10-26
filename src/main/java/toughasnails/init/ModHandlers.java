package toughasnails.init;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import toughasnails.config.SyncedConfigHandler;
import toughasnails.handler.AchievementEventHandler;
import toughasnails.handler.ExtendedStatHandler;
import toughasnails.handler.PacketHandler;
import toughasnails.handler.health.HealthOverlayHandler;
import toughasnails.handler.health.MaxHealthHandler;
import toughasnails.handler.season.ProviderIceHandler;
import toughasnails.handler.season.RandomUpdateHandler;
import toughasnails.handler.season.SeasonHandler;
import toughasnails.handler.season.SeasonSleepHandler;
import toughasnails.handler.season.StopSpawnHandler;
import toughasnails.handler.season.WeatherFrequencyHandler;
import toughasnails.handler.temperature.TemperatureDebugOverlayHandler;
import toughasnails.handler.temperature.TemperatureOverlayHandler;
import toughasnails.handler.thirst.FillBottleHandler;
import toughasnails.handler.thirst.ThirstOverlayHandler;
import toughasnails.handler.thirst.ThirstStatHandler;
import toughasnails.handler.thirst.VanillaDrinkHandler;
import toughasnails.season.SeasonTime;
import toughasnails.util.SeasonColourPatcher;

public class ModHandlers {

    public static void init() {
        PacketHandler.init();

        // Common handlers
        MinecraftForge.EVENT_BUS.register(new ExtendedStatHandler());
        MinecraftForge.EVENT_BUS.register(new SyncedConfigHandler());
        MinecraftForge.EVENT_BUS.register(new ThirstStatHandler());
        MinecraftForge.EVENT_BUS.register(new VanillaDrinkHandler());
        MinecraftForge.EVENT_BUS.register(new FillBottleHandler());
        MinecraftForge.EVENT_BUS.register(new MaxHealthHandler());
        MinecraftForge.EVENT_BUS.register(new SeasonHandler());
        MinecraftForge.EVENT_BUS.register(new RandomUpdateHandler());
        MinecraftForge.EVENT_BUS.register(new ProviderIceHandler());
        MinecraftForge.EVENT_BUS.register(new SeasonSleepHandler());

        StopSpawnHandler stop = new StopSpawnHandler();
        MinecraftForge.EVENT_BUS.register(stop);
        MinecraftForge.EVENT_BUS.register(new WeatherFrequencyHandler());
        MinecraftForge.EVENT_BUS.register(new AchievementEventHandler());

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            // Client-only overlays
            MinecraftForge.EVENT_BUS.register(new TemperatureOverlayHandler());
            MinecraftForge.EVENT_BUS.register(new TemperatureDebugOverlayHandler());
            MinecraftForge.EVENT_BUS.register(new ThirstOverlayHandler());
            MinecraftForge.EVENT_BUS.register(new HealthOverlayHandler());

            // Initialize seasonal color tables once at startup
            initSeasonColoursClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private static void initSeasonColoursClient() {
        // Build tables for the current client sub-season
        SeasonTime now = new SeasonTime(SeasonHandler.clientSeasonCycleTicks);
        SeasonColourPatcher.initOnce(now.getSubSeason());
    }
}
