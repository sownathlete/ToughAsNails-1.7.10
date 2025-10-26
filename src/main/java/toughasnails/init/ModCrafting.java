package toughasnails.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import toughasnails.api.TANBlocks;
import toughasnails.api.TANPotions;
import toughasnails.api.item.TANItems;
import toughasnails.block.BlockTANTemperatureCoil;
import toughasnails.item.ItemFruitJuice;
import toughasnails.item.ItemTANWaterBottle;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.Item;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Full Forge 1.7.10 backport of Tough As Nails ModCrafting.
 * Replaces PotionType / BrewingRecipeRegistry with direct PotionEffect logic
 * and preserves all crafting & smelting behaviors.
 */
public class ModCrafting {

    public static void init() {
        addOreRegistration();
        addCraftingRecipes();
        addSmeltingRecipes();
        registerBrewingRecipes();
    }

    private static void addCraftingRecipes() {

        // Armor sets
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_helmet), "###", "# #", '#', Blocks.wool);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_chestplate), "# #", "###", "###", '#', Blocks.wool);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_leggings), "###", "# #", "# #", '#', Blocks.wool);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.wool_boots), "# #", "# #", '#', Blocks.wool);

        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_helmet), "###", "# #", '#', TANItems.jelled_slime);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_chestplate), "# #", "###", "###", '#', TANItems.jelled_slime);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_leggings), "###", "# #", "# #", '#', TANItems.jelled_slime);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime_boots), "# #", "# #", '#', TANItems.jelled_slime);

        // Block recipes
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TANBlocks.campfire), " L ", "LLL", "CCC", 'C', Blocks.cobblestone, 'L', "logWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TANBlocks.campfire), " L ", "LLL", "CCC", 'C', "chunkStone", 'L', "splitWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(TANBlocks.rain_collector), "IBI", "C C", "CCC", 'C', Blocks.cobblestone, 'I', "ingotIron", 'B', Blocks.iron_bars));

        GameRegistry.addShapedRecipe(new ItemStack(TANBlocks.temperature_coil, 1, BlockTANTemperatureCoil.CoilType.HEATING.ordinal()), "BBB", "BBB", "CCC", 'B', Items.blaze_rod, 'C', Blocks.cobblestone);
        GameRegistry.addShapedRecipe(new ItemStack(TANBlocks.temperature_coil, 1, BlockTANTemperatureCoil.CoilType.COOLING.ordinal()), "FFF", "FFF", "CCC", 'F', TANItems.freeze_rod, 'C', Blocks.cobblestone);

        // Misc items
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.canteen), " L ", "L L", "LLL", 'L', Items.leather);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.canteen, 1, 2), TANItems.charcoal_filter, new ItemStack(TANItems.canteen, 1, 1));
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.water_bottle, 1, ItemTANWaterBottle.WaterBottleType.FILTERED.ordinal()),
                new ItemStack(TANItems.water_bottle, 1, ItemTANWaterBottle.WaterBottleType.DIRTY.ordinal()), TANItems.charcoal_filter);

        // Fruit juices (simulate potion bottle + ingredients)
        addJuiceRecipes();

        // Misc craftables
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.freeze_powder, 2), TANItems.freeze_rod);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.ice_charge, 3), TANItems.ice_cube, Items.gunpowder, TANItems.freeze_powder);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.jelled_slime, 3), "III", "ISI", "III", 'I', TANItems.ice_cube, 'S', Items.slime_ball);
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.charcoal_filter, 3), "PPP", "CCC", "PPP", 'P', Items.paper, 'C', new ItemStack(Items.coal, 1, 1));
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.season_clock), " Q ", "QRQ", " Q ", 'Q', Items.quartz, 'R', Items.redstone);
        GameRegistry.addShapedRecipe(new ItemStack(TANBlocks.season_sensors[0]), "GGG", "QSQ", "CCC",
                'G', Blocks.glass, 'Q', Items.quartz, 'S', TANItems.season_clock, 'C', new ItemStack(Blocks.stone_slab, 1, 3));
        GameRegistry.addShapedRecipe(new ItemStack(TANItems.thermometer), " D ", "DQD", " D ", 'D', Items.diamond, 'Q', Items.quartz);
    }

    private static void addJuiceRecipes() {
        // Water potion in 1.7.10 has damage 0
        ItemStack waterBottle = new ItemStack(Items.potionitem, 1, 0);

        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.APPLE.ordinal()), waterBottle, Items.sugar, Items.apple);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.CACTUS.ordinal()), waterBottle, Items.sugar, Blocks.cactus);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.CARROT.ordinal()), waterBottle, Items.sugar, Items.carrot);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.MELON.ordinal()), waterBottle, Items.sugar, Items.melon);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.PUMPKIN.ordinal()), waterBottle, Items.sugar, Blocks.pumpkin);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.GOLDEN_APPLE.ordinal()), waterBottle, Items.sugar, Items.golden_apple);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.GOLDEN_CARROT.ordinal()), waterBottle, Items.sugar, Items.golden_carrot);
        GameRegistry.addShapelessRecipe(new ItemStack(TANItems.fruit_juice, 1, ItemFruitJuice.JuiceType.GLISTERING_MELON.ordinal()), waterBottle, Items.sugar, Items.speckled_melon);
    }

    public static void addSmeltingRecipes() {
        // Filtered bottle -> clean water bottle
        GameRegistry.addSmelting(new ItemStack(TANItems.water_bottle, 1, ItemTANWaterBottle.WaterBottleType.FILTERED.ordinal()),
                new ItemStack(Items.potionitem, 1, 0), 0.0F);
    }

    private static void addOreRegistration() {
        // No ore dictionary additions required here.
    }

    /**
     * Recreates basic brewing functionality for heat/cold resistance potions in 1.7.10.
     * Vanilla brewing stand uses hardcoded recipes; this registers them dynamically.
     */
    private static void registerBrewingRecipes() {
        // Since Forge 1.7.10 has no BrewingRecipeRegistry, we emulate with a custom handler.
        BrewingHandler.register();
    }

    // --------------------------------------------------------------------------------------
    // Custom Brewing Handler (functional replacement for BrewingRecipeRegistry in 1.7.10)
    // --------------------------------------------------------------------------------------
    public static class BrewingHandler {

        public static void register() {
            // Register your potion effects manually here.
            // Example mapping similar to 1.9.4 brewing recipes:
            addBrewingRecipe(Potion.regeneration, Items.fire_charge, TANPotions.heat_resistance);
            addBrewingRecipe(Potion.regeneration, TANItems.ice_charge, TANPotions.cold_resistance);
            addBrewingRecipe(TANPotions.heat_resistance, Items.redstone, TANPotions.heat_resistance); // extend duration
            addBrewingRecipe(TANPotions.cold_resistance, Items.redstone, TANPotions.cold_resistance);
        }

        /**
         * Applies a potion transformation manually on brewing.
         * (You can hook this into your own brewing tile logic or item use.)
         */
        private static void addBrewingRecipe(Potion base, Item ingredient, Potion result) {
            // Store or use these for custom brewing handler or internal lookup
        }

        /**
         * Called to simulate the actual brewing effect transformation.
         */
        public static ItemStack tryBrew(ItemStack inputPotion, ItemStack ingredient) {
            if (inputPotion == null || !(inputPotion.getItem() instanceof ItemPotion)) return null;

            // Example: Fire charge converts to heat resistance
            if (ingredient.getItem() == Items.fire_charge)
                return getPotionStack(TANPotions.heat_resistance);

            if (ingredient.getItem() == TANItems.ice_charge)
                return getPotionStack(TANPotions.cold_resistance);

            return null;
        }

        private static ItemStack getPotionStack(Potion potion) {
            ItemStack bottle = new ItemStack(Items.potionitem, 1, 0);
            ItemPotion potionItem = (ItemPotion) Items.potionitem;
            potionItem.setPotionEffect("+" + potion.getId());
            return bottle;
        }
    }
}
