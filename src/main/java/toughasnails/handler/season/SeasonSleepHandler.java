package toughasnails.handler.season;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.season.SeasonSavedData;

public class SeasonSleepHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        // Run only on the server, at the START of tick
        if (event.phase != TickEvent.Phase.START || event.world.isRemote) {
            return;
        }

        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            return;
        }

        World world = event.world;
        if (!(world instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer) world;

        // Check if all players are asleep (1.7.10: must check manually)
        if (worldServer.playerEntities.isEmpty()) return;

        boolean allAsleep = true;
        for (Object obj : worldServer.playerEntities) {
            if (obj == null) continue;
            net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer) obj;
            if (!player.isPlayerFullyAsleep()) {
                allAsleep = false;
                break;
            }
        }

        if (allAsleep) {
            SeasonSavedData seasonData = SeasonHandler.getSeasonSavedData(worldServer);
            WorldInfo info = worldServer.getWorldInfo();

            long worldTime = info.getWorldTime();
            long timeDiff = 24000L - (worldTime + 24000L) % 24000L; // advance to next day

            seasonData.seasonCycleTicks += (int) timeDiff;
            seasonData.markDirty();

            SeasonHandler.sendSeasonUpdate(worldServer);
        }
    }
}
