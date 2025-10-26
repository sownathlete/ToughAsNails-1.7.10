package toughasnails.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

public class ItemIceCharge extends Item {

    public ItemIceCharge() {
        this.setHasSubtypes(true);
        this.setMaxStackSize(16);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);
        if (mop == null) return stack;

        if (mop.typeOfHit == MovingObjectType.BLOCK) {
            int x = mop.blockX;
            int y = mop.blockY;
            int z = mop.blockZ;

            if (!world.canMineBlock(player, x, y, z)) return stack;
            if (!player.canPlayerEdit(x, y, z, mop.sideHit, stack)) return stack;

            Block block = world.getBlock(x, y, z);
            Material mat = block.getMaterial();

            // Only freeze source water (meta == 0)
            if (mat == Material.water && world.getBlockMetadata(x, y, z) == 0) {
                // Take a snapshot so we can restore if the event is canceled
                BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, x, y, z);
                world.setBlock(x, y, z, Blocks.ice, 0, 3);

                // Correct 1.7.10 hook signature
                if (ForgeEventFactory.onPlayerBlockPlace(player, snapshot, ForgeDirection.getOrientation(mop.sideHit)).isCanceled()) {
                    snapshot.restore(true, false);
                    return stack;
                }

                if (!player.capabilities.isCreativeMode) {
                    stack.stackSize--;
                }

                // Use the correct stat index in 1.7.10
                player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);

                world.playSoundEffect(
                    x + 0.5D, y + 0.5D, z + 0.5D,
                    "random.fizz", 0.5F, 0.8F + world.rand.nextFloat() * 0.4F
                );
            }
        }

        return stack;
    }
}
