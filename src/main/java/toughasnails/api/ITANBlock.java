package toughasnails.api;

import net.minecraft.item.ItemBlock;

/**
 * Interface for TAN blocks in 1.7.10.
 * Simplified since 1.7.10 does not use IBlockState or IProperty.
 */
public interface ITANBlock {

    /**
     * @return the ItemBlock class used for this block
     */
    Class<? extends ItemBlock> getItemClass();

    /**
     * @return an array of property names (for example, metadata variants)
     */
    String[] getPresetProperties();

    /**
     * @return an array of property names that are not used for rendering
     */
    String[] getNonRenderingProperties();

    /**
     * Gets the state name for a given metadata value.
     * @param meta metadata value of the block
     * @return the name of the block state
     */
    String getStateName(int meta);
}
