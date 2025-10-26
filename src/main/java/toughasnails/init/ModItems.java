// File: toughasnails/init/ModItems.java
package toughasnails.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.EnumHelper;
import toughasnails.api.item.TANItems;
import toughasnails.item.*;
import toughasnails.util.inventory.CreativeTabTAN;
import toughasnails.recipe.RecipesWoolArmorDyes;   // keep this

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
        TANItems.lifeblood_crystal = registerItem(new ItemLifebloodCrystal(), "lifeblood_crystal");

        /* NEW foods (show up in custom tab ordering) */
        TANItems.ice_cream = registerItem(new ItemIceCream(), "ice_cream");
        TANItems.charc_os  = registerItem(new ItemCharcos(),  "charc_os");

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
        addWoolArmour();          // dyeable (icons + worn model + custom recipe)
        addJelledSlimeArmour();   // comes after wool in creative ordering
        addLeafArmour();          // biome-tinted while worn

        /* spawn egg */
        TANItems.spawn_egg = registerItem(new ItemTANSpawnEgg(), "spawn_egg");

        /* ---------------------- recipes ----------------------------------- */
        // Allow dyeing of the wool armor in the crafting grid (like leather)
        RecipesWoolArmorDyes.register();
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
        // Dyeable, with base + overlay icons, and overlay model layer
        TANItems.wool_helmet = registerItem(
            new ArmorItemWool(TANItems.wool_armor_material, 0), "wool_helmet");
        TANItems.wool_chestplate = registerItem(
            new ArmorItemWool(TANItems.wool_armor_material, 1), "wool_chestplate");
        TANItems.wool_leggings = registerItem(
            new ArmorItemWool(TANItems.wool_armor_material, 2), "wool_leggings");
        TANItems.wool_boots = registerItem(
            new ArmorItemWool(TANItems.wool_armor_material, 3), "wool_boots");
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

    private static void addLeafArmour() {
        // special subclass that provides custom item icons + foliage tint.
        TANItems.leaf_helmet = registerItem(
            new ArmorItemLeaf(TANItems.jelled_slime_armor_material, 0), "leaf_helmet");
        TANItems.leaf_chestplate = registerItem(
            new ArmorItemLeaf(TANItems.jelled_slime_armor_material, 1), "leaf_chestplate");
        TANItems.leaf_leggings = registerItem(
            new ArmorItemLeaf(TANItems.jelled_slime_armor_material, 2), "leaf_leggings");
        TANItems.leaf_boots = registerItem(
            new ArmorItemLeaf(TANItems.jelled_slime_armor_material, 3), "leaf_boots");
    }

    /* ============================================================= */
    /* tiny armour subclass that points to *_armor_layer_[1|2].png   */
    /* (used by jelled slime / respirator)                           */
    /* ============================================================= */
    private static class ArmorItemTAN extends ItemArmor {

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
            if ("overlay".equals(type)) {
                return MODID + ":textures/models/armor/" +
                       texPrefix + "_armor_layer_" + layer + "_overlay.png";
            }
            return MODID + ":textures/models/armor/" +
                   texPrefix + "_armor_layer_" + layer + ".png";
        }
    }

    /* ============================================================= */
    /* Wool armor: dyeable (icons + worn model overlay)              */
    /* - Item icons expected:                                        */
    /*   wool_helmet.png, wool_helmet_overlay.png, etc               */
    /* - Model textures expected:                                    */
    /*   wool_armor_layer_1.png / _overlay.png, layer_2...           */
    /* - Color stored in vanilla-style NBT: display.color (int RGB)  */
    /* ============================================================= */
    public static final class ArmorItemWool extends ItemArmor {

        private IIcon baseIcon;
        private IIcon overlayIcon;

        public ArmorItemWool(ArmorMaterial mat, int armorSlot) {
            super(mat, 0, armorSlot);
            setCreativeTab(CreativeTabTAN.instance);
            setUnlocalizedName(nameFor(armorSlot));
        }

        private static String nameFor(int slot) {
            switch (slot) {
                case 0: return "wool_helmet";
                case 1: return "wool_chestplate";
                case 2: return "wool_leggings";
                case 3: return "wool_boots";
            }
            return "wool_armor";
        }

        /* ----- worn model textures (supports overlay) ----- */
        @Override
        public String getArmorTexture(ItemStack stack,
                                      net.minecraft.entity.Entity entity,
                                      int slot, String type) {
            int layer = (slot == 2 ? 2 : 1);
            if ("overlay".equals(type)) {
                return MODID + ":textures/models/armor/wool_armor_layer_" + layer + "_overlay.png";
            }
            return MODID + ":textures/models/armor/wool_armor_layer_" + layer + ".png";
        }

        /* ----- item icon registration (two-pass) ----- */
        @Override
        public void registerIcons(IIconRegister reg) {
            String part = partSuffix(this.armorType);
            String base = MODID + ":wool_" + part;
            String over = base + "_overlay";
            this.baseIcon    = reg.registerIcon(base);
            this.overlayIcon = reg.registerIcon(over);
        }

        @Override public boolean requiresMultipleRenderPasses() { return true; }

        @Override
        public IIcon getIcon(ItemStack stack, int pass) {
            return (pass == 0) ? baseIcon : overlayIcon;
        }

        /* ----- coloring (vanilla-style: display.color) ----- */
        @Override public boolean hasColor(ItemStack stack) {
            return getColorTag(stack, false) != null && getColorTag(stack, false).hasKey("color");
        }

        @Override
        public int getColor(ItemStack stack) {
            net.minecraft.nbt.NBTTagCompound disp = getColorTag(stack, false);
            if (disp != null && disp.hasKey("color")) {
                return disp.getInteger("color");
            }
            // default white (no tint)
            return 0xFFFFFF;
        }

        @Override
        public void removeColor(ItemStack stack) {
            net.minecraft.nbt.NBTTagCompound disp = getColorTag(stack, false);
            if (disp != null && disp.hasKey("color")) {
                disp.removeTag("color");
            }
        }

        // 1.7.10 setColor obf name
        @Override
        public void func_82813_b(ItemStack stack, int color) {
            net.minecraft.nbt.NBTTagCompound disp = getColorTag(stack, true);
            disp.setInteger("color", color);
        }

        @Override
        public int getColorFromItemStack(ItemStack stack, int pass) {
            // base pass tinted, overlay pass white
            if (pass == 0) return getColor(stack);
            return 0xFFFFFF;
        }

        private static net.minecraft.nbt.NBTTagCompound getColorTag(ItemStack stack, boolean create) {
            if (!stack.hasTagCompound()) {
                if (!create) return null;
                stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            }
            net.minecraft.nbt.NBTTagCompound tag = stack.getTagCompound();
            if (!tag.hasKey("display")) {
                if (!create) return null;
                tag.setTag("display", new net.minecraft.nbt.NBTTagCompound());
            }
            return tag.getCompoundTag("display");
        }

        private static String partSuffix(int armorType) {
            switch (armorType) {
                case 0: return "helmet";
                case 1: return "chestplate";
                case 2: return "leggings";
                case 3: return "boots";
            }
            return "armor";
        }
    }

    /* ============================================================= */
    /* Leaf armor: custom item icons + dynamic biome tint when worn  */
    /* ============================================================= */
    private static final class ArmorItemLeaf extends ItemArmor {

        private static final String NBT_COLOR_KEY = "LeafTint";

        // One base + one overlay icon per piece
        private IIcon baseIcon;
        private IIcon overlayIcon;

        ArmorItemLeaf(ArmorMaterial mat, int slot) {
            super(mat, 0, slot);
            setCreativeTab(CreativeTabTAN.instance);
            setUnlocalizedName(nameFor(slot));
        }

        private static String nameFor(int slot) {
            switch (slot) {
                case 0: return "leaf_helmet";
                case 1: return "leaf_chestplate";
                case 2: return "leaf_leggings";
                case 3: return "leaf_boots";
            }
            return "leaf_armor";
        }

        /* ----- worn model textures ----- */
        @Override
        public String getArmorTexture(ItemStack stack,
                                      net.minecraft.entity.Entity entity,
                                      int slot, String type) {
            int layer = (slot == 2 ? 2 : 1);
            if ("overlay".equals(type)) {
                return MODID + ":textures/models/armor/leaf_armor_layer_" + layer + "_overlay.png";
            }
            return MODID + ":textures/models/armor/leaf_armor_layer_" + layer + ".png";
        }

        /* ----- dynamic biome tint while worn ----- */
        @Override
        public void onArmorTick(World world, net.minecraft.entity.player.EntityPlayer player, ItemStack stack) {
            int x = (int)Math.floor(player.posX);
            int y = (int)Math.floor(player.posY);
            int z = (int)Math.floor(player.posZ);

            BiomeGenBase biome = world.getBiomeGenForCoords(x, z);

            int tint = (biome != null)
                    ? biome.getBiomeFoliageColor(x, y, z) // 3-arg signature in 1.7.10
                    : ColorizerFoliage.getFoliageColorBasic();

            ensureTag(stack).setInteger(NBT_COLOR_KEY, tint);
        }

        @Override public boolean hasColor(ItemStack stack) { return true; }

        @Override
        public int getColor(ItemStack stack) {
            if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_COLOR_KEY)) {
                return stack.getTagCompound().getInteger(NBT_COLOR_KEY);
            }
            return ColorizerFoliage.getFoliageColorBasic();
        }

        /* ----- item icon registration (two-pass) ----- */
        @Override
        public void registerIcons(IIconRegister reg) {
            String base = MODID + ":leaf_" + partSuffix(this.armorType);
            String over = base + "_overlay";
            this.baseIcon    = reg.registerIcon(base);
            this.overlayIcon = reg.registerIcon(over);
        }

        @Override public boolean requiresMultipleRenderPasses() { return true; }

        @Override
        public IIcon getIcon(ItemStack stack, int pass) {
            return (pass == 0) ? baseIcon : overlayIcon;
        }

        @Override
        public int getColorFromItemStack(ItemStack stack, int pass) {
            return (pass == 0) ? ColorizerFoliage.getFoliageColorBasic() : 0xFFFFFF;
        }

        private static String partSuffix(int armorType) {
            switch (armorType) {
                case 0: return "helmet";
                case 1: return "chestplate";
                case 2: return "leggings";
                case 3: return "boots";
            }
            return "armor";
        }

        private static net.minecraft.nbt.NBTTagCompound ensureTag(ItemStack stack) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            }
            return stack.getTagCompound();
        }
    }

    private ModItems() {} // utility class – no instances
}
