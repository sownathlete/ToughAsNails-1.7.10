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

    public AltitudeModifier(TemperatureDebugger debugger) { super(debugger); }

    @Override
    public int modifyChangeRate(World world, EntityPlayer player, int changeRate, TemperatureTrend trend) {
        return changeRate;
    }

    @Override
    public Temperature modifyTarget(World world, EntityPlayer player, Temperature temperature) {
        int out = temperature.getRawValue();
        debugger.start(TemperatureDebugger.Modifier.ALTITUDE_TARGET, out);

        if (world.provider.isSurfaceWorld()) {
            // dy > 0: above sea level (cooler). dy < 0: below sea level (warmer).
            double dy = player.posY - 64.0;
            int mag = MathHelper.floor_double((Math.abs(dy) / 64.0) * ALTITUDE_TARGET_MODIFIER);
            if (dy > 0) out -= mag;   // higher → colder
            else if (dy < 0) out += mag; // lower  → warmer
        }

        debugger.end(out);
        return new Temperature(out);
    }
}
