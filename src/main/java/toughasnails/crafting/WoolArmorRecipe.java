// File: toughasnails/crafting/WoolArmorRecipe.java
package toughasnails.crafting;

import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import toughasnails.init.ModItems;

public class WoolArmorRecipe implements IRecipe {

    private final String[] pattern; // e.g., {"###","# #"} etc.
    private final int resultArmorType; // 0 helm,1 chest,2 legs,3 boots

    public WoolArmorRecipe(int armorType, String... pattern) {
        this.pattern = pattern;
        this.resultArmorType = armorType;
    }

    @Override public boolean matches(InventoryCrafting inv, World world) {
        return findColor(inv) != -1 && shapeMatches(inv);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int woolMeta = findColor(inv);
        if (woolMeta == -1) return null;

        ItemStack out;
        switch (resultArmorType) {
            case 0: out = new ItemStack(toughasnails.api.item.TANItems.wool_helmet); break;
            case 1: out = new ItemStack(toughasnails.api.item.TANItems.wool_chestplate); break;
            case 2: out = new ItemStack(toughasnails.api.item.TANItems.wool_leggings); break;
            default: out = new ItemStack(toughasnails.api.item.TANItems.wool_boots); break;
        }

        // Convert WOOL metadata (0=white … 15=black) to the corresponding DYE metadata (15=white … 0=black)
        final int dyeMeta = 15 - woolMeta;
        final int rgb = ItemDye.field_150922_c[dyeMeta]; // vanilla leather-armor RGB

        // Write solid color directly (no pastel mixing here)
        net.minecraft.nbt.NBTTagCompound tag = out.getTagCompound();
        if (tag == null) { tag = new net.minecraft.nbt.NBTTagCompound(); out.setTagCompound(tag); }
        net.minecraft.nbt.NBTTagCompound disp = tag.getCompoundTag("display");
        if (!tag.hasKey("display")) tag.setTag("display", disp);
        disp.setInteger("color", rgb);

        return out;
    }
    
    @Override public int getRecipeSize() { return 9; }
    @Override public ItemStack getRecipeOutput() { return null; } // dynamic

    /* -------- helpers ---------- */

    // Require the standard armor layout with any wool blocks
    private boolean shapeMatches(InventoryCrafting inv) {
        // 3x3 grid only in 1.7.10 crafting table
        // Check that cells with '#' in the pattern contain wool, and ' ' are empty
        for (int r = 0; r < 3; r++) {
            String row = r < pattern.length ? pattern[r] : "   ";
            for (int c = 0; c < 3; c++) {
                char ch = c < row.length() ? row.charAt(c) : ' ';
                ItemStack cell = inv.getStackInRowAndColumn(c, r);
                if (ch == '#') {
                    if (cell == null || cell.getItem() != Item.getItemFromBlock(Blocks.wool)) return false;
                } else {
                    if (cell != null) return false;
                }
            }
        }
        return true;
    }

    private int findColor(InventoryCrafting inv) {
        int meta = -1;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack st = inv.getStackInSlot(i);
            if (st != null && st.getItem() == Item.getItemFromBlock(Blocks.wool)) {
                int m = st.getItemDamage();
                if (meta == -1) meta = m;
                else if (meta != m) return -1; // mixed colors → treat as no match for this recipe
            }
        }
        return meta;
    }
}
