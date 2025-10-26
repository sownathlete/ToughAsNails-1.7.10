// File: toughasnails/init/ModCrafting.java
package toughasnails.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import toughasnails.api.TANBlocks;
import toughasnails.api.item.TANItems;
import toughasnails.block.BlockTANTemperatureCoil;
import toughasnails.item.ItemFruitJuice;
import toughasnails.item.ItemTANWaterBottle;

public final class ModCrafting {

    public static void init() {
        addCraftingRecipes();
        addSmeltingRecipes();
    }

    /* ============================================================ */
    /* Crafting                                                     */
    /* ============================================================ */
    private static void addCraftingRecipes() {
        /* ---------------- Armor: wool ---------------- */
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_helmet),
                "###", "# #", '#', Blocks.wool);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_chestplate),
                "# #", "###", "###", '#', Blocks.wool);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_leggings),
                "###", "# #", "# #", '#', Blocks.wool);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_boots),
                "# #", "# #", '#', Blocks.wool);

        /* ---------------- Armor: jelled slime ---------------- */
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_helmet),
                "###", "# #", '#', TANItems.jelled_slime);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_chestplate),
                "# #", "###", "###", '#', TANItems.jelled_slime);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_leggings),
                "###", "# #", "# #", '#', TANItems.jelled_slime);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_boots),
                "# #", "# #", '#', TANItems.jelled_slime);

        /* ---------------- Armor: leaf (ANY leaves via oredict + fallbacks) ---------------- */
        // Prefer oredict so modded leaves work out of the box
        safeAddOreRecipe(new ItemStack(TANItems.leaf_helmet),
                "###", "# #",
                '#', "treeLeaves");
        safeAddOreRecipe(new ItemStack(TANItems.leaf_chestplate),
                "# #", "###", "###",
                '#', "treeLeaves");
        safeAddOreRecipe(new ItemStack(TANItems.leaf_leggings),
                "###", "# #", "# #",
                '#', "treeLeaves");
        safeAddOreRecipe(new ItemStack(TANItems.leaf_boots),
                "# #", "# #",
                '#', "treeLeaves");

        // Fallbacks in case "treeLeaves" isn't populated in some packs
        ItemStack anyLeaves1 = new ItemStack(Blocks.leaves, 1, OreDictionary.WILDCARD_VALUE);
        ItemStack anyLeaves2 = new ItemStack(Blocks.leaves2, 1, OreDictionary.WILDCARD_VALUE);

        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_helmet),
                "###", "# #", '#', anyLeaves1);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_chestplate),
                "# #", "###", "###", '#', anyLeaves1);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_leggings),
                "###", "# #", "# #", '#', anyLeaves1);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_boots),
                "# #", "# #", '#', anyLeaves1);

        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_helmet),
                "###", "# #", '#', anyLeaves2);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_chestplate),
                "# #", "###", "###", '#', anyLeaves2);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_leggings),
                "###", "# #", "# #", '#', anyLeaves2);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.leaf_boots),
                "# #", "# #", '#', anyLeaves2);

        /* ---------------- Blocks ---------------- */
        safeAddOreRecipe(new ItemStack(TANBlocks.campfire),
                " L ",
                "LLL",
                "CCC",
                'L', "logWood",
                'C', Blocks.cobblestone
        );

        safeAddOreRecipe(new ItemStack(TANBlocks.rain_collector),
                "IBI",
                "C C",
                "CCC",
                'I', "ingotIron",
                'B', Blocks.iron_bars,
                'C', Blocks.cobblestone
        );

        GameRegistry.addShapedRecipe(
                new ItemStack(TANBlocks.temperature_coil, 1, BlockTANTemperatureCoil.CoilType.HEATING.ordinal()),
                "BBB", "BBB", "CCC",
                'B', Items.blaze_rod,
                'C', Blocks.cobblestone
        );
        GameRegistry.addShapedRecipe(
                new ItemStack(TANBlocks.temperature_coil, 1, BlockTANTemperatureCoil.CoilType.COOLING.ordinal()),
                "FFF", "FFF", "CCC",
                'F', TANItems.freeze_rod,
                'C', Blocks.cobblestone
        );

        /* ---------------- Canteen & filters ---------------- */
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.canteen),
                " L ",
                "L L",
                "LLL",
                'L', Items.leather
        );

        GameRegistry.addShapelessRecipe(
                new ItemStack(TANItems.water_bottle, 1, ItemTANWaterBottle.WaterBottleType.FILTERED.ordinal()),
                new ItemStack(TANItems.water_bottle, 1, ItemTANWaterBottle.WaterBottleType.DIRTY.ordinal()),
                TANItems.charcoal_filter
        );

        GameRegistry.addShapedRecipe(new ItemStack(TANItems.charcoal_filter, 3),
                "PPP", "CCC", "PPP",
                'P', Items.paper,
                'C', new ItemStack(Items.coal, 1, 1)
        );

        /* ---------------- Cold/ice items ---------------- */
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.freeze_powder, 2),
                TANItems.freeze_rod);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.ice_charge, 3),
                TANItems.ice_cube, Items.gunpowder, TANItems.freeze_powder);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime, 3),
                "III", "ISI", "III",
                'I', TANItems.ice_cube,
                'S', Items.slime_ball
        );

        /* ---------------- Thermometer ---------------- */
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.thermometer),
                " D ",
                "DQD",
                " D ",
                'D', Items.diamond,
                'Q', Items.quartz
        );

        /* ---------------- Fruit Juices ---------------- */
        addJuice("apple",            Items.apple);
        addJuice("beetroot",         null);
        addJuice("cactus",           Blocks.cactus);
        addJuice("carrot",           Items.carrot);
        addJuice("chorus_fruit",     null);
        addJuice("melon",            Items.melon);
        addJuice("pumpkin",          Blocks.pumpkin);
        addJuice("golden_apple",     Items.golden_apple);
        addJuice("golden_carrot",    Items.golden_carrot);
        addJuice("glistering_melon", Items.speckled_melon);
        addJuice("sweetberry",       oreOrItem("cropSweetberry", null));
        addJuice("glowberry",        oreOrItem("cropGlowberry",  null));
    }

    /* ============================================================ */
    /* Smelting (DIRTY -> FILTERED)                                 */
    /* ============================================================ */
    private static void addSmeltingRecipes() {
        GameRegistry.addSmelting(
                new ItemStack(TANItems.water_bottle, 1, ItemTANWaterBottle.WaterBottleType.DIRTY.ordinal()),
                new ItemStack(TANItems.water_bottle, 1, ItemTANWaterBottle.WaterBottleType.FILTERED.ordinal()),
                0.0F
        );
    }

    /* ============================================================ */
    /* Helpers                                                      */
    /* ============================================================ */

    private static void safeAddOreRecipe(ItemStack output, Object... patternAndMap) {
        if (output == null || output.getItem() == null) return;
        for (int i = 0; i < patternAndMap.length; i++) {
            if (patternAndMap[i] instanceof Character) {
                int v = i + 1;
                if (v >= patternAndMap.length) return;
                Object val = patternAndMap[v];
                if (val instanceof String) {
                    if (OreDictionary.getOres((String) val).isEmpty()) {
                        return; // skip if missing oredict entry
                    }
                } else if (val instanceof ItemStack) {
                    ItemStack st = (ItemStack) val;
                    if (st.getItem() == null) return;
                }
            }
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(output, patternAndMap));
    }

    private static ItemStack oreOrItem(String oreKey, ItemStack fallback) {
        if (oreKey != null && !OreDictionary.getOres(oreKey).isEmpty()) {
            ItemStack st = OreDictionary.getOres(oreKey).get(0);
            return st == null ? null : st.copy();
        }
        return fallback;
    }

    private static void addJuice(String juiceEnumLowerName, Object source) {
        int meta = juiceMeta(juiceEnumLowerName);
        if (meta < 0) return;

        ItemStack bottle = new ItemStack(Items.glass_bottle);
        ItemStack sugar  = new ItemStack(Items.sugar);

        ItemStack fruit = null;
        if (source instanceof ItemStack) {
            fruit = (ItemStack) source;
        } else if (source instanceof net.minecraft.item.Item) {
            fruit = new ItemStack((net.minecraft.item.Item) source);
        } else if (source instanceof net.minecraft.block.Block) {
            fruit = new ItemStack((net.minecraft.block.Block) source);
        } else if (source instanceof String) {
            fruit = oreOrItem((String) source, null);
        }

        if (fruit == null || fruit.getItem() == null) return;

        GameRegistry.addShapelessRecipe(
                new ItemStack(TANItems.fruit_juice, 1, meta),
                bottle, sugar, fruit
        );
    }

    private static int juiceMeta(String enumNameLower) {
        ItemFruitJuice.JuiceType[] vals = ItemFruitJuice.JuiceType.values();
        for (int i = 0; i < vals.length; i++) {
            if (vals[i].getName().equals(enumNameLower)) return i;
        }
        return -1;
    }

    private ModCrafting() {}
}
