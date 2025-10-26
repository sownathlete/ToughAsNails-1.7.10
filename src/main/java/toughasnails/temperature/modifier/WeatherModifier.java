// File: toughasnails/temperature/modifier/WeatherModifier.java
package toughasnails.temperature.modifier;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

public class WeatherModifier extends TemperatureModifier {

    private static final int   WET_TARGET_DELTA  = -6;
    private static final int   SNOW_TARGET_DELTA = -10;
    private static final int   BURN_TARGET_DELTA = +8;

    private static final float WET_RATE_MULT   = 0.60F;
    private static final float SNOW_RATE_MULT  = 0.70F;
    private static final float BURN_RATE_MULT  = 0.60F;
    private static final float LAVA_RATE_MULT  = 0.35F;

    public WeatherModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        int out = changeRate;
        float mult = 1.0F;

        if (player.isInsideOfMaterial(Material.lava)) mult *= LAVA_RATE_MULT;
        if (isPlayerWet(world, player))               mult *= WET_RATE_MULT;
        else if (isSnowingOnPlayer(world, player))    mult *= SNOW_RATE_MULT;
        if (player.isBurning())                       mult *= BURN_RATE_MULT;

        int newRate = Math.max(20, Math.round(out * mult));
        debugger.start(TemperatureDebugger.Modifier.WET_RATE, changeRate);
        debugger.end(newRate);
        return newRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int level = temperature.getRawValue();

        // Hard override: lava = absolute max
        if (player.isInsideOfMaterial(Material.lava)) {
            int max = TemperatureScale.getScaleTotal();
            debugger.start(TemperatureDebugger.Modifier.WET_TARGET, level);
            debugger.end(max);
            return new Temperature(max);
        }

        if (player.isBurning()) {
            debugger.start(TemperatureDebugger.Modifier.WET_TARGET, level);
            level += BURN_TARGET_DELTA;
            debugger.end(level);
        }

        if (isPlayerWet(world, player)) {
            debugger.start(TemperatureDebugger.Modifier.WET_TARGET, level);
            level += WET_TARGET_DELTA;
            debugger.end(level);
        } else if (isSnowingOnPlayer(world, player)) {
            debugger.start(TemperatureDebugger.Modifier.SNOW_TARGET, level);
            level += SNOW_TARGET_DELTA;
            debugger.end(level);
        }

        return new Temperature(level);
    }

    private static boolean isPlayerWet(World world, EntityPlayer player) {
        if (player.isInWater() || player.isInsideOfMaterial(Material.water)) return true;
        int x = (int)Math.floor(player.posX);
        int y = (int)Math.floor(player.posY + player.getEyeHeight());
        int z = (int)Math.floor(player.posZ);
        return world.isRaining() && world.canLightningStrikeAt(x, y, z);
    }

    private static boolean isSnowingOnPlayer(World world, EntityPlayer player) {
        if (!world.isRaining()) return false;
        int x = (int)Math.floor(player.posX);
        int y = (int)Math.floor(player.posY);
        int z = (int)Math.floor(player.posZ);
        if (!world.canBlockSeeTheSky(x, y, z)) return false;
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        return biome != null && biome.getEnableSnow();
    }
}
