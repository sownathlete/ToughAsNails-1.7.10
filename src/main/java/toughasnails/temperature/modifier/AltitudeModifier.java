package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;
import toughasnails.temperature.modifier.TemperatureModifier;

public class AltitudeModifier extends TemperatureModifier {
    public static final int ALTITUDE_TARGET_MODIFIER = 3;

    public AltitudeModifier(TemperatureDebugger debugger) {
        super(debugger);
    }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        // 1:1 backport, no change
        return changeRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int temperatureLevel = temperature.getRawValue();
        int newTemperatureLevel = temperatureLevel;

        // Start debugging measurement
        this.debugger.start(TemperatureDebugger.Modifier.ALTITUDE_TARGET, newTemperatureLevel);

        if (world.provider.isSurfaceWorld()) {
            // Altitude effect: colder at higher elevation, scaled by ALTITUDE_TARGET_MODIFIER
            int altitudeEffect = MathHelper.abs_int(
                    MathHelper.floor_double((64.0 - player.posY) / 64.0 * ALTITUDE_TARGET_MODIFIER) + 1
            );
            newTemperatureLevel -= altitudeEffect;
        }

        // End debugging measurement
        this.debugger.end(newTemperatureLevel);

        return new Temperature(newTemperatureLevel);
    }
}
