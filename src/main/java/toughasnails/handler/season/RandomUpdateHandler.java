package toughasnails.handler.season;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;

public class RandomUpdateHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        // server side, END phase only
        if (event.phase != Phase.END || event.world.isRemote) return;

        World world = event.world;

        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();
        Season.SubSeason subSeason = SeasonHelper.getSeasonData(world).getSubSeason();

        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            season = Season.SUMMER;
            subSeason = Season.SubSeason.MID_SUMMER;
        }

        // skip winter (this handler *removes* ice/snow when warm)
        if (subSeason == Season.SubSeason.EARLY_WINTER
         || subSeason == Season.SubSeason.MID_WINTER
         || subSeason == Season.SubSeason.LATE_WINTER) {
            return;
        }

        // Iterate loaded chunks (1.7.10: use ChunkProviderServer.loadedChunks)
        if (!(world.getChunkProvider() instanceof ChunkProviderServer)) return;
        ChunkProviderServer cps = (ChunkProviderServer) world.getChunkProvider();

        @SuppressWarnings("unchecked")
        List<Chunk> loaded = cps.loadedChunks;
        if (loaded == null || loaded.isEmpty()) return;

        for (Chunk chunk : loaded) {
            if (chunk == null) continue;

            int baseX = chunk.xPosition * 16;
            int baseZ = chunk.zPosition * 16;

            int randDiv;
            switch (subSeason) {
                case EARLY_SPRING: randDiv = 16; break;
                case MID_SPRING:   randDiv = 12; break;
                case LATE_SPRING:  randDiv = 8;  break;
                default:           randDiv = 4;  break;
            }

            if (world.rand.nextInt(randDiv) != 0) continue;

            // Pick a random column inside the chunk
            int x = baseX + world.rand.nextInt(16);
            int z = baseZ + world.rand.nextInt(16);
            int yTop = world.getPrecipitationHeight(x, z); // top block Y + 1
            int yGround = yTop - 1;

            Block ground = world.getBlock(x, yGround, z);
            Block above  = world.getBlock(x, yTop,   z);
            float temp   = world.getBiomeGenForCoords(x, z).getFloatTemperature(x, yGround, z);

            // Melt ice if it's too warm
            if (ground == Blocks.ice && !SeasonHelper.canSnowAtTempInSeason(season, temp)) {
                world.setBlock(x, yGround, z, Blocks.water);
            }

            // Remove snow layer if it's too warm
            if (above == Blocks.snow_layer && !SeasonHelper.canSnowAtTempInSeason(season, temp)) {
                world.setBlockToAir(x, yTop, z);
            }
        }
    }
}
