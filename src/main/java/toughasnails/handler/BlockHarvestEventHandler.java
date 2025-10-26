package toughasnails.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import toughasnails.api.item.TANItems;

import java.util.List;
import java.util.Random;

public class BlockHarvestEventHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.HarvestDropsEvent event) {
        // Skip if no harvester or silk touch is active
        if (event.harvester == null || event.isSilkTouching) {
            return;
        }

        Block block = event.block;

        if (block == Blocks.ice) {
            List<ItemStack> drops = event.drops;
            drops.clear();
            // 1–2 ice cubes
            drops.add(new ItemStack(TANItems.ice_cube, 1 + new Random().nextInt(2)));
        }
    }
}
