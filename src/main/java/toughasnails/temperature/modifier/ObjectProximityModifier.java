// File: toughasnails/temperature/modifier/ObjectProximityModifier.java
package toughasnails.temperature.modifier;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import toughasnails.api.temperature.Temperature;
import toughasnails.config.TANConfig;
import toughasnails.temperature.BlockTemperatureData;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

public class ObjectProximityModifier extends TemperatureModifier {

    // Tuning so proximity can’t explode:
    private static final int   RADIUS_XZ          = 2;    // scan -2..+2 (5×5)
    private static final int   RADIUS_Y           = 1;    // scan -1..+1 (3 high)
    private static final int   MAX_HEAT_SOURCES   = 8;    // only first N sources affect rate
    private static final float PER_BLOCK_CLAMP    = 3.0f; // any single block contributes at most ±3
    private static final float TOTAL_CLAMP        = 6.0f; // sum of all blocks clamped to ±6

    public ObjectProximityModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int newRate = changeRate;

        final int px = (int)Math.floor(player.posX);
        final int py = (int)Math.floor(player.posY);
        final int pz = (int)Math.floor(player.posZ);

        int heatSources = 0;
        outer:
        for (int x = -RADIUS_XZ; x <= RADIUS_XZ; x++) {
            for (int y = -RADIUS_Y; y <= RADIUS_Y; y++) {
                for (int z = -RADIUS_XZ; z <= RADIUS_XZ; z++) {
                    Block b = world.getBlock(px + x, py + y - 1, pz + z);
                    float t = clampf(getBlockTemperature(player, b), -PER_BLOCK_CLAMP, PER_BLOCK_CLAMP);
                    if (t != 0.0f) {
                        heatSources++;
                        if (heatSources >= MAX_HEAT_SOURCES) break outer;
                    }
                }
            }
        }

        // Pull faster toward target when many sources are around (but never crazy-fast)
        newRate = Math.max(20, newRate - (heatSources * 10));

        debugger.start(TemperatureDebugger.Modifier.NEARBY_BLOCKS_RATE, changeRate);
        debugger.end(newRate);
        return newRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int level = temperature.getRawValue();

        final int px = (int)Math.floor(player.posX);
        final int py = (int)Math.floor(player.posY);
        final int pz = (int)Math.floor(player.posZ);

        float sum = 0.0f;
        for (int x = -RADIUS_XZ; x <= RADIUS_XZ; x++) {
            for (int y = -RADIUS_Y; y <= RADIUS_Y; y++) {
                for (int z = -RADIUS_XZ; z <= RADIUS_XZ; z++) {
                    Block b = world.getBlock(px + x, py + y - 1, pz + z);
                    float t = clampf(getBlockTemperature(player, b), -PER_BLOCK_CLAMP, PER_BLOCK_CLAMP);
                    sum += t;
                }
            }
        }

        // Global clamp so proximity can’t shove you to extremes
        sum = clampf(sum, -TOTAL_CLAMP, TOTAL_CLAMP);

        int out = level + (int)sum;

        debugger.start(TemperatureDebugger.Modifier.NEARBY_BLOCKS_TARGET, level);
        debugger.end(out);

        return new Temperature(out);
    }

    public static float getBlockTemperature(EntityPlayer player, Block block) {
        if (block == null) return 0.0f;

        // Config-defined block temps (exact block match)
        String blockName = Block.blockRegistry.getNameForObject(block);
        if (blockName != null && TANConfig.blockTemperatureData.containsKey(blockName)) {
            ArrayList<BlockTemperatureData> list = TANConfig.blockTemperatureData.get(blockName);
            for (BlockTemperatureData d : list) {
                if (d.block == block) return d.blockTemperature;
            }
        }

        // Material-based defaults
        Material mat = block.getMaterial();
        if (mat == Material.fire)  return TANConfig.materialTemperatureData.fire;
        if (mat == Material.lava)  return TANConfig.materialTemperatureData.fire * 6.0f; // very hot
        if (mat == Material.ice)   return -2.0f;

        return 0.0f;
    }

    private static float clampf(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
        }
}
