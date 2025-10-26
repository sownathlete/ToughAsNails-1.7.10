// File: toughasnails/item/ItemCanteen.java
package toughasnails.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.thirst.WaterType;
import toughasnails.thirst.ThirstHandler;

/**
 * Multi-variant canteen (LEATHER, COPPER, IRON, GOLD, DIAMOND, NETHERITE)
 * with 3 water types: DIRTY, FILTERED, CLEAN.
 *
 * Item damage layout:
 *   bits 0..1 : water type (0=empty, 1=DIRTY, 2=FILTERED, 3=CLEAN)
 *   bits 2..  : times used (0..capacity)
 *
 * Variant is stored in NBT key "Variant".
 *
 * Textures (in assets/toughasnails/textures/items/):
 *   empty_<variant>_canteen.png
 *   <variant>_dirty_water_canteen.png
 *   <variant>_purified_water_canteen.png
 *   <variant>_water_canteen.png
 */
public class ItemCanteen extends Item {

    /* ---------------------------- constants ---------------------------- */

    private static final String NBT_VARIANT = "Variant";

    /** Highest capacity among variants; used as item-wide maxDamage. */
    private static final int MAX_CAPACITY = 8;

    /** Variant list & per-variant sip capacities (increasing) */
    public static enum CanteenVariant {
        LEATHER(3),
        COPPER(4),
        IRON(5),
        GOLD(6),
        DIAMOND(7),
        NETHERITE(8);

        public final int capacity;
        CanteenVariant(int cap){ this.capacity = cap; }
        public String key(){ return name().toLowerCase(); }
        public static CanteenVariant fromId(int id){
            CanteenVariant[] v = values();
            return v[Math.max(0, Math.min(id, v.length - 1))];
        }
    }

    /* ---------------------------- state helpers ---------------------------- */

    /** 0 empty; 1 DIRTY; 2 FILTERED; 3 CLEAN */
    private static int stateIndex(ItemStack st){ return st.getItemDamage() & 3; }
    private static int stateIndex(int dmg){ return dmg & 3; }

    private static int getTimesUsed(ItemStack st){ return st.getItemDamage() >> 2; }

    private static void setTimesUsed(ItemStack st, int used){
        int state = stateIndex(st);
        st.setItemDamage((used << 2) | state);
    }

    private static void setState(ItemStack st, int state){
        int used = getTimesUsed(st);
        st.setItemDamage((used << 2) | (state & 3));
    }

    private static CanteenVariant getVariant(ItemStack st){
        if (st.hasTagCompound() && st.getTagCompound().hasKey(NBT_VARIANT)){
            return CanteenVariant.fromId(st.getTagCompound().getInteger(NBT_VARIANT));
        }
        return CanteenVariant.LEATHER;
    }
    private static void setVariant(ItemStack st, CanteenVariant v){
        if (!st.hasTagCompound()) st.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        st.getTagCompound().setInteger(NBT_VARIANT, v.ordinal());
    }

    private static WaterType getWaterType(ItemStack st){
        int s = stateIndex(st);
        return s == 0 ? null : WaterType.values()[s - 1]; // 1..3 map to enum ordinals 0..2
    }

    /* ---------------------------- ctor & base ---------------------------- */

    @SideOnly(Side.CLIENT)
    private IIcon[][] icons; // [variant][state] state: 0 empty, 1 dirty, 2 purified, 3 clean

    public ItemCanteen(){
        setMaxStackSize(1);
        // Set to the highest capacity so the vanilla damage clamp never blocks us;
        // we scale the durability bar ourselves per-variant.
        setMaxDamage(MAX_CAPACITY);
        setHasSubtypes(true);
        setNoRepair();
        setUnlocalizedName("canteen");
    }

    /* ---------------------------- textures/icons ---------------------------- */

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg){
        CanteenVariant[] vs = CanteenVariant.values();
        icons = new IIcon[vs.length][4];
        for (int vi = 0; vi < vs.length; vi++){
            String v = vs[vi].key();
            icons[vi][0] = reg.registerIcon("toughasnails:empty_" + v + "_canteen");
            icons[vi][1] = reg.registerIcon("toughasnails:" + v + "_dirty_water_canteen");
            icons[vi][2] = reg.registerIcon("toughasnails:" + v + "_purified_water_canteen");
            icons[vi][3] = reg.registerIcon("toughasnails:" + v + "_water_canteen");
        }
    }

    /** Inventory/creative path — must be NBT-aware for variant. */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack){
        int v = getVariant(stack).ordinal();
        int s = stateIndex(stack);
        if (icons == null) return super.getIconIndex(stack);
        if (v < 0 || v >= icons.length) v = 0;
        if (s < 0 || s >= 4) s = 0;
        return icons[v][s];
    }

    /** Sometimes called by old renderers; keep a decent fallback. */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int dmg){
        // No NBT here; show LEATHER of the right state to avoid missing textures.
        int s = stateIndex(dmg);
        if (icons == null) return super.getIconFromDamage(dmg);
        return icons[0][(s < 0 || s > 3) ? 0 : s];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass){
        return getIconIndex(stack);
    }

    /* ---------------------------- naming ---------------------------- */

    @Override
    public String getUnlocalizedName(ItemStack st){
        CanteenVariant var = getVariant(st);
        WaterType wt = getWaterType(st);

        String state;
        if (wt == null) state = "empty";
        else if (wt == WaterType.DIRTY) state = "dirty";
        else if (wt == WaterType.FILTERED) state = "purified";
        else /* CLEAN */ state = "water";

        return "item." + var.key() + "_" + state + "_canteen";
    }

    /* ---------------------------- use/drink ---------------------------- */

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        // Try filling first
        if (attemptCanteenFill(player, stack)) return stack;

        // Allow drinking even if not thirsty
        WaterType wt = getWaterType(stack);
        if (wt != null && getTimesUsed(stack) < getCapacity(stack)){
            player.setItemInUse(stack, getMaxItemUseDuration(stack));
        }
        return stack;
    }

    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player){
        WaterType wt = getWaterType(stack);
        if (wt != null && !world.isRemote){
            ThirstHandler th = ThirstHandler.get(player);
            if (th != null){
                th.addStats(wt.getThirst(), wt.getHydration());
                ThirstHandler.save(player, th);
            }

            if (world.rand.nextFloat() < wt.getPoisonChance()
                    && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)){
                player.addPotionEffect(new PotionEffect(TANPotions.thirst.id, 600, 0));
            }

            if (!player.capabilities.isCreativeMode){
                final int used = getTimesUsed(stack) + 1;
                final int cap  = getCapacity(stack);
                if (used >= cap){
                    // empty completely
                    stack.setItemDamage(0);
                }else{
                    // keep same state; bump usage
                    setTimesUsed(stack, used);
                }
            }
        }
        return stack;
    }

    /* Fill logic: water → DIRTY, cauldron (level>0) → CLEAN */
    private boolean attemptCanteenFill(EntityPlayer pl, ItemStack st){
        World w = pl.worldObj;
        MovingObjectPosition mop = getMovingObjectPositionFromPlayer(w, pl, true);
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) return false;

        int x = mop.blockX, y = mop.blockY, z = mop.blockZ;
        Block b = w.getBlock(x, y, z);

        if (b == Blocks.water || b == Blocks.flowing_water){
            // DIRTY
            setTimesUsed(st, 0);
            setState(st, 1);
            w.playSoundEffect(x + .5, y + .5, z + .5, "random.splash", .5F,
                    .8F + w.rand.nextFloat() * .4F);
            return true;
        }

        if (b == Blocks.cauldron && w.getBlockMetadata(x, y, z) > 0){
            if (!pl.capabilities.isCreativeMode){
                ((BlockCauldron) Blocks.cauldron).func_150024_a(
                        w, x, y, z, w.getBlockMetadata(x, y, z) - 1);
            }
            // CLEAN (water)
            setTimesUsed(st, 0);
            setState(st, 3);
            w.playSoundEffect(x + .5, y + .5, z + .5, "random.drink", .5F, 1F);
            return true;
        }
        return false;
    }

    /* ---------------------------- bar & timings ---------------------------- */

    @Override public int getMaxItemUseDuration(ItemStack s){ return 32; }
    @Override public EnumAction getItemUseAction(ItemStack s){ return EnumAction.drink; }

    /** Variant-specific capacity. */
    private static int getCapacity(ItemStack st){
        return getVariant(st).capacity;
    }

    /** Hide the bar until the first sip has been taken. */
    @Override
    public boolean showDurabilityBar(ItemStack stack){
        return getTimesUsed(stack) > 0 && getWaterType(stack) != null;
    }

    /** Scale the bar by the variant’s capacity. */
    @Override
    public double getDurabilityForDisplay(ItemStack st){
        int used = getTimesUsed(st);
        int cap  = getCapacity(st);
        if (cap <= 0) return 0.0;
        return (double) used / (double) cap;
    }

    /* ---------------------------- creative listing ---------------------------- */

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item item, CreativeTabs tab, List list){
        for (CanteenVariant v : CanteenVariant.values()){
            // Empty
            ItemStack empty = new ItemStack(item, 1, 0);
            setVariant(empty, v);
            list.add(empty);

            // Dirty (state=1)
            ItemStack dirty = new ItemStack(item, 1, 1);
            setVariant(dirty, v);
            list.add(dirty);

            // Water/Clean (state=3)
            ItemStack water = new ItemStack(item, 1, 3);
            setVariant(water, v);
            list.add(water);

            // Purified/Filtered (state=2)
            ItemStack purified = new ItemStack(item, 1, 2);
            setVariant(purified, v);
            list.add(purified);
        }
    }
}
