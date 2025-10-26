package toughasnails.season;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.TANBlocks;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.IDecayableCrop;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.handler.season.SeasonHandler;
import toughasnails.season.SeasonTime;

public class SeasonASMHelper {

    public static boolean canSnowAtInSeason(World world, int x, int y, int z, boolean checkLight, Season season) {
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        float temperature = biome.getFloatTemperature(x, y, z);

        if (!SeasonHelper.canSnowAtTempInSeason(season, temperature)) {
            return false;
        }
        if (biome == BiomeGenBase.river || biome == BiomeGenBase.ocean || biome == BiomeGenBase.deepOcean) {
            return false;
        }

        if (checkLight) {
            if (y >= 0 && y < 256 && world.getSavedLightValue(EnumSkyBlock.Block, x, y, z) < 10) {
                Block block = world.getBlock(x, y, z);
                return block.isAir(world, x, y, z) && Blocks.snow_layer.canPlaceBlockAt(world, x, y, z);
            }
            return false;
        }

        return true;
    }

    public static boolean canBlockFreezeInSeason(World world, int x, int y, int z, boolean noWaterAdj, Season season) {
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        float temperature = biome.getFloatTemperature(x, y, z);

        if (!SeasonHelper.canSnowAtTempInSeason(season, temperature)) {
            return false;
        }
        if (biome == BiomeGenBase.river || biome == BiomeGenBase.ocean || biome == BiomeGenBase.deepOcean) {
            return false;
        }

        if (y >= 0 && y < 256 && world.getSavedLightValue(EnumSkyBlock.Block, x, y, z) < 10) {
            Block block = world.getBlock(x, y, z);
            int meta = world.getBlockMetadata(x, y, z);
            if ((block == Blocks.water || block == Blocks.flowing_water) && meta == 0) {
                if (!noWaterAdj) {
                    return true;
                }

                boolean allWaterAdj = world.getBlock(x - 1, y, z).getMaterial().isLiquid()
                                   && world.getBlock(x + 1, y, z).getMaterial().isLiquid()
                                   && world.getBlock(x, y, z - 1).getMaterial().isLiquid()
                                   && world.getBlock(x, y, z + 1).getMaterial().isLiquid();
                return !allWaterAdj;
            }
        }
        return false;
    }

    public static boolean isRainingAtInSeason(World world, int x, int y, int z, Season season) {
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

        // If the biome normally snows and it's NOT winter (per season rules), treat as not-rain.
        if (biome.getEnableSnow() && season != Season.WINTER) {
            return false;
        }

        // In 1.7.10, a decent proxy for “can it rain here right now?” is the biome’s
        // lightning capability (i.e., not a desert/hell biome) combined with your season gate.
        return biome.canSpawnLightningBolt();
    }

    public static float getFloatTemperature(BiomeGenBase biome, int x, int y, int z) {
        Season season = new SeasonTime(SeasonHandler.clientSeasonCycleTicks).getSubSeason().getSeason();
        if (biome.temperature <= 0.7F && season == Season.WINTER && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            return 0.0F;
        }
        return biome.getFloatTemperature(x, y, z);
    }

    public static void onUpdateTick(BlockCrops block, World world, int x, int y, int z) {
        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();
        if (season == Season.WINTER && block instanceof IDecayableCrop
            && !TemperatureHelper.isPosClimatisedForTemp(world, x, y, z, new Temperature(1))
            && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)) {
            world.setBlock(x, y, z, TANBlocks.dead_crops);
        }
    }
}
