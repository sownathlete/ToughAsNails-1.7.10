// File: toughasnails/item/ItemTANWaterBottle.java
package toughasnails.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import toughasnails.api.TANPotions;                 // <- use your API potions directly
import toughasnails.api.thirst.IDrink;
import toughasnails.api.thirst.WaterType;
import toughasnails.thirst.ThirstHandler;

/**
 * Water bottles with three types: DIRTY, FILTERED, PURIFIED.
 *
 * Textures required (items/):
 *   toughasnails:dirty_water_bottle.png
 *   toughasnails:filtered_water_bottle.png
 *   toughasnails:purified_water_bottle.png
 *
 * Behavior:
 * - Always drinkable (even if not thirsty).
 * - Applies thirst/hydration from WaterType.
 * - Chance to apply TAN 'thirst' debuff for non-purified variants (if enabled on your side).
 * - Returns glass bottle after drinking (unless in creative).
 */
public class ItemTANWaterBottle extends Item {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons; // 0=DIRTY, 1=FILTERED, 2=PURIFIED

    public ItemTANWaterBottle() {
        setHasSubtypes(true);
        setMaxStackSize(1);
        setUnlocalizedName("water_bottle");
    }

    /* ----------------------- type helpers ------------------------ */

    public WaterBottleType getTypeFromMeta(int meta) {
        WaterBottleType[] vals = WaterBottleType.values();
        int i = Math.abs(meta) % vals.length;
        return vals[i];
    }

    @Override
    public String getUnlocalizedName(ItemStack st) {
        return "item." + getTypeFromMeta(st.getItemDamage()).toString() + "_water_bottle";
    }

    /* ----------------------- icons ------------------------------- */

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        icons = new IIcon[3];
        icons[0] = reg.registerIcon("toughasnails:dirty_water_bottle");
        icons[1] = reg.registerIcon("toughasnails:filtered_water_bottle");
        icons[2] = reg.registerIcon("toughasnails:purified_water_bottle");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        switch (getTypeFromMeta(meta)) {
            case DIRTY:     return icons[0];
            case FILTERED:  return icons[1];
            case PURIFIED:
            default:        return icons[2];
        }
    }

    /* ----------------------- creative menu ----------------------- */

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, WaterBottleType.DIRTY.ordinal()));
        list.add(new ItemStack(item, 1, WaterBottleType.FILTERED.ordinal()));
        list.add(new ItemStack(item, 1, WaterBottleType.PURIFIED.ordinal()));
    }

    /* ----------------------- consumption (always drinkable) ------ */

    @Override
    public int getMaxItemUseDuration(ItemStack stack) { return 32; }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.drink; }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Always allow drinking (no thirst check)
        player.setItemInUse(stack, getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            WaterBottleType t  = getTypeFromMeta(stack.getItemDamage());
            WaterType wt       = t.getWaterType();

            // Apply thirst/hydration
            ThirstHandler th = ThirstHandler.get(player);
            if (th != null) {
                th.addStats(wt.getThirst(), wt.getHydration());
                ThirstHandler.save(player, th);
            }

            // Apply 'thirst' debuff chance for non-purified water (if that potion exists)
            float p = wt.getPoisonChance();
            if (p > 0 && world.rand.nextFloat() < p && TANPotions.thirst != null) {
                player.addPotionEffect(new PotionEffect(TANPotions.thirst.id, 600, 0));
            }

            if (!player.capabilities.isCreativeMode) {
                // Consume and return a glass bottle
                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    return new ItemStack(Items.glass_bottle);
                } else {
                    if (!player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle))) {
                        player.dropPlayerItemWithRandomChoice(new ItemStack(Items.glass_bottle), false);
                    }
                }
            }
        }
        return stack;
    }

    /* ----------------------- drink data -------------------------- */

    public static enum WaterBottleType implements IDrink {
        DIRTY    (WaterType.DIRTY),
        FILTERED (WaterType.FILTERED),
        PURIFIED (WaterType.CLEAN);

        private final WaterType type;
        WaterBottleType(WaterType t){ this.type = t; }

        public WaterType getWaterType() { return type; }

        @Override public int   getThirst()       { return type.getThirst(); }
        @Override public float getHydration()    { return type.getHydration(); }
        @Override public float getPoisonChance() { return type.getPoisonChance(); }

        public String getName() { return name().toLowerCase(); }
        @Override public String toString(){ return getName(); }
    }
}
