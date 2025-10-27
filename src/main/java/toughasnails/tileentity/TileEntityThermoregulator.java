package toughasnails.tileentity;

import net.minecraft.block.Block;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import toughasnails.api.item.TANItems;

import java.util.HashMap;
import java.util.Map;

public class TileEntityThermoregulator extends TileEntity implements ISidedInventory {

    private ItemStack[] inv = new ItemStack[2]; // 0=cool, 1=heat
    private int coolingTicks;
    private int heatingTicks;
    private boolean redstoneDisabledOverride = false;

    private static final Map<Item, Integer> COOL_FUELS = new HashMap<Item, Integer>();
    private static final Map<Item, Integer> HEAT_FUELS = new HashMap<Item, Integer>();

    static {
        // Cooling fuels
        addCool(Item.getItemFromBlock(net.minecraft.init.Blocks.ice),            200);
        addCool(Item.getItemFromBlock(net.minecraft.init.Blocks.packed_ice),     400);
        addCool(net.minecraft.init.Items.snowball,                                 50);
        addCool(Item.getItemFromBlock(net.minecraft.init.Blocks.snow),           100);
        addCool(Item.getItemFromBlock(net.minecraft.init.Blocks.snow_layer),      40);

        // TAN cooling items
        if (TANItems.ice_cream != null) addCool(TANItems.ice_cream, 300);

        // Heating fuels
        addHeat(net.minecraft.init.Items.blaze_rod,       800);
        addHeat(net.minecraft.init.Items.blaze_powder,    400);
        addHeat(net.minecraft.init.Items.fire_charge,     300);
        addHeat(net.minecraft.init.Items.coal,            400); // charcoal handled by meta
        addHeat(Item.getItemFromBlock(net.minecraft.init.Blocks.coal_block), 3600);
        addHeat(net.minecraft.init.Items.lava_bucket,     2000);
        addHeat(net.minecraft.init.Items.magma_cream,     600);

        // TAN heating items
        if (TANItems.charc_os != null) addHeat(TANItems.charc_os, 500);
    }

    private static void addCool(Block b, int t){ COOL_FUELS.put(Item.getItemFromBlock(b), t); }
    private static void addCool(Item i, int t){ if(i!=null) COOL_FUELS.put(i, t); }
    private static void addHeat(Block b, int t){ HEAT_FUELS.put(Item.getItemFromBlock(b), t); }
    private static void addHeat(Item i, int t){ if(i!=null) HEAT_FUELS.put(i, t); }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        boolean powered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        if (powered || redstoneDisabledOverride) return;

        if (!isFrontClear(worldObj, xCoord, yCoord, zCoord) || !hasCeiling(worldObj, xCoord, yCoord, zCoord)) return;

        if (coolingTicks <= 0) tryConsumeFuel(0, true);
        if (heatingTicks <= 0) tryConsumeFuel(1, false);

        if (coolingTicks > 0) coolingTicks--;
        if (heatingTicks > 0) heatingTicks--;

        if (coolingTicks > 0 && heatingTicks > 0) {
            affectAreaNeutral();
        } else if (coolingTicks > 0) {
            affectAreaCool();
        } else if (heatingTicks > 0) {
            affectAreaHeat();
        }
    }

    private boolean isFrontClear(World w, int x, int y, int z) {
        int fx = x, fy = y, fz = z + 1; // TODO: add facing later
        Block b = w.getBlock(fx, fy, fz);
        return !b.getMaterial().isSolid();
    }

    private boolean hasCeiling(World w, int x, int y, int z) {
        for (int dy = 1; dy <= 8; dy++) {
            Block b = w.getBlock(x, y + dy, z);
            if (b != null && b.getMaterial().isSolid()) return true;
        }
        return false;
    }

    private AxisAlignedBB affectedBox() {
        return AxisAlignedBB.getBoundingBox(xCoord - 4, yCoord - 2, zCoord + 1, xCoord + 5, yCoord + 3, zCoord + 10);
    }

    private void affectAreaCool() { /* TODO: hook into TAN temp system */ }
    private void affectAreaHeat() { /* TODO: hook into TAN temp system */ }
    private void affectAreaNeutral() { /* TODO: hook into TAN temp system */ }

    private void tryConsumeFuel(int slot, boolean cooling) {
        ItemStack s = inv[slot];
        if (s == null) return;

        if (cooling) {
            Integer t = COOL_FUELS.get(s.getItem());
            if (t != null) {
                coolingTicks += t;
                decrSlot(slot);
            }
        } else {
            Integer t = HEAT_FUELS.get(s.getItem());
            if (t == null && s.getItem() == net.minecraft.init.Items.coal && s.getItemDamage() == 1) {
                t = 400; // charcoal
            }
            if (t != null) {
                heatingTicks += t;
                if (s.getItem() == net.minecraft.init.Items.lava_bucket) {
                    inv[slot] = new ItemStack(net.minecraft.init.Items.bucket);
                } else {
                    decrSlot(slot);
                }
            }
        }
        markDirty();
    }

    private void decrSlot(int slot) {
        if (inv[slot] == null) return;
        inv[slot].stackSize--;
        if (inv[slot].stackSize <= 0) inv[slot] = null;
    }

    public boolean toggleRedstoneOverride() {
        redstoneDisabledOverride = !redstoneDisabledOverride;
        return redstoneDisabledOverride;
    }

    public int getCoolingTicksRemaining(){ return coolingTicks; }
    public int getHeatingTicksRemaining(){ return heatingTicks; }
    public String getModeString() {
        if (coolingTicks > 0 && heatingTicks > 0) return "Neutralize";
        if (coolingTicks > 0) return "Cooling";
        if (heatingTicks > 0) return "Heating";
        return "Idle";
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("coolTicks", coolingTicks);
        nbt.setInteger("heatTicks", heatingTicks);
        nbt.setBoolean("override", redstoneDisabledOverride);
        writeInv(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        coolingTicks = nbt.getInteger("coolTicks");
        heatingTicks = nbt.getInteger("heatTicks");
        redstoneDisabledOverride = nbt.getBoolean("override");
        readInv(nbt);
    }

    private void writeInv(NBTTagCompound nbt) {
        for (int i = 0; i < 2; i++) {
            if (inv[i] != null) {
                NBTTagCompound t = new NBTTagCompound();
                t.setByte("Slot", (byte) i);
                inv[i].writeToNBT(t);
                nbt.setTag("Item" + i, t);
            }
        }
    }

    private void readInv(NBTTagCompound nbt) {
        for (int i = 0; i < 2; i++) {
            if (nbt.hasKey("Item" + i)) {
                NBTTagCompound t = nbt.getCompoundTag("Item" + i);
                inv[i] = ItemStack.loadItemStackFromNBT(t);
            }
        }
    }

    // === Inventory ===
    @Override public int getSizeInventory() { return 2; }
    @Override public ItemStack getStackInSlot(int slot) { return inv[slot]; }
    @Override public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
            ItemStack ret;
            if (stack.stackSize <= amount) {
                ret = stack;
                setInventorySlotContents(slot, null);
                return ret;
            } else {
                ret = stack.splitStack(amount);
                if (stack.stackSize <= 0) setInventorySlotContents(slot, null);
                return ret;
            }
        }
        return null;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack s = inv[slot];
        if (s != null) inv[slot] = null;
        return s;
    }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        inv[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        markDirty();
    }
    @Override public String getInventoryName() { return "Thermoregulator"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null) return false;
        if (slot == 0) return COOL_FUELS.containsKey(stack.getItem());
        if (slot == 1) {
            if (stack.getItem() == net.minecraft.init.Items.coal) return true; // charcoal too
            return HEAT_FUELS.containsKey(stack.getItem());
        }
        return false;
    }
    @Override public int[] getAccessibleSlotsFromSide(int side) { return new int[]{0,1}; }
    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) { return isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) { return false; }
}
