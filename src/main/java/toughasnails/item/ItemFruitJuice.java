package toughasnails.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.client.renderer.texture.IIconRegister;
import toughasnails.api.thirst.IDrink;

/**
 * 1.7.10 back-port – registers one 16×16 icon per juice flavour:
 * toughasnails:textures/items/juice_<type>.png
 */
public class ItemFruitJuice extends Item {

    /* ------------------------------------------------------------------ */
    public ItemFruitJuice() {
        setHasSubtypes(true);
        setMaxStackSize(1);
    }

    /* ------------------------------------------------------------------ */
    /** quick helper → enum from meta (clamped) */
    public JuiceType getTypeFromMeta(int meta) {
        int i = MathHelper.clamp_int(meta, 0, JuiceType.values().length - 1);
        return JuiceType.values()[i];
    }

    /* ---------- names ---------- */
    @Override
    public String getUnlocalizedName(ItemStack stack) {
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

    /* ============================================================ */
    /*  Icon handling (1.7.10)                                      */
    /* ============================================================ */
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        JuiceType[] vals = JuiceType.values();
        icons = new IIcon[vals.length];

        for (int i = 0; i < vals.length; i++) {
            String tex = "toughasnails:juice_" + vals[i].getName(); // <modid>:filename
            icons[i] = reg.registerIcon(tex);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int i = MathHelper.clamp_int(meta, 0, icons.length - 1);
        return icons[i];
    }

    /* ============================================================ */
    /*  Flavour enum (unchanged)                                    */
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
        PUMPKIN          ( 7, 0.7f);

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
