// File: toughasnails/util/inventory/CreativeTabTAN.java
package toughasnails.util.inventory;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import toughasnails.api.item.TANItems;
import toughasnails.item.ItemCanteen;
import toughasnails.item.ItemFruitJuice;

/**
 * Tough As Nails creative tab with a fixed, explicit ordering.
 *
 * Order:
 *  1) Thermometer
 *  2) Leaf armor (helm, chest, legs, boots)
 *  3) Wool armor (helm, chest, legs, boots)
 *  4) Jelled Slime armor (helm, chest, legs, boots)
 *  5) Foods: Ice Cream, Charc-O's
 *  6) Cold/Ice materials: Ice Cube, Freeze Rod, Freeze Powder, Ice Charge, Jelled Slime
 *  7) Canteens per variant (empty, dirty, water, purified) for:
 *     leather, copper, iron, gold, diamond, netherite
 *  8) Water bottles: Dirty, Filtered, Purified
 *  9) Juices (in this order):
 *     apple, cactus, chorus fruit, glow berry, melon, pumpkin, sweet berry,
 *     carrot, beetroot, glistering melon, golden carrot, golden apple
 */
public final class CreativeTabTAN extends CreativeTabs {

    public static final CreativeTabs instance =
            new CreativeTabTAN(CreativeTabs.getNextID(), "tabToughAsNails");

    private static final String CANTEEN_VARIANT_KEY = "Variant";

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

        // 1) Thermometer
        list.add(new ItemStack(TANItems.thermometer));

        // 2) Leaf armor
        list.add(new ItemStack(TANItems.leaf_helmet));
        list.add(new ItemStack(TANItems.leaf_chestplate));
        list.add(new ItemStack(TANItems.leaf_leggings));
        list.add(new ItemStack(TANItems.leaf_boots));

        // 3) Wool armor
        list.add(new ItemStack(TANItems.wool_helmet));
        list.add(new ItemStack(TANItems.wool_chestplate));
        list.add(new ItemStack(TANItems.wool_leggings));
        list.add(new ItemStack(TANItems.wool_boots));

        // 4) Jelled Slime armor (after wool)
        list.add(new ItemStack(TANItems.jelled_slime_helmet));
        list.add(new ItemStack(TANItems.jelled_slime_chestplate));
        list.add(new ItemStack(TANItems.jelled_slime_leggings));
        list.add(new ItemStack(TANItems.jelled_slime_boots));

        // 5) Foods (between armor and materials)
        list.add(new ItemStack(TANItems.ice_cream));
        list.add(new ItemStack(TANItems.charc_os));

        // 6) Cold/Ice materials
        list.add(new ItemStack(TANItems.ice_cube));
        list.add(new ItemStack(TANItems.freeze_rod));
        list.add(new ItemStack(TANItems.freeze_powder));
        list.add(new ItemStack(TANItems.ice_charge));
        list.add(new ItemStack(TANItems.jelled_slime));

        // 7) Canteens (variant rows): empty, dirty, water, purified
        addCanteenRow(list, toughasnails.item.ItemCanteen.CanteenVariant.LEATHER);
        addCanteenRow(list, toughasnails.item.ItemCanteen.CanteenVariant.COPPER);
        addCanteenRow(list, toughasnails.item.ItemCanteen.CanteenVariant.IRON);
        addCanteenRow(list, toughasnails.item.ItemCanteen.CanteenVariant.GOLD);
        addCanteenRow(list, toughasnails.item.ItemCanteen.CanteenVariant.DIAMOND);
        addCanteenRow(list, toughasnails.item.ItemCanteen.CanteenVariant.NETHERITE);

        // 8) Water bottles (Dirty, Filtered, Purified)
        list.add(new ItemStack(TANItems.water_bottle, 1, 0)); // DIRTY
        list.add(new ItemStack(TANItems.water_bottle, 1, 1)); // FILTERED
        list.add(new ItemStack(TANItems.water_bottle, 1, 2)); // PURIFIED

        // 9) Juices
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
        // Empty (state = 0)
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 0), variant));

        // Dirty (state = 1)
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 1), variant));

        // Water / Clean (state = 3)
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 3), variant));

        // Purified / Filtered (state = 2)
        list.add(withVariant(new ItemStack(TANItems.canteen, 1, 2), variant));
    }

    /** Write the canteen variant directly to NBT for creative listing. */
    private static ItemStack withVariant(ItemStack stack, ItemCanteen.CanteenVariant variant) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger(CANTEEN_VARIANT_KEY, variant.ordinal());
        return stack;
    }
}
