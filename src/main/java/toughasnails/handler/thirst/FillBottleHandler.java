package toughasnails.handler.thirst;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.item.TANItems;

/**
 * Backported FillBottleHandler for Forge 1.7.10.
 * Handles filling bottles with water or from cauldrons using TAN's thirst system.
 */
public class FillBottleHandler {

    /** Handles right-click on water while holding a glass bottle. */
    @SubscribeEvent
    public void onPlayerRightClickWater(PlayerInteractEvent event) {
        // Only handle right-click on a block
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK &&
            event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
            return;

        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;

        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null || stack.getItem() != Items.glass_bottle)
            return;

        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST))
            return;

        // Check if aimed at water
        int x = event.x;
        int y = event.y;
        int z = event.z;
        Block targetBlock = world.getBlock(x, y, z);

        boolean touchingWater =
                targetBlock == net.minecraft.init.Blocks.water ||
                targetBlock == net.minecraft.init.Blocks.flowing_water;

        if (!touchingWater)
            return;

        if (!world.isRemote) {
            stack.stackSize--;
            player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(stack.getItem())]);

            ItemStack bottleStack = new ItemStack(TANItems.water_bottle);

            if (!player.inventory.addItemStackToInventory(bottleStack)) {
                world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, bottleStack));
            }

            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
        }
        event.setCanceled(true);
    }

    /** Handles filling a bottle from a cauldron. */
    @SubscribeEvent
    public void onRightClickCauldron(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            return;

        EntityPlayer player = event.entityPlayer;
        World world = player.worldObj;
        int x = event.x;
        int y = event.y;
        int z = event.z;

        ItemStack heldStack = player.getCurrentEquippedItem();
        if (heldStack == null)
            return;

        Item heldItem = heldStack.getItem();
        Block block = world.getBlock(x, y, z);

        if (!(block instanceof BlockCauldron))
            return;

        if (heldItem != Items.glass_bottle)
            return;

        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST))
            return;

        BlockCauldron cauldron = (BlockCauldron) block;
        int level = world.getBlockMetadata(x, y, z);

        if (level > 0 && !world.isRemote) {
            if (!player.capabilities.isCreativeMode) {
                ItemStack waterBottle = new ItemStack(TANItems.water_bottle);
                player.triggerAchievement(StatList.field_151186_x); // Use Cauldron stat

                heldStack.stackSize--;
                if (heldStack.stackSize <= 0) {
                    player.setCurrentItemOrArmor(0, waterBottle);
                } else if (!player.inventory.addItemStackToInventory(waterBottle)) {
                    world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, waterBottle));
                } else if (player instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                }
            }

            // Reduce cauldron water level
            int newLevel = Math.max(0, level - 1);
            world.setBlockMetadataWithNotify(x, y, z, newLevel, 2);

            event.setCanceled(true);
        }
    }
}
