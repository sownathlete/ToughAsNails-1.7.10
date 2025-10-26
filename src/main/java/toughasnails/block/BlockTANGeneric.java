package toughasnails.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import toughasnails.api.ITANBlock;
import toughasnails.item.ItemTANBlock;

/**
 * Simple generic solid block for Tough As Nails.
 * Backported to Forge 1.7.10.
 */
public class BlockTANGeneric extends Block implements ITANBlock {

    public BlockTANGeneric() {
        this(Material.rock);
    }

    public BlockTANGeneric(Material material) {
        super(material);
        setHardness(1.0F);
        setStepSound(soundTypeStone);
    }

    // ---------- ITANBlock ----------

    @Override
    public Class<? extends ItemBlock> getItemClass() {
        return ItemTANBlock.class;
    }

    @Override
    public String[] getPresetProperties() {
        return new String[0];
    }

    @Override
    public String[] getNonRenderingProperties() {
        return null;
    }

    @Override
    public String getStateName(int meta) {
        return "";
    }
}
