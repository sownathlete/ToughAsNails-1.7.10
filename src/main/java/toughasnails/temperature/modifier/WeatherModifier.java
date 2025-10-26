package toughasnails.temperature.modifier;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

/**
 * Weather-driven temperature modifier (Forge 1.7.10 backport).
 * - Getting wet (rain/water) cools target temp and speeds movement toward it
 * - Snow exposure cools more
 * - Burning warms and speeds movement toward warm target
 * All rate math is clamped to avoid goofy/negative tick lengths.
 */
public class WeatherModifier extends TemperatureModifier {

    /* Target deltas (levels on TAN scale) */
    private static final int WET_TARGET_DELTA   = -6;  // rain / submerged
    private static final int SNOW_TARGET_DELTA  = -10; // snow exposure
    private static final int BURN_TARGET_DELTA  = +8;  // on fire

    /* Rate multipliers ( <1 = faster toward target ) */
    private static final float WET_RATE_MULT    = 0.60F; // 40% faster when wet
    private static final float SNOW_RATE_MULT   = 0.70F; // 30% faster in snow
    private static final float BURN_RATE_MULT   = 0.60F; // 40% faster when burning

    public WeatherModifier(TemperatureDebugger debugger) {
        super(debugger);
    }

    /* -------------------- Change-rate -------------------- */
    @Override
    public int modifyChangeRate(World world, EntityPlayer player,
                                int changeRate, TemperatureTrend trend) {

        int out = changeRate;
        float mult = 1.0F;

        // Getting wet / snow exposure should pull you faster toward the colder target
        if (isPlayerWet(world, player)) {
            mult *= WET_RATE_MULT;
        } else if (isSnowingOnPlayer(world, player)) {
            mult *= SNOW_RATE_MULT;
        }

        // Burning should pull you faster toward a hotter target
        if (player.isBurning()) {
            mult *= BURN_RATE_MULT;
        }

        int newRate = Math.max(20, Math.round(out * mult)); // never < 1s per step

        debugger.start(TemperatureDebugger.Modifier.WET_RATE, changeRate);
        debugger.end(newRate);
        return newRate;
    }

    /* -------------------- Target temperature -------------------- */
    @Override
    public Temperature modifyTarget(World world, EntityPlayer player,
                                    Temperature temperature) {

        int newLevel = temperature.getRawValue();

        if (player.isBurning()) {
            debugger.start(TemperatureDebugger.Modifier.WET_TARGET, newLevel);
            newLevel += BURN_TARGET_DELTA;
            debugger.end(newLevel);
        }

        if (isPlayerWet(world, player)) {
            debugger.start(TemperatureDebugger.Modifier.WET_TARGET, newLevel);
            newLevel += WET_TARGET_DELTA;
            debugger.end(newLevel);

        } else if (isSnowingOnPlayer(world, player)) {
            debugger.start(TemperatureDebugger.Modifier.SNOW_TARGET, newLevel);
            newLevel += SNOW_TARGET_DELTA;
            debugger.end(newLevel);
        }

        return new Temperature(newLevel);
    }

    /** True if the player is in water OR directly being rained on at head level. */
    private static boolean isPlayerWet(World world, EntityPlayer player) {
        if (player.isInWater() || player.isInsideOfMaterial(Material.water)) return true;

        int x = (int)Math.floor(player.posX);
        int y = (int)Math.floor(player.posY + player.getEyeHeight());
        int z = (int)Math.floor(player.posZ);

        // In 1.7.10, this is the closest to "raining on head"
        return world.isRaining() && world.canLightningStrikeAt(x, y, z);
    }

    /** True when it is snowing on the player (raining + snowy biome + sky access). */
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
