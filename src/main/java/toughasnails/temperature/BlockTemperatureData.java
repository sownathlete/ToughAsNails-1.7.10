package toughasnails.temperature;

import net.minecraft.block.Block;

/**
 * Represents the temperature contribution of a specific block and metadata.
 * Used by TANConfig and temperature modifiers.
 */
public class BlockTemperatureData {

    /** The block this data applies to */
    public Block block;

    /** The metadata value for the block (0–15). Use 0 if not relevant */
    public int meta;

    /** The temperature value contributed by this block */
    public float blockTemperature;

    public BlockTemperatureData(Block block, int meta, float blockTemperature) {
        this.block = block;
        this.meta = meta;
        this.blockTemperature = blockTemperature;
    }

    /** Simple match check for block and meta */
    public boolean matches(Block otherBlock, int otherMeta) {
        return this.block == otherBlock && this.meta == otherMeta;
    }

    @Override
    public String toString() {
        String name = Block.blockRegistry.getNameForObject(block);
        return "BlockTemperatureData[" + name + ":" + meta + ", temp=" + blockTemperature + "]";
    }
}
