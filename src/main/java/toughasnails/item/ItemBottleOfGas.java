package toughasnails.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toughasnails.api.TANBlocks;

public class ItemBottleOfGas extends Item {

    private final Block gasBlock;

    public ItemBottleOfGas() {
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.gasBlock = TANBlocks.gas;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < BottleContents.values().length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    public BottleContents getContentsType(ItemStack stack) {
        int meta = MathHelper.clamp_int(stack.getItemDamage(), 0, BottleContents.values().length - 1);
        return BottleContents.values()[meta];
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item.bottle_of_" + getContentsType(stack).getName();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

        if (stack.stackSize == 0) return false;

        Block block = world.getBlock(x, y, z);

        // Adjust for snow layers
        if (block == Blocks.snow_layer && world.getBlockMetadata(x, y, z) < 1) {
            side = 1; // UP
        } else if (!block.isReplaceable(world, x, y, z)) {
            switch (side) {
                case 0: --y; break;
                case 1: ++y; break;
                case 2: --z; break;
                case 3: ++z; break;
                case 4: --x; break;
                case 5: ++x; break;
            }
        }

        if (!player.canPlayerEdit(x, y, z, side, stack)) return false;
        if (!world.canPlaceEntityOnSide(this.gasBlock, x, y, z, false, side, player, stack)) return false;

        // FIX: use item damage, not getMetadata(ItemStack)
        int meta = stack.getItemDamage();
        int placedMeta = this.gasBlock.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, meta);

        if (world.setBlock(x, y, z, this.gasBlock, placedMeta, 3)) {
            this.gasBlock.onBlockPlacedBy(world, x, y, z, player, stack);
            this.gasBlock.onPostBlockPlaced(world, x, y, z, meta);

            // Replace the filled bottle with an empty glass bottle
            if (!player.capabilities.isCreativeMode) {
                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    // put empty bottle in hand
                    player.setCurrentItemOrArmor(0, new ItemStack(Items.glass_bottle));
                } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle))) {
                    player.dropPlayerItemWithRandomChoice(new ItemStack(Items.glass_bottle), false);
                }
            }
            return true;
        }

        return false;
    }

    public static enum BottleContents {
        BLACKDAMP,
        WHITEDAMP,
        FIREDAMP,
        STINKDAMP;

        public String getName() {
            return this.name().toLowerCase();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
