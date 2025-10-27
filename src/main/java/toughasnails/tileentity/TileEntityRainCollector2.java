package toughasnails.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityRainCollector2 extends TileEntity {

    public static final int CAPACITY = 4000;      // 4 buckets
    public static final int TICK_FILL_RAIN = 2;   // mB/tick when raining
    public static final int TICK_FILL_SNOW = 1;   // mB/tick snowy biomes
    public static final int IDLE_TRICKLE = 0;
    public static final int BOTTLE_COST = 250;

    // Simple, central place to define your TAN water bottle metas:
    public static final int META_DIRTY     = 0;
    public static final int META_REGULAR   = 1;
    public static final int META_PURIFIED  = 2;

    // Simple canteen capacity units (1 unit = 1000 mB)
    public static final int CANTEEN_MAX = 3;

    private int amount; // mB

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        int add = IDLE_TRICKLE;
        if (worldObj.isRaining()) add = TICK_FILL_RAIN;
        if (worldObj.isRaining() && worldObj.getBiomeGenForCoords(xCoord, zCoord).getEnableSnow()) {
            add = TICK_FILL_SNOW;
        }
        if (add > 0) {
            amount = Math.min(CAPACITY, amount + add);
            markDirty();
        }
    }

    public int getAmount(){ return amount; }
    public void drain(int mB){ amount = Math.max(0, amount - mB); markDirty(); }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("amt", amount);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        amount = nbt.getInteger("amt");
    }
}
