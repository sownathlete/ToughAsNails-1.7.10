package toughasnails.config;

import net.minecraft.block.Block;

/**
 * Simple container representing a block and its metadata.
 * Backport of the 1.8+ ReadBlockState structure for Forge 1.7.10.
 */
final class ReadBlockState {

    public final Block block;
    public final int meta;
    public final String[] usedProperties;

    public ReadBlockState(Block block, int meta, String[] usedProperties) {
        this.block = block;
        this.meta = meta;
        this.usedProperties = usedProperties;
    }
}
