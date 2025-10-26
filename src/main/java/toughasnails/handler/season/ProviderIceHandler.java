package toughasnails.handler.season;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import toughasnails.season.SeasonASMHelper;

public class ProviderIceHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPopulateChunkEvent(PopulateChunkEvent.Populate event) {
        World world = event.world;
        Random rand = event.rand;

        // 1.7.10: no BlockPos — use raw coordinates
        int baseX = event.chunkX * 16;
        int baseZ = event.chunkZ * 16;

        if (event.type == PopulateChunkEvent.Populate.EventType.ICE) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int worldX = baseX + x;
                    int worldZ = baseZ + z;

                    // getPrecipitationHeight() → world.getPrecipitationHeight(x,z)
                    int topY = world.getPrecipitationHeight(worldX, worldZ);
                    int belowY = topY - 1;

                    Block below = world.getBlock(worldX, belowY, worldZ);
                    Block top = world.getBlock(worldX, topY, worldZ);

                    // use seasonal checks
                    if (SeasonASMHelper.canBlockFreezeInSeason(world, worldX, belowY, worldZ, false, null)) {
                        world.setBlock(worldX, belowY, worldZ, Blocks.ice);
                    }
                    if (SeasonASMHelper.canSnowAtInSeason(world, worldX, topY, worldZ, true, null)) {
                        world.setBlock(worldX, topY, worldZ, Blocks.snow_layer);
                    }
                }
            }
            event.setResult(Event.Result.DENY);
        }
    }
}
