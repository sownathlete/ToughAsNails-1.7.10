// File: toughasnails/temperature/modifier/AltitudeModifier.java
package toughasnails.temperature.modifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toughasnails.api.temperature.Temperature;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureTrend;

public class AltitudeModifier extends TemperatureModifier {
    public static final int ALTITUDE_TARGET_MODIFIER = 3; // per 64 blocks

    public AltitudeModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) { return changeRate; }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int out = temperature.getRawValue();
        double dy = player.posY - 64.0;
        int mag = MathHelper.floor_double((Math.abs(dy) / 64.0) * ALTITUDE_TARGET_MODIFIER);
        if (dy > 0) out -= mag; else if (dy < 0) out += mag;

        debugger.start(TemperatureDebugger.Modifier.ALTITUDE_TARGET, temperature.getRawValue());
        debugger.end(out);
        return new Temperature(out);
    }
}
