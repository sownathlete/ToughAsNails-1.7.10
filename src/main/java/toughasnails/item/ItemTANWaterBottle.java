package toughasnails.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import toughasnails.api.item.ItemDrink;
import toughasnails.api.thirst.IDrink;
import toughasnails.api.thirst.WaterType;

/**
 * Dirty / Filtered water bottles with icon switching.
 *
 * Textures needed:
 *   dirty_water_bottle.png
 *   filtered_water_bottle.png
 */
public class ItemTANWaterBottle extends ItemDrink<ItemTANWaterBottle.WaterBottleType> {

    @SideOnly(Side.CLIENT) private IIcon[] icons;   // 0-DIRTY, 1-FILTERED

    /* ----------------------- type helpers ------------------------ */
    @Override
    public WaterBottleType getTypeFromMeta(int meta) {
        return WaterBottleType.values()[meta % WaterBottleType.values().length];
    }

    @Override
    public String getUnlocalizedName(ItemStack st) {
        return "item." + getTypeFromMeta(st.getItemDamage()).toString() + "_water_bottle";
    }

    /* ----------------------- icons ------------------------------- */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        icons = new IIcon[2];
        icons[0] = reg.registerIcon("toughasnails:dirty_water_bottle");
        icons[1] = reg.registerIcon("toughasnails:filtered_water_bottle");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int idx = getTypeFromMeta(meta) == WaterBottleType.DIRTY ? 0 : 1;
        return icons[idx];
    }

    /* creative menu ------------------------------------------------ */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (WaterBottleType t : WaterBottleType.values())
            list.add(new ItemStack(item, 1, t.ordinal()));
    }

    /* drink data --------------------------------------------------- */
    public static enum WaterBottleType implements IDrink {
        DIRTY(WaterType.DIRTY),
        FILTERED(WaterType.FILTERED);

        private final WaterType type;
        WaterBottleType(WaterType t){ this.type = t; }

        @Override public int   getThirst()       { return type.getThirst(); }
        @Override public float getHydration()    { return type.getHydration(); }
        @Override public float getPoisonChance() { return type.getPoisonChance(); }

        public String getName() { return name().toLowerCase(); }
        @Override public String toString(){ return getName(); }
    }
}
