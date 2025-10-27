package toughasnails.tileentity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import toughasnails.api.item.TANItems;

/**
 * Slots:
 *  0 = top input  (dirty/regular -> next stage)
 *  1 = bottom input (filter item; consumed)
 *  2 = output
 */
public class TileEntityWaterPurifier extends TileEntity implements ISidedInventory {

    private static final int[] SLOTS_ALL = new int[]{0,1,2};
    private static final int[] SLOTS_INPUT = new int[]{0,1};
    private static final int[] SLOTS_OUTPUT = new int[]{2};

    private ItemStack[] inv = new ItemStack[3];

    private int processTime;      // counts up
    private int processTimeGoal;  // ticks from filter table

    private static final Map<ItemKey, Integer> FILTER_TIMES = new HashMap<ItemKey, Integer>();

    // Define the TAN water_bottle metas here so you can tweak centrally
    public static final int META_DIRTY     = 0;
    public static final int META_REGULAR   = 1;
    public static final int META_PURIFIED  = 2;

    static {
        // Base equivalents for 1.7.10
        putFilter(new ItemKey(Items.wheat_seeds),       100);  // short grass proxy (5s)
        putFilter(new ItemKey(Items.paper),             200);  // 10s
        putFilter(new ItemKey(Item.getItemFromBlock(Blocks.gravel)), 400); // 20s
        putFilter(new ItemKey(Item.getItemFromBlock(Blocks.sand)),   800); // 40s
        putFilter(new ItemKey(Items.coal, 1),           1600); // 80s (charcoal)
        // Proper TAN charcoal filter if present (80s or more—let’s reward it)
        if (TANItems.charcoal_filter != null) {
            putFilter(new ItemKey(TANItems.charcoal_filter), 1600);
        }
    }

    private static void putFilter(ItemKey key, int ticks){ FILTER_TIMES.put(key, ticks); }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        if (canProcess()) {
            if (processTimeGoal <= 0) processTimeGoal = nextProcessGoal();
            if (processTimeGoal <= 0) return; // safety

            processTime++;
            if (processTime >= processTimeGoal) {
                doProcessOnce();
                processTime = 0;
                processTimeGoal = 0;
            }
            markDirty();
        } else {
            processTime = 0;
        }
    }

    private boolean canProcess() {
        if (inv[0] == null) return false;
        if (!isFilterPresent()) return false;

        ItemStack out = getProcessResult(inv[0]);
        if (out == null) return false;

        if (inv[2] == null) return true;
        if (!inv[2].isItemEqual(out)) return false;
        int newSize = inv[2].stackSize + out.stackSize;
        return newSize <= inv[2].getMaxStackSize();
    }

    private boolean isFilterPresent() {
        if (inv[1] == null) return false;
        Integer t = FILTER_TIMES.get(new ItemKey(inv[1]));
        return t != null && t > 0;
    }

    private int nextProcessGoal() {
        Integer t = FILTER_TIMES.get(new ItemKey(inv[1]));
        return t != null ? t : 0;
    }

    private void consumeOneFilter() {
        if (inv[1] == null) return;
        inv[1].stackSize--;
        if (inv[1].stackSize <= 0) inv[1] = null;
    }

    /**
     * Processing chain:
     *  - If input is TANItems.water_bottle:
     *      dirty(0) -> regular(1) -> purified(2)
     *  - If input is vanilla water bottle (Items.potionitem, dmg=0),
     *      treat as regular → purified, output TANItems.water_bottle (purified) if available.
     * Adjust metas as you wish.
     */
    private ItemStack getProcessResult(ItemStack in) {
        if (in == null) return null;

        // TAN water bottles
        if (TANItems.water_bottle != null && in.getItem() == TANItems.water_bottle) {
            int meta = in.getItemDamage();
            if (meta == META_DIRTY)   return new ItemStack(TANItems.water_bottle, 1, META_REGULAR);
            if (meta == META_REGULAR) return new ItemStack(TANItems.water_bottle, 1, META_PURIFIED);
            return null; // already purified
        }

        // Vanilla water bottle (regular) -> purified TAN bottle if available, else same vanilla
        if (in.getItem() == Items.potionitem && in.getItemDamage() == 0) {
            if (TANItems.water_bottle != null) {
                return new ItemStack(TANItems.water_bottle, 1, META_PURIFIED);
            } else {
                return new ItemStack(Items.potionitem, 1, 0); // vanilla can't be "more purified"; placeholder
            }
        }

        return null;
    }

    private void doProcessOnce() {
        ItemStack out = getProcessResult(inv[0]);
        if (out == null) return;

        if (inv[2] == null) {
            inv[2] = out.copy();
        } else if (inv[2].isItemEqual(out)) {
            inv[2].stackSize += out.stackSize;
        }

        // consume one input
        inv[0].stackSize--;
        if (inv[0].stackSize <= 0) inv[0] = null;

        // consume one filter item
        consumeOneFilter();

        markDirty();
    }

    // === Inventory ===
    @Override public int getSizeInventory() { return 3; }
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
    @Override public String getInventoryName() { return "WaterPurifier"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(net.minecraft.entity.player.EntityPlayer player) {
        return player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0;
    }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 2) return false;

        if (slot == 1) return stack != null && FILTER_TIMES.containsKey(new ItemKey(stack));

        if (slot == 0 && stack != null) {
            if (TANItems.water_bottle != null && stack.getItem() == TANItems.water_bottle) {
                int m = stack.getItemDamage();
                return (m == META_DIRTY || m == META_REGULAR);
            }
            // vanilla water bottle (regular) also allowed
            return stack.getItem() == Items.potionitem && stack.getItemDamage() == 0;
        }
        return false;
    }
    @Override public int[] getAccessibleSlotsFromSide(int side) { return SLOTS_ALL; }
    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) { return isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) { return slot == 2; }

    // === NBT ===
    @Override public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("pt", processTime);
        nbt.setInteger("ptg", processTimeGoal);
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null) {
                NBTTagCompound t = new NBTTagCompound();
                t.setByte("Slot", (byte)i);
                inv[i].writeToNBT(t);
                nbt.setTag("Item" + i, t);
            }
        }
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        processTime = nbt.getInteger("pt");
        processTimeGoal = nbt.getInteger("ptg");
        for (int i = 0; i < inv.length; i++) {
            if (nbt.hasKey("Item" + i)) {
                inv[i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Item" + i));
            }
        }
    }

    public String debugString() {
        return "in=" + (inv[0] == null ? "null" : inv[0].getDisplayName() + "x" + inv[0].stackSize)
                + ", filter=" + (inv[1] == null ? "null" : inv[1].getDisplayName() + "x" + inv[1].stackSize)
                + ", out=" + (inv[2] == null ? "null" : inv[2].getDisplayName() + "x" + inv[2].stackSize)
                + ", t=" + processTime + "/" + processTimeGoal;
    }

    private static class ItemKey {
        final Item item;
        final int meta;
        ItemKey(Item item){ this(item, 0); }
        ItemKey(Item item, int meta){ this.item = item; this.meta = meta; }
        ItemKey(ItemStack s){ this(s.getItem(), s.getItemDamage()); }
        @Override public boolean equals(Object o){
            if (!(o instanceof ItemKey)) return false;
            ItemKey k = (ItemKey)o;
            return k.item == item && k.meta == meta;
        }
        @Override public int hashCode(){ return (item == null ? 0 : Item.getIdFromItem(item)) * 31 + meta; }
    }
}
