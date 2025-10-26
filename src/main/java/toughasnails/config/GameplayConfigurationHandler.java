package toughasnails.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.core.ToughAsNails;

/**
 * Handles loading and syncing of the gameplay.cfg configuration file.
 * Backported for Forge 1.7.10 (no FML package).
 */
public class GameplayConfigurationHandler {

    public static final String SURVIVAL_SETTINGS = "Survival Settings";
    public static Configuration config;

    /** Called from preInit */
    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {
        try {
            addSyncedBool(GameplayOption.ENABLE_LOWERED_STARTING_HEALTH, true, SURVIVAL_SETTINGS,
                    "Players begin with a lowered maximum health.");
            addSyncedBool(GameplayOption.ENABLE_SEASONS, true, SURVIVAL_SETTINGS,
                    "Seasons progress as days increase.");
            addSyncedBool(GameplayOption.ENABLE_TEMPERATURE, true, SURVIVAL_SETTINGS,
                    "Players are affected by temperature.");
            addSyncedBool(GameplayOption.ENABLE_THIRST, true, SURVIVAL_SETTINGS,
                    "Players are affected by thirst.");
        }
        catch (Exception e) {
            ToughAsNails.logger.error("Tough As Nails encountered a problem loading gameplay.cfg", e);
        }
        finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    private static void addSyncedBool(GameplayOption option, boolean defaultValue, String category, String comment) {
        boolean value = config.get(category, option.getOptionName(), defaultValue, comment).getBoolean(defaultValue);
        SyncedConfig.addOption(option, Boolean.toString(value));
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase("ToughAsNails")) {
            loadConfiguration();
        }
    }
}
