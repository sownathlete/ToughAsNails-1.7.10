package toughasnails.init;

import java.lang.reflect.Constructor;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry;
import toughasnails.api.ITANBlock;
import toughasnails.api.TANBlocks;
import toughasnails.block.BlockGlowstoneTorch;
import toughasnails.block.BlockRainCollector;
import toughasnails.block.BlockSeasonSensor;
import toughasnails.block.BlockTANCampfire;
import toughasnails.block.BlockTANDeadCrops;
import toughasnails.block.BlockTANTemperatureCoil;
import toughasnails.block.BlockTANTorchNew;
import toughasnails.core.CommonProxy;
import toughasnails.core.ToughAsNails;
import toughasnails.tileentity.TileEntitySeasonSensor;
import toughasnails.tileentity.TileEntityTemperatureSpread;
import toughasnails.util.inventory.CreativeTabTAN;

public class ModBlocks {

    public static void init() {
        TANBlocks.season_sensors[0] = registerBlock(new BlockSeasonSensor(BlockSeasonSensor.DetectorType.SPRING), "season_sensor_spring");
        TANBlocks.season_sensors[1] = registerBlock(new BlockSeasonSensor(BlockSeasonSensor.DetectorType.SUMMER), "season_sensor_summer").setCreativeTab(null);
        TANBlocks.season_sensors[2] = registerBlock(new BlockSeasonSensor(BlockSeasonSensor.DetectorType.AUTUMN), "season_sensor_autumn").setCreativeTab(null);
        TANBlocks.season_sensors[3] = registerBlock(new BlockSeasonSensor(BlockSeasonSensor.DetectorType.WINTER), "season_sensor_winter").setCreativeTab(null);

        TANBlocks.campfire = registerBlock(new BlockTANCampfire(), "campfire");
        TANBlocks.rain_collector = registerBlock(new BlockRainCollector(), "rain_collector");
        TANBlocks.temperature_coil = registerBlock(new BlockTANTemperatureCoil(), "temperature_coil");
        TANBlocks.torch_new = registerBlock(new BlockTANTorchNew(), "torch_new");
        TANBlocks.glowstone_torch = registerBlock(new BlockGlowstoneTorch(), "glowstone_torch");
        TANBlocks.dead_crops = registerBlock(new BlockTANDeadCrops(), "dead_crops").setCreativeTab(null);

        GameRegistry.registerTileEntity(TileEntityTemperatureSpread.class, "temperature_spread");
        GameRegistry.registerTileEntity(TileEntitySeasonSensor.class, "season_sensor");
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
        try {
            if (itemClass != null) {
                Constructor<? extends ItemBlock> ctor = itemClass.getConstructor(Block.class);
                ItemBlock itemBlock = ctor.newInstance(block);
                GameRegistry.registerBlock(block, itemBlock.getClass(), name);
            } else {
                GameRegistry.registerBlock(block, name);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error registering block: " + name, e);
        }
    }
}
