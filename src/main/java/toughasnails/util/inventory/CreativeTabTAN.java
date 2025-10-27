// File: toughasnails/util/inventory/CreativeTabTAN.java
package toughasnails.util.inventory;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ColorizerFoliage;
import toughasnails.api.TANBlocks;
import toughasnails.api.item.TANItems;
import toughasnails.item.ItemCanteen;
import toughasnails.item.ItemFruitJuice;

/**
 * Tough As Nails creative tab with a fixed, explicit ordering.
 *
 * Order:
 *  1) Thermometer
 *  2) Leaf armor (helm, chest, legs, boots)  [pre-tinted to foliage color]
 *  3) Wool armor (helm, chest, legs, boots)  [pre-dyed to white]
 *  4) Jelled Slime armor (helm, chest, legs, boots)
 *  5) Foods: Ice Cream, Charc-O's
 *  6) Canteens per variant (empty, dirty, water, purified) for:
 *     leather, copper, iron, gold, diamond, netherite
 *  7) Water bottles: Dirty, Filtered, Purified
 *  8) Juices (in this order):
 *     apple, cactus, chorus fruit, glow berry, melon, pumpkin, sweet berry,
 *     carrot, beetroot, glistering melon, golden carrot, golden apple
 *  9) Basic mats: ice cube, freeze rod, freeze powder, ice charge, jelled slime
 */
public final class CreativeTabTAN extends CreativeTabs {

    public static final CreativeTabs instance =
            new CreativeTabTAN(CreativeTabs.getNextID(), "tabToughAsNails");

    private static final String CANTEEN_VARIANT_KEY = "Variant";
    private static final String LEAF_TINT_KEY       = "LeafTint";

    // Flag read by ItemThermometer to render per-client live temp in Creative
    private static final String THERMO_DYNAMIC_KEY  = "ThermoDynamic";

    private CreativeTabTAN(int index, String label) {
        super(index, label);
    }

    @Override
    public Item getTabIconItem() {
        return TANItems.tan_icon;
    }

    /** Forge 1.7.10 calls this (note the misspelling). */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void displayAllReleventItems(List list) {

    	addBlockIfPresent(list, TANBlocks.thermoregulator);
    	addBlockIfPresent(list, TANBlocks.temperature_gauge);
    	
    	addBlockIfPresent(list, TANBlocks.rain_collector2 != null ? TANBlocks.rain_collector2 : TANBlocks.rain_collector);

    	addBlockIfPresent(list, TANBlocks.water_purifier);
        // 1) Thermometer – dynamic per client in Creative
        list.add(makeDynamicThermometer());

        // 2) Leaf armor (pre-tinted so icons are green immediately)
        list.add(preTintLeaf(new ItemStack(TANItems.leaf_helmet)));
        list.add(preTintLeaf(new ItemStack(TANItems.leaf_chestplate)));
        list.add(preTintLeaf(new ItemStack(TANItems.leaf_leggings)));
        list.add(preTintLeaf(new ItemStack(TANItems.leaf_boots)));

        // 3) Wool armor (pre-dyed to white so it’s “dyed by default”)
        list.add(preDyeWhite(new ItemStack(TANItems.wool_helmet)));
        list.add(preDyeWhite(new ItemStack(TANItems.wool_chestplate)));
        list.add(preDyeWhite(new ItemStack(TANItems.wool_leggings)));
        list.add(preDyeWhite(new ItemStack(TANItems.wool_boots)));

        // 4) Jelled Slime armor
        list.add(new ItemStack(TANItems.jelled_slime_helmet));
        list.add(new ItemStack(TANItems.jelled_slime_chestplate));
        list.add(new ItemStack(TANItems.jelled_slime_leggings));
        list.add(new ItemStack(TANItems.jelled_slime_boots));

        // 5) Foods (between armor and leather canteen)
        list.add(new ItemStack(TANItems.ice_cream));
        list.add(new ItemStack(TANItems.charc_os));

        // 9) Basic mats
        list.add(new ItemStack(TANItems.ice_cube));
        list.add(new ItemStack(TANItems.freeze_rod));
        list.add(new ItemStack(TANItems.freeze_powder));
        list.add(new ItemStack(TANItems.ice_charge));
        list.add(new ItemStack(TANItems.jelled_slime));

        // 6) Canteens (variant rows): empty, dirty, water, purified
        addCanteenRow(list, ItemCanteen.CanteenVariant.LEATHER);
        addCanteenRow(list, ItemCanteen.CanteenVariant.COPPER);
        addCanteenRow(list, ItemCanteen.CanteenVariant.IRON);
        addCanteenRow(list, ItemCanteen.CanteenVariant.GOLD);
        addCanteenRow(list, ItemCanteen.CanteenVariant.DIAMOND);
        addCanteenRow(list, ItemCanteen.CanteenVariant.NETHERITE);

        // 7) Water bottles (Dirty, Filtered, Purified)
        list.add(new ItemStack(TANItems.water_bottle, 1, 0)); // DIRTY
        list.add(new ItemStack(TANItems.water_bottle, 1, 1)); // FILTERED
        list.add(new ItemStack(TANItems.water_bottle, 1, 2)); // PURIFIED

        // 8) Juices
        list.add(juice(ItemFruitJuice.JuiceType.APPLE));
        list.add(juice(ItemFruitJuice.JuiceType.CACTUS));
        list.add(juice(ItemFruitJuice.JuiceType.CHORUS_FRUIT));
        list.add(juice(ItemFruitJuice.JuiceType.GLOW_BERRY));
        list.add(juice(ItemFruitJuice.JuiceType.MELON));
        list.add(juice(ItemFruitJuice.JuiceType.PUMPKIN));
        list.add(juice(ItemFruitJuice.JuiceType.SWEET_BERRY));

        list.add(juice(ItemFruitJuice.JuiceType.CARROT));
        list.add(juice(ItemFruitJuice.JuiceType.BEETROOT));
        list.add(juice(ItemFruitJuice.JuiceType.GLISTERING_MELON));
        list.add(juice(ItemFruitJuice.JuiceType.GOLDEN_CARROT));
        list.add(juice(ItemFruitJuice.JuiceType.GOLDEN_APPLE));
    }

    private static ItemStack juice(ItemFruitJuice.JuiceType t) {
        return new ItemStack(TANItems.fruit_juice, 1, t.ordinal());
    }

    /** Adds canteens (empty, dirty, water, purified) for the given variant. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addCanteenRow(List list, ItemCanteen.CanteenVariant variant) {
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 0), variant)); // Empty
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 1), variant)); // Dirty
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 3), variant)); // Water
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 2), variant)); // Purified
    }

    /** Write the canteen variant directly to NBT for creative listing. */
    private static ItemStack withVariant(ItemStack stack, ItemCanteen.CanteenVariant variant) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger(CANTEEN_VARIANT_KEY, variant.ordinal());
        return stack;
    }

    /** Pre-dye wool armor to white so it always counts as “dyed”. */
    private static ItemStack preDyeWhite(ItemStack st) {
        NBTTagCompound tag = st.getTagCompound();
        if (tag == null) { tag = new NBTTagCompound(); st.setTagCompound(tag); }
        NBTTagCompound disp = tag.getCompoundTag("display");
        if (!tag.hasKey("display")) tag.setTag("display", disp);
        disp.setInteger("color", 0xFFFFFF);
        return st;
    }

    /** Pre-tint leaf armor so icons show green immediately in the tab. */
    private static ItemStack preTintLeaf(ItemStack st) {
        int base = ColorizerFoliage.getFoliageColorBasic();
        if (st.getTagCompound() == null) st.setTagCompound(new NBTTagCompound());
        st.getTagCompound().setInteger(LEAF_TINT_KEY, base);
        return st;
    }

    /** Creative thermometer flagged to render from the local client's temperature live. */
    private static ItemStack makeDynamicThermometer() {
        ItemStack st = new ItemStack(TANItems.thermometer);
        if (st.getTagCompound() == null) st.setTagCompound(new NBTTagCompound());
        st.getTagCompound().setBoolean(THERMO_DYNAMIC_KEY, true);
        return st;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addBlockIfPresent(List list, net.minecraft.block.Block b) {
        if (b != null) {
            list.add(new ItemStack(Item.getItemFromBlock(b)));
        }
    }

}
