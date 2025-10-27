// File: toughasnails/tileentity/TileEntityTemperatureGauge.java
// (reposted here unchanged from earlier so you have the full set together)
package toughasnails.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureAtPos;

public class TileEntityTemperatureGauge extends TileEntity {

    private int redstoneLevel = 0; // 0 or 15
    private static final float MID_DEADBAND_FRAC = 0.08f;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;

        Temperature t = TemperatureAtPos.compute(worldObj, xCoord, yCoord + 1, zCoord);
        int raw = t.getRawValue();

        final int total    = TemperatureScale.getScaleTotal();
        final int midpoint = total / 2;
        final int band     = Math.max(1, (int)(total * MID_DEADBAND_FRAC));

        final int warmOn = midpoint + band;
        final int coldOn = midpoint - band;

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean warmMode = (meta & 1) != 0;

        boolean on = warmMode ? (raw > warmOn) : (raw < coldOn);
        setRedstoneLevel(on ? 15 : 0);
    }

    public int getRedstoneLevelClient() { return redstoneLevel; }

    private void setRedstoneLevel(int lvl) {
        int clamped = (lvl <= 0 ? 0 : 15);
        if (clamped != redstoneLevel) {
            redstoneLevel = clamped;
            if (worldObj != null) {
                worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
            markDirty();
        }
    }

    public void onModeChanged() {
        setRedstoneLevel(0);
        markDirty();
    }

    public void sync() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
        if (worldObj != null) {
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("RS", redstoneLevel);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneLevel = tag.getInteger("RS");
    }
}
