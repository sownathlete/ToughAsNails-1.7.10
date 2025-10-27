package toughasnails.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

// TAN API fields you populate elsewhere
import toughasnails.api.TANBlocks;

// New blocks
import toughasnails.block.BlockRainCollector2;
import toughasnails.block.BlockTemperatureGauge;
import toughasnails.block.BlockThermoregulator;
import toughasnails.block.BlockWaterPurifier;

// New tile entities
import toughasnails.tileentity.TileEntityRainCollector2;
import toughasnails.tileentity.TileEntityTemperatureGauge;
import toughasnails.tileentity.TileEntityThermoregulator;
import toughasnails.tileentity.TileEntityWaterPurifier;

// Your creative tab
import toughasnails.util.inventory.CreativeTabTAN;

public class ModBlocks {

    public static void init() {
        // === NEW BLOCKS ===
        TANBlocks.thermoregulator   = registerBlock(new BlockThermoregulator(), "thermoregulator");
        TANBlocks.temperature_gauge = registerBlock(new BlockTemperatureGauge(), "temperature_gauge");
        TANBlocks.rain_collector2   = registerBlock(new BlockRainCollector2(), "rain_collector"); // keeps old name/id
        TANBlocks.water_purifier    = registerBlock(new BlockWaterPurifier(), "water_purifier");

        // === TILE ENTITIES ===
        GameRegistry.registerTileEntity(TileEntityThermoregulator.class, "tan_thermoregulator");
        GameRegistry.registerTileEntity(TileEntityTemperatureGauge.class, "tan_temperature_gauge");
        GameRegistry.registerTileEntity(TileEntityRainCollector2.class, "tan_rain_collector");
        GameRegistry.registerTileEntity(TileEntityWaterPurifier.class, "tan_water_purifier");

        // If you still need legacy TEs elsewhere, register them where appropriate.
        // GameRegistry.registerTileEntity(TileEntityTemperatureSpread.class, "temperature_spread");
        // GameRegistry.registerTileEntity(TileEntitySeasonSensor.class, "season_sensor");
    }

    public static Block registerBlock(Block block, String name) {
        return registerBlock(block, name, CreativeTabTAN.instance);
    }

    public static Block registerBlock(Block block, String name, CreativeTabs tab) {
        block.setBlockName("toughasnails." + name);
        block.setBlockTextureName("toughasnails:" + name);
        block.setCreativeTab(tab);
        registerBlockWithItem(block, name, ItemBlock.class);
        return block;
    }

    private static void registerBlockWithItem(Block block, String name, Class<? extends ItemBlock> itemClass) {
        if (itemClass != null) {
            // 1.7.10 overload: pass the ItemBlock class directly
            GameRegistry.registerBlock(block, itemClass, name);
        } else {
            GameRegistry.registerBlock(block, name);
        }
    }
}
