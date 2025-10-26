// File: toughasnails/recipe/RecipesWoolArmorDyes.java
package toughasnails.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import toughasnails.api.item.TANItems;

/**
 * Clone of vanilla RecipesArmorDyes, but targets Tough As Nails WOOl armor
 * by checking the concrete item instances in TANItems (no inner-class typing).
 */
public final class RecipesWoolArmorDyes implements IRecipe {

    public static void register() {
        @SuppressWarnings("unchecked")
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        recipes.add(new RecipesWoolArmorDyes());
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        ItemStack armor = null;
        List<ItemStack> dyes = new ArrayList<ItemStack>();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack st = inv.getStackInSlot(i);
            if (st == null) continue;

            Item it = st.getItem();
            if (isWoolArmorItem(it)) {
                if (armor != null) return false; // only one piece allowed
                armor = st;
            } else if (it == Items.dye) {
                dyes.add(st);
            } else {
                return false; // unexpected item
            }
        }
        return armor != null && !dyes.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack armor = null;
        int[] rgb = new int[3];
        int totalShade = 0;
        int count = 0;

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack st = inv.getStackInSlot(i);
            if (st == null) continue;

            Item it = st.getItem();
            if (isWoolArmorItem(it)) {
                if (armor != null) return null;
                armor = st.copy();
                armor.stackSize = 1;

                if (((ItemArmor)it).hasColor(st)) {
                    int col = ((ItemArmor)it).getColor(st);
                    float r = (float)((col >> 16) & 0xFF) / 255.0F;
                    float g = (float)((col >>  8) & 0xFF) / 255.0F;
                    float b = (float)((col      ) & 0xFF) / 255.0F;
                    totalShade += (int)(Math.max(r, Math.max(g, b)) * 255.0F);
                    rgb[0] += (int)(r * 255.0F);
                    rgb[1] += (int)(g * 255.0F);
                    rgb[2] += (int)(b * 255.0F);
                    ++count;
                }
            } else if (it == Items.dye) {
                int dyeMeta = st.getItemDamage();
                int col = net.minecraft.item.ItemDye.field_150922_c[dyeMeta];

                float r = (float)((col >> 16) & 0xFF) / 255.0F;
                float g = (float)((col >>  8) & 0xFF) / 255.0F;
                float b = (float)((col      ) & 0xFF) / 255.0F;

                totalShade += (int)(Math.max(r, Math.max(g, b)) * 255.0F);
                rgb[0] += (int)(r * 255.0F);
                rgb[1] += (int)(g * 255.0F);
                rgb[2] += (int)(b * 255.0F);
                ++count;
            } else {
                return null;
            }
        }

        if (armor == null) return null;

        int r = rgb[0] / count;
        int g = rgb[1] / count;
        int b = rgb[2] / count;
        float avgShade = (float)totalShade / (float)count;
        float max = Math.max(r, Math.max(g, b));

        if (max == 0) max = 1; // avoid div by zero when starting from pure black

        // Normalize like vanilla so colors don't get too dark
        r = (int)(r * (avgShade / max));
        g = (int)(g * (avgShade / max));
        b = (int)(b * (avgShade / max));

        int finalColor = (r << 16) | (g << 8) | b;
        ((ItemArmor)armor.getItem()).func_82813_b(armor, finalColor);
        return armor;
    }

    @Override
    public int getRecipeSize() { return 10; }

    @Override
    public ItemStack getRecipeOutput() { return null; }

    /* -------------------------------------------------------------- */
    /* Helpers                                                        */
    /* -------------------------------------------------------------- */
    private static boolean isWoolArmorItem(Item it) {
        return it == TANItems.wool_helmet
            || it == TANItems.wool_chestplate
            || it == TANItems.wool_leggings
            || it == TANItems.wool_boots;
    }
}
