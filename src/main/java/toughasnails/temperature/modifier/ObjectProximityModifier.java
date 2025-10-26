package toughasnails.temperature.modifier;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.temperature.Temperature;
import toughasnails.config.TANConfig;
import toughasnails.temperature.BlockTemperatureData;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

public class ObjectProximityModifier extends TemperatureModifier {

    public ObjectProximityModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int newRate = changeRate;

        int px = (int)Math.floor(player.posX);
        int py = (int)Math.floor(player.posY);
        int pz = (int)Math.floor(player.posZ);

        int heatSources = 0;
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block b = world.getBlock(px + x, py + y - 1, pz + z);
                    float t = getBlockTemperature(player, b);
                    if (t != 0.0f) heatSources++;
                }
            }
        }

        // pull faster toward target when many heat/cold sources are around (but never crazy-fast)
        // each source reduces ~10 ticks, capped to avoid < 10 globally by handler
        newRate = Math.max(20, newRate - (heatSources * 10));

        debugger.start(TemperatureDebugger.Modifier.NEARBY_BLOCKS_RATE, changeRate);
        debugger.end(newRate);
        return newRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int level = temperature.getRawValue();

        int px = (int)Math.floor(player.posX);
        int py = (int)Math.floor(player.posY);
        int pz = (int)Math.floor(player.posZ);

        float sum = 0.0f;
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block b = world.getBlock(px + x, py + y - 1, pz + z);
                    sum += getBlockTemperature(player, b);
                }
            }
        }

        int out = level + (int)sum;

        debugger.start(TemperatureDebugger.Modifier.NEARBY_BLOCKS_TARGET, level);
        debugger.end(out);

        return new Temperature(out);
    }

    public static float getBlockTemperature(EntityPlayer player, Block block) {
        if (block == null) return 0.0f;

        World world = player.worldObj;
        Material mat = block.getMaterial();

        // Config-defined block temps (exact block match)
        String blockName = Block.blockRegistry.getNameForObject(block);
        if (blockName != null && TANConfig.blockTemperatureData.containsKey(blockName)) {
            ArrayList<BlockTemperatureData> list = TANConfig.blockTemperatureData.get(blockName);
            for (BlockTemperatureData d : list) {
                if (d.block == block) return d.blockTemperature;
            }
        }

        // Material-based defaults
        if (mat == Material.fire)  return TANConfig.materialTemperatureData.fire;
        if (mat == Material.lava)  return TANConfig.materialTemperatureData.fire * 6.0f; // treat lava as very hot
        if (mat == Material.ice)   return -2.0f;

        return 0.0f;
    }
}
