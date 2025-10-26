package toughasnails.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import toughasnails.api.ITANBlock;

/**
 * Forge 1.7.10 backport of ItemTANBlock.
 * - Uses ItemBlock.field_150939_a (block reference).
 * - Delegates sub-item listing to Block#getSubBlocks (1.7.10 pattern).
 * - Builds unlocalized name via ITANBlock#getStateName(int) if available.
 */
public class ItemTANBlock extends ItemBlock {

    private final ITANBlock tanBlock; // the block implements this interface

    public ItemTANBlock(Block block) {
        super(block);
        if (!(block instanceof ITANBlock)) {
            throw new IllegalArgumentException("ItemTANBlock must be created with a block implementing ITANBlock");
        }
        this.tanBlock = (ITANBlock) block;
        this.setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        // Let the block decide which variants to expose in creative (metadata variants)
        this.field_150939_a.getSubBlocks(item, tab, list);
        // Fallback if the block didn't add any (rare)
        if (list.isEmpty()) {
            list.add(new ItemStack(item, 1, 0));
        }
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        // Prefer ITANBlock's variant naming if provided
        try {
            String variant = tanBlock.getStateName(stack.getItemDamage());
            if (variant != null && !variant.isEmpty()) {
                return super.getUnlocalizedName() + "." + variant;
            }
        } catch (Throwable ignored) {
            // If ITANBlock doesn't have getStateName(int) or throws, fall through
        }
        // Safe fallback: append meta
        return super.getUnlocalizedName() + "." + stack.getItemDamage();
    }
}
