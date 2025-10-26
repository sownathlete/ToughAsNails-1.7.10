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
import toughasnails.temperature.modifier.TemperatureModifier;

public class ObjectProximityModifier extends TemperatureModifier {

    public ObjectProximityModifier(TemperatureDebugger debugger) {
        super(debugger);
    }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int newChangeRate = changeRate;

        int px = (int) Math.floor(player.posX);
        int py = (int) Math.floor(player.posY);
        int pz = (int) Math.floor(player.posZ);

        int tempSourceBlocks = 0;
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    int bx = px + x;
                    int by = py + y - 1;
                    int bz = pz + z;
                    Block block = world.getBlock(bx, by, bz);
                    if (getBlockTemperature(player, block) != 0.0f) {
                        tempSourceBlocks++;
                    }
                }
            }
        }

        debugger.start(TemperatureDebugger.Modifier.NEARBY_BLOCKS_RATE, newChangeRate);
        newChangeRate -= tempSourceBlocks * 20;
        debugger.end(newChangeRate);

        return newChangeRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int newTemperatureLevel = temperature.getRawValue();

        int px = (int) Math.floor(player.posX);
        int py = (int) Math.floor(player.posY);
        int pz = (int) Math.floor(player.posZ);

        float blockTemperatureModifier = 0.0f;
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    int bx = px + x;
                    int by = py + y - 1;
                    int bz = pz + z;
                    Block block = world.getBlock(bx, by, bz);
                    blockTemperatureModifier += getBlockTemperature(player, block);
                }
            }
        }

        debugger.start(TemperatureDebugger.Modifier.NEARBY_BLOCKS_TARGET, newTemperatureLevel);
        newTemperatureLevel += (int) blockTemperatureModifier;
        debugger.end(newTemperatureLevel);

        return new Temperature(newTemperatureLevel);
    }

    public static float getBlockTemperature(EntityPlayer player, Block block) {
        World world = player.worldObj;
        Material material = block.getMaterial();
        BiomeGenBase biome = world.getBiomeGenForCoords((int) player.posX, (int) player.posZ);

        String blockName = Block.blockRegistry.getNameForObject(block);
        if (blockName == null) return 0.0f;

        if (TANConfig.blockTemperatureData.containsKey(blockName)) {
            ArrayList<BlockTemperatureData> blockTempData = TANConfig.blockTemperatureData.get(blockName);
            for (BlockTemperatureData tempData : blockTempData) {
                // Compare by Block reference only
                if (tempData.block == block) {
                    return tempData.blockTemperature;
                }
            }
            return 0.0f;
        }

        if (material == Material.fire) {
            return TANConfig.materialTemperatureData.fire;
        }

        return 0.0f;
    }
}
