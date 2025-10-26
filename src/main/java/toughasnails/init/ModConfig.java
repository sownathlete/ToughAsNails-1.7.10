/*
 * Decompiled with CFR 0.148.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.fml.common.eventhandler.EventBus
 */
package toughasnails.init;

import java.io.File;
import net.minecraftforge.common.MinecraftForge;
import toughasnails.config.GameplayConfigurationHandler;

public class ModConfig {
    public static void init(File configDirectory) {
        GameplayConfigurationHandler.init(new File(configDirectory, "gameplay.cfg"));
        MinecraftForge.EVENT_BUS.register((Object)new GameplayConfigurationHandler());
    }
}

