package toughasnails.handler.season;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;

public class StopSpawnHandler {

    @SubscribeEvent
    public void onCheckEntitySpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.world;
        if (world == null || world.isRemote) return;

        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();

        // Deny passive animal spawns during winter if seasons are enabled
        if (season == Season.WINTER
                && event.entity instanceof EntityAnimal
                && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onChunkPopulate(PopulateChunkEvent.Populate event) {
        World world = event.world;
        if (world == null || world.isRemote) return;

        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();

        // Disable animal population generation during winter
        if (event.type == PopulateChunkEvent.Populate.EventType.ANIMALS
                && season == Season.WINTER
                && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            event.setResult(Event.Result.DENY);
        }
    }
}
