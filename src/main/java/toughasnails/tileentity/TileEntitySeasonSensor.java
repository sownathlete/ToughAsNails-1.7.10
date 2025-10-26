package toughasnails.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import toughasnails.api.season.SeasonHelper;
import toughasnails.block.BlockSeasonSensor;

/**
 * Season-sensor tile entity (Forge 1.7.10 back-port).
 *
 * <p>In 1.7.10 there is no {@code ITickable}; instead we override
 * {@code updateEntity()} which is called every tick.</p>
 */
public class TileEntitySeasonSensor extends TileEntity {

    /** Called once per tick by Minecraft (1.7.10). */
    @Override
    public void updateEntity() {

        if (worldObj == null || worldObj.isRemote) return;

        /* Run logic every 20 ticks (≈ 1 second) */
        if (SeasonHelper.getSeasonData(worldObj).getSeasonCycleTicks() % 20 != 0) return;

        Block block = getBlockType();
        if (block instanceof BlockSeasonSensor) {
            ((BlockSeasonSensor) block).updatePower(worldObj, xCoord, yCoord, zCoord);
        }
    }
}
