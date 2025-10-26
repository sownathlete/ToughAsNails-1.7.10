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
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import toughasnails.api.thirst.IDrink;
import toughasnails.thirst.ThirstHandler;

/**
 * 1.7.10 back-port – registers one 16×16 icon per juice flavour:
 * toughasnails:textures/items/<type>_juice.png  (e.g., apple_juice.png)
 *
 * Behavior:
 * - Always drinkable (even if not thirsty).
 * - Consumes the juice and returns a glass bottle (unless in creative).
 */
public class ItemFruitJuice extends Item {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemFruitJuice() {
        setHasSubtypes(true);
        setMaxStackSize(1);
        setUnlocalizedName("fruit_juice");
    }

    /** quick helper → enum from meta (clamped) */
    public JuiceType getTypeFromMeta(int meta) {
        int i = MathHelper.clamp_int(meta, 0, JuiceType.values().length - 1);
        return JuiceType.values()[i];
    }

    /* ---------- names ---------- */
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        // Keeps the lang key pattern used previously: item.juice_<type>
        return "item.juice_" + getTypeFromMeta(stack.getItemDamage()).getName();
    }

    /* ---------- creative-tab listing ---------- */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < JuiceType.values().length; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    /* ---------- glint for golden / glistering variants ---------- */
    @Override
    public boolean hasEffect(ItemStack stack) {
        JuiceType t = getTypeFromMeta(stack.getItemDamage());
        return t == JuiceType.GLISTERING_MELON ||
               t == JuiceType.GOLDEN_APPLE   ||
               t == JuiceType.GOLDEN_CARROT;
    }

    /* ---------- icons ---------- */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        JuiceType[] vals = JuiceType.values();
        icons = new IIcon[vals.length];

        for (int i = 0; i < vals.length; i++) {
            // "<type>_juice" (e.g., apple_juice)
            String tex = "toughasnails:" + vals[i].getName() + "_juice";
            icons[i] = reg.registerIcon(tex);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int i = MathHelper.clamp_int(meta, 0, icons.length - 1);
        return icons[i];
    }

    /* ---------- use/drink behavior (always drinkable) ---------- */

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
            final JuiceType type = getTypeFromMeta(stack.getItemDamage());

            // Apply thirst/hydration (no poison on juices)
            ThirstHandler th = ThirstHandler.get(player);
            if (th != null) {
                th.addStats(type.getThirst(), type.getHydration());
                ThirstHandler.save(player, th);
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

    /* ============================================================ */
    /*  Flavour enum                                                */
    /* ============================================================ */
    public static enum JuiceType implements IDrink {
        APPLE            ( 8, 0.8f),
        BEETROOT         (10, 0.8f),
        CACTUS           ( 9, 0.2f),
        CARROT           ( 8, 0.6f),
        CHORUS_FRUIT     (12, 0.6f),
        GLISTERING_MELON (16, 1.0f),
        GOLDEN_APPLE     (20, 1.2f),
        GOLDEN_CARROT    (14, 1.0f),
        MELON            ( 8, 0.5f),
        PUMPKIN          ( 7, 0.7f),
        GLOW_BERRY       ( 9, 0.9f),  // new
        SWEET_BERRY      ( 8, 0.7f);  // new

        private final int   thirst;
        private final float hydration;

        JuiceType(int t, float h){ thirst = t; hydration = h; }

        @Override public int   getThirst()        { return thirst;    }
        @Override public float getHydration()     { return hydration; }
        @Override public float getPoisonChance()  { return 0;         }
        public  String getName()                  { return name().toLowerCase(); }
        @Override public String toString()        { return getName(); }
    }
}
