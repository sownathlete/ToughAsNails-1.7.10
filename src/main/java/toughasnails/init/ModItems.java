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

/** Registers every Tough-as-Nails item for the 1.7.10 back-port. */
public final class ModItems {

    private static final String MODID = "toughasnails";

    public static void init() { registerItems(); }

    private static void registerItems() {

        /* hidden logo (no creative-tab entry) */
        TANItems.tan_icon = registerItem(new Item(), "tan_icon", null);

        /* ---------------------- armour materials -------------------------- */
        // Custom material used only for jelled slime
        TANItems.jelled_slime_armor_material = EnumHelper.addArmorMaterial(
                "JELLED_SLIME", 9, new int[] {2, 5, 3, 2}, 11);

        TANItems.respirator_material = EnumHelper.addArmorMaterial(
                "RESPIRATOR", -1, new int[] {0, 0, 0, 0}, 0);

        // Use vanilla CLOTH so vanilla dyeing + model tint works for wool/leaf
        ItemArmor.ArmorMaterial CLOTH = ItemArmor.ArmorMaterial.CLOTH;
        CLOTH.customCraftingMaterial = Item.getItemFromBlock(Blocks.wool);

        /* ---------------------- regular items ----------------------------- */
        TANItems.thermometer       = registerItem(new ItemThermometer(),       "thermometer");
        TANItems.lifeblood_crystal = registerItem(new ItemLifebloodCrystal(), "lifeblood_crystal");

        TANItems.ice_cream = registerItem(new ItemIceCream(), "ice_cream");
        TANItems.charc_os  = registerItem(new ItemCharcos(),  "charc_os");

        /* meta-icon items (multi-icon register themselves) */
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
        addWoolArmour(CLOTH);                // dyeable, tinted model + icons
        addJelledSlimeArmour();              // non-dyeable custom
        addLeafArmour(CLOTH);                // biome-tinted model + icons

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

    private static void addWoolArmour(ItemArmor.ArmorMaterial cloth) {
        TANItems.wool_helmet = registerItem(
            new ArmorItemWool(cloth, 0), "wool_helmet");
        TANItems.wool_chestplate = registerItem(
            new ArmorItemWool(cloth, 1), "wool_chestplate");
        TANItems.wool_leggings = registerItem(
            new ArmorItemWool(cloth, 2), "wool_leggings");
        TANItems.wool_boots = registerItem(
            new ArmorItemWool(cloth, 3), "wool_boots");
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

    private static void addLeafArmour(ItemArmor.ArmorMaterial cloth) {
        TANItems.leaf_helmet = registerItem(
            new ArmorItemLeaf(cloth, 0), "leaf_helmet");
        TANItems.leaf_chestplate = registerItem(
            new ArmorItemLeaf(cloth, 1), "leaf_chestplate");
        TANItems.leaf_leggings = registerItem(
            new ArmorItemLeaf(cloth, 2), "leaf_leggings");
        TANItems.leaf_boots = registerItem(
            new ArmorItemLeaf(cloth, 3), "leaf_boots");
    }

    /* ============================================================= */
    /* jelled slime (non-dyeable)                                    */
    /* ============================================================= */
    private static class ArmorItemTAN extends ItemArmor {

        private final String texPrefix;

        ArmorItemTAN(ArmorMaterial mat, String prefix, int slot) {
            super(mat, 0, slot);
            this.texPrefix = prefix;
            setCreativeTab(CreativeTabTAN.instance);
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
    /* Wool armor: dyeable + overlay icons + public pastel setter     */
    /* ============================================================= */
    public static final class ArmorItemWool extends ItemArmor {

        private static final String TEX_PREFIX = "wool";
        private static final String NBT_DISPLAY = "display";
        private static final String NBT_COLOR   = "color";
        private static final int DEFAULT_COLOR = 0xFFFFFF; // default to white in case no NBT

        private IIcon baseIcon;
        private IIcon overlayIcon;

        public ArmorItemWool(ArmorMaterial mat, int slot) {
            super(mat, 0, slot); // CLOTH -> vanilla dyeing works
            setCreativeTab(CreativeTabTAN.instance);
        }

        /* model textures */
        @Override
        public String getArmorTexture(ItemStack stack,
                                      net.minecraft.entity.Entity entity,
                                      int slot, String type) {
            int layer = (slot == 2 ? 2 : 1);
            if ("overlay".equals(type)) {
                return MODID + ":textures/models/armor/" +
                        TEX_PREFIX + "_armor_layer_" + layer + "_overlay.png";
            }
            return MODID + ":textures/models/armor/" +
                    TEX_PREFIX + "_armor_layer_" + layer + ".png";
        }

        /* dyed behavior (for model) */
        @Override public boolean hasColor(ItemStack stack) { return true; }

        @Override
        public int getColor(ItemStack stack) {
            if (stack != null && stack.hasTagCompound()
                && stack.getTagCompound().hasKey(NBT_DISPLAY)) {
                net.minecraft.nbt.NBTTagCompound d =
                        stack.getTagCompound().getCompoundTag(NBT_DISPLAY);
                if (d.hasKey(NBT_COLOR)) return d.getInteger(NBT_COLOR);
            }
            return DEFAULT_COLOR;
        }

        /** Public helper used by CreativeTabTAN to pre-tint to a *pastel* (mix with white). */
        public void setPastelColor(ItemStack stack, int rgb) {
            int pastel = pastelize(rgb);
            net.minecraft.nbt.NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) { tag = new net.minecraft.nbt.NBTTagCompound(); stack.setTagCompound(tag); }
            net.minecraft.nbt.NBTTagCompound disp = tag.getCompoundTag(NBT_DISPLAY);
            if (!tag.hasKey(NBT_DISPLAY)) tag.setTag(NBT_DISPLAY, disp);
            disp.setInteger(NBT_COLOR, pastel);
        }

        private static int pastelize(int rgb) {
            // (fix) correct precedence: mask BEFORE adding 0xFF
            int r = (((rgb >> 16) & 0xFF) + 0xFF) / 2;
            int g = (((rgb >>  8) & 0xFF) + 0xFF) / 2;
            int b = (((rgb      ) & 0xFF) + 0xFF) / 2;
            return (r << 16) | (g << 8) | b;
        }

        /* icons (two passes: tinted base + white overlay) */
        @Override
        public void registerIcons(IIconRegister reg) {
            String part = partSuffix(this.armorType);
            baseIcon    = reg.registerIcon(MODID + ":" + TEX_PREFIX + "_" + part);
            overlayIcon = reg.registerIcon(MODID + ":" + TEX_PREFIX + "_" + part + "_overlay");
        }

        @Override public boolean requiresMultipleRenderPasses() { return true; }
        @Override public IIcon getIcon(ItemStack stack, int pass) { return (pass == 0) ? baseIcon : overlayIcon; }
        @Override public int getColorFromItemStack(ItemStack stack, int pass) { return (pass == 0) ? getColor(stack) : 0xFFFFFF; }

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
    /* Leaf armor: biome-tinted icons AND model                      */
    /* ============================================================= */
    public static final class ArmorItemLeaf extends ItemArmor {

        private static final String NBT_COLOR_KEY = "LeafTint";

        private IIcon baseIcon;
        private IIcon overlayIcon;

        public ArmorItemLeaf(ArmorMaterial mat, int slot) {
            super(mat, 0, slot);   // CLOTH so model tint applies
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

        /** Keep tint current both when worn and when just sitting in inventory. */
        @Override
        public void onUpdate(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean held) {
            updateTint(stack, world, (int)Math.floor(entity.posX), (int)Math.floor(entity.posY), (int)Math.floor(entity.posZ));
        }

        @Override
        public void onArmorTick(World world, net.minecraft.entity.player.EntityPlayer player, ItemStack stack) {
            updateTint(stack, world, (int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
        }

        private void updateTint(ItemStack stack, World world, int x, int y, int z) {
            BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
            int tint = (biome != null)
                    ? biome.getBiomeFoliageColor(x, y, z)
                    : ColorizerFoliage.getFoliageColorBasic();
            ensureTag(stack).setInteger(NBT_COLOR_KEY, tint);
        }

        @Override public boolean hasColor(ItemStack stack) { return true; }
        @Override public int getColor(ItemStack stack) {
            if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_COLOR_KEY)) {
                return stack.getTagCompound().getInteger(NBT_COLOR_KEY);
            }
            return ColorizerFoliage.getFoliageColorBasic();
        }

        @Override
        public void registerIcons(IIconRegister reg) {
            String base = MODID + ":leaf_" + partSuffix(this.armorType);
            String over = base + "_overlay";
            this.baseIcon    = reg.registerIcon(base);
            this.overlayIcon = reg.registerIcon(over);
        }

        @Override public boolean requiresMultipleRenderPasses() { return true; }
        @Override public IIcon getIcon(ItemStack stack, int pass) { return (pass == 0) ? baseIcon : overlayIcon; }
        @Override public int getColorFromItemStack(ItemStack stack, int pass) { return (pass == 0) ? getColor(stack) : 0xFFFFFF; }

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
            if (!stack.hasTagCompound()) stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            return stack.getTagCompound();
        }
    }

    private ModItems() {}
}
