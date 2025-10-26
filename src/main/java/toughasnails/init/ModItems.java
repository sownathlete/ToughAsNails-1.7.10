package toughasnails.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import toughasnails.api.item.TANItems;
import toughasnails.item.*;
import toughasnails.util.inventory.CreativeTabTAN;

/** Registers every Tough-as-Nails item for the 1.7.10 back-port. */
public final class ModItems {

    /* --------------------------------------------------------------------- */
    private static final String MODID = "toughasnails";

    public static void init() { registerItems(); }

    /* --------------------------------------------------------------------- */
    private static void registerItems() {

        /* hidden logo (no creative-tab entry) */
        TANItems.tan_icon = registerItem(new Item(), "tan_icon", null);

        /* ---------------------- armour materials (4-arg API) -------------- */
        TANItems.wool_armor_material = EnumHelper.addArmorMaterial(
                "WOOL", 3, new int[] {2, 2, 2, 1}, 5);

        TANItems.jelled_slime_armor_material = EnumHelper.addArmorMaterial(
                "JELLED_SLIME", 9, new int[] {2, 5, 3, 2}, 11);

        TANItems.respirator_material = EnumHelper.addArmorMaterial(
                "RESPIRATOR", -1, new int[] {0, 0, 0, 0}, 0);

        TANItems.wool_armor_material.customCraftingMaterial =
                Item.getItemFromBlock(Blocks.wool);

        /* ---------------------- regular items ----------------------------- */
        TANItems.thermometer       = registerItem(new ItemThermometer(),       "thermometer");
        TANItems.season_clock      = registerItem(new ItemSeasonClock(),      "season_clock");
        TANItems.lifeblood_crystal = registerItem(new ItemLifebloodCrystal(), "lifeblood_crystal");

        /* meta-icon items (skip setTextureName – they handle it themselves) */
        TANItems.canteen       = registerItem(new ItemCanteen(),        "canteen",       true);
        TANItems.water_bottle  = registerItem(new ItemTANWaterBottle(), "water_bottle", true);
        TANItems.fruit_juice   = registerItem(new ItemFruitJuice(),     "fruit_juice",  true);

        TANItems.ice_cube        = registerItem(new Item(), "ice_cube");
        TANItems.freeze_rod      = registerItem(new Item(), "freeze_rod");
        TANItems.freeze_powder   = registerItem(new Item(), "freeze_powder");
        TANItems.ice_charge      = registerItem(new ItemIceCharge(), "ice_charge");
        TANItems.jelled_slime    = registerItem(new Item(), "jelled_slime");
        TANItems.charcoal_filter = registerItem(new Item(), "charcoal_filter");

        /* ---------------------- armour items ------------------------------ */
        addWoolArmour();
        addJelledSlimeArmour();

        /* spawn egg */
        TANItems.spawn_egg = registerItem(new ItemTANSpawnEgg(), "spawn_egg");
    }

    /* ============================================================= */
    /* registration helpers                                          */
    /* ============================================================= */

    private static Item registerItem(Item it, String name) {
        return registerItem(it, name, CreativeTabTAN.instance, false);
    }
    private static Item registerItem(Item it, String name, boolean metaIcons) {
        return registerItem(it, name, CreativeTabTAN.instance, metaIcons);
    }
    private static Item registerItem(Item it, String name,
                                     CreativeTabs tab) {
        return registerItem(it, name, tab, false);
    }

    /**
     * Core helper.  
     * @param metaIcons if <code>true</code> we **don’t** call
     *                  <code>setTextureName</code> because the item itself
     *                  registers multiple icons.
     */
    private static Item registerItem(Item it, String name,
                                     CreativeTabs tab, boolean metaIcons) {

        it.setUnlocalizedName(name);
        if (!metaIcons) it.setTextureName(MODID + ':' + name);
        if (tab != null) it.setCreativeTab(tab);

        GameRegistry.registerItem(it, name);
        return it;
    }

    /* ============================================================= */
    /* armour helpers                                                */
    /* ============================================================= */
    private static void addWoolArmour() {
        TANItems.wool_helmet = registerItem(
            new ArmorItemTAN(TANItems.wool_armor_material, "wool", 0), "wool_helmet");
        TANItems.wool_chestplate = registerItem(
            new ArmorItemTAN(TANItems.wool_armor_material, "wool", 1), "wool_chestplate");
        TANItems.wool_leggings = registerItem(
            new ArmorItemTAN(TANItems.wool_armor_material, "wool", 2), "wool_leggings");
        TANItems.wool_boots = registerItem(
            new ArmorItemTAN(TANItems.wool_armor_material, "wool", 3), "wool_boots");
    }

    private static void addJelledSlimeArmour() {
        TANItems.jelled_slime_helmet = registerItem(
            new ArmorItemTAN(TANItems.jelled_slime_armor_material, "jelled_slime", 0), "jelled_slime_helmet");
        TANItems.jelled_slime_chestplate = registerItem(
            new ArmorItemTAN(TANItems.jelled_slime_armor_material, "jelled_slime", 1), "jelled_slime_chestplate");
        TANItems.jelled_slime_leggings = registerItem(
            new ArmorItemTAN(TANItems.jelled_slime_armor_material, "jelled_slime", 2), "jelled_slime_leggings");
        TANItems.jelled_slime_boots = registerItem(
            new ArmorItemTAN(TANItems.jelled_slime_armor_material, "jelled_slime", 3), "jelled_slime_boots");
    }

    /* ============================================================= */
    /* tiny armour subclass that points to *_armor_layer_[1|2].png    */
    /* ============================================================= */
    private static final class ArmorItemTAN extends ItemArmor {

        private final String texPrefix;

        ArmorItemTAN(ArmorMaterial mat, String prefix, int slot) {
            super(mat, 0, slot);
            this.texPrefix = prefix;
        }

        @Override
        public String getArmorTexture(ItemStack stack,
                                      net.minecraft.entity.Entity entity,
                                      int slot, String type) {
            int layer = (slot == 2 ? 2 : 1);
            return MODID + ":textures/models/armor/" +
                   texPrefix + "_armor_layer_" + layer + ".png";
        }
    }

    private ModItems() {} // utility class – no instances
}
