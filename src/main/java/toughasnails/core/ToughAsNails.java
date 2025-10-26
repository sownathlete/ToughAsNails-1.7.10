package toughasnails.core;

import java.io.File;

import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.core.CommonProxy;
import toughasnails.core.ClientProxy;
import toughasnails.command.TANCommand;
import toughasnails.config.TANConfig;
import toughasnails.handler.BlockHarvestEventHandler;
import toughasnails.handler.LootTableEventHandler;
import toughasnails.handler.StatTickHandler;
import toughasnails.init.ModAchievements;
import toughasnails.init.ModBlocks;
import toughasnails.init.ModConfig;
import toughasnails.init.ModCrafting;
import toughasnails.init.ModEntities;
import toughasnails.init.ModHandlers;
import toughasnails.init.ModItems;
import toughasnails.init.ModPotions;
import toughasnails.init.ModStats;
import toughasnails.init.ModVanillaCompat;
import toughasnails.thirst.ThirstHandler;

@Mod(modid = ToughAsNails.MOD_ID,
     name = ToughAsNails.MOD_NAME,
     version = ToughAsNails.MOD_VERSION,
     guiFactory = ToughAsNails.GUI_FACTORY)
public class ToughAsNails {

    public static final String MOD_NAME    = "Tough As Nails";
    public static final String MOD_ID      = "ToughAsNails";
    public static final String MOD_VERSION = "1.1.1.66";
    public static final String GUI_FACTORY = "toughasnails.client.gui.GuiFactory";

    @Instance(ToughAsNails.MOD_ID)
    public static ToughAsNails instance;

    @SidedProxy(clientSide = "toughasnails.core.ClientProxy",
                serverSide = "toughasnails.core.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger = LogManager.getLogger(MOD_ID);
    public static File   configDirectory;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDirectory = new File(event.getModConfigurationDirectory(), "toughasnails");
        ModConfig.init(configDirectory);
        ModBlocks.init();
        ModEntities.init();
        ModItems.init();
        ModStats.init();
        ModPotions.init();
        ModVanillaCompat.init();
        ModHandlers.init();
        ModCrafting.init();
        ModAchievements.init();

        // Register event handlers living on the Forge bus
        MinecraftForge.EVENT_BUS.register(new LootTableEventHandler());
        MinecraftForge.EVENT_BUS.register(new BlockHarvestEventHandler());
        MinecraftForge.EVENT_BUS.register(new StatTickHandler());

        // >>> IMPORTANT: hook our per-player tick handlers (FML bus) <<<
        TemperatureHelper.bootstrap();
        ThirstHandler.bootstrap();
        logger.info("[TAN] Bootstrapped Temperature & Thirst tick handlers.");

        proxy.registerRenderers();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        TANConfig.init(configDirectory);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new TANCommand());
    }
}
