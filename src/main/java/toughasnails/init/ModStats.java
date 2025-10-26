package toughasnails.init;

import toughasnails.api.TANCapabilities;
import toughasnails.api.stat.PlayerStatRegistry;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.stat.capability.IThirst;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.thirst.ThirstHandler;

public class ModStats {

    public static void init() {
        // Register player stat handlers and their capabilities
        PlayerStatRegistry.addStat(ITemperature.class, TANCapabilities.TEMPERATURE, TemperatureHandler.class);
        PlayerStatRegistry.addStat(IThirst.class, TANCapabilities.THIRST, ThirstHandler.class);

        // Register capabilities globally (optional but consistent with API)
        PlayerStatRegistry.registerCapability(TANCapabilities.TEMPERATURE);
        PlayerStatRegistry.registerCapability(TANCapabilities.THIRST);
    }
}
