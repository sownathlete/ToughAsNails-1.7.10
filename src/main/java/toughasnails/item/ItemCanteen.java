package toughasnails.item;

import java.util.Random;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.thirst.WaterType;
import toughasnails.thirst.ThirstHandler;

/**
 * 1.7.10 back-port canteen – two textures:
 * <ul>
 *   <li><code>canteen_empty.png</code></li>
 *   <li><code>canteen_filled.png</code> (used for both dirty &amp; filtered)</li>
 * </ul>
 */
public class ItemCanteen extends Item {

    /* ------------------------------------------------- */
    /* icon pool                                          */
    /* ------------------------------------------------- */
    @SideOnly(Side.CLIENT) private IIcon[] icons; // 0-empty, 1-filled

    public ItemCanteen() {
        maxStackSize = 1;
        setMaxDamage(3);              // three sips
        setNoRepair();
        setUnlocalizedName("canteen");
    }

    /* ============================================================== */
    /*  Icon registration                                             */
    /* ============================================================== */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        icons = new IIcon[2];
        icons[0] = reg.registerIcon("toughasnails:canteen_empty");
        icons[1] = reg.registerIcon("toughasnails:canteen_filled");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int dmg) {
        return icons[(dmg & 3) == 0 ? 0 : 1];     // low 2-bits store water-type
    }

    /* ============================================================== */
    /*  Drinking                                                      */
    /* ============================================================== */
    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        WaterType wt = getWaterType(stack);

        if (wt != null && !world.isRemote) {
            ThirstHandler th = ThirstHandler.get(player);
            th.addStats(wt.getThirst(), wt.getHydration());
            ThirstHandler.save(player, th);

            if (world.rand.nextFloat() < wt.getPoisonChance()
                    && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST))
                player.addPotionEffect(new PotionEffect(TANPotions.thirst.id, 600, 0));

            if (!player.capabilities.isCreativeMode) {
                int used = getTimesUsed(stack) + 1;
                if (used >= getMaxDamage())
                    stack.setItemDamage(0);                       // fully empty
                else
                    stack.setItemDamage((used << 2) | (wt.ordinal() + 1));
            }
        }
        return stack;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (attemptCanteenFill(player, stack)) return stack;

        WaterType wt = getWaterType(stack);
        if (wt != null && getTimesUsed(stack) < getMaxDamage()) {
            ThirstHandler th = ThirstHandler.get(player);
            if (th != null && th.isThirsty())
                player.setItemInUse(stack, getMaxItemUseDuration(stack));
        }
        return stack;
    }

    /* fill from water/cauldron ------------------------------------ */
    private boolean attemptCanteenFill(EntityPlayer pl, ItemStack st) {
        World w = pl.worldObj;
        MovingObjectPosition mop = getMovingObjectPositionFromPlayer(w, pl, true);
        if (mop == null || mop.typeOfHit != MovingObjectType.BLOCK) return false;

        int x = mop.blockX, y = mop.blockY, z = mop.blockZ;
        Block b = w.getBlock(x, y, z);

        if (b == Blocks.water || b == Blocks.flowing_water) {
            st.setItemDamage(1);
            w.playSoundEffect(x + .5, y + .5, z + .5, "random.splash", .5F,
                              .8F + w.rand.nextFloat() * .4F);
            return true;
        }

        if (b == Blocks.cauldron && w.getBlockMetadata(x, y, z) > 0) {
            if (!pl.capabilities.isCreativeMode)
                ((BlockCauldron) Blocks.cauldron).func_150024_a(w, x, y, z,
                                                               w.getBlockMetadata(x, y, z) - 1);
            st.setItemDamage(1);
            w.playSoundEffect(x + .5, y + .5, z + .5, "random.drink", .5F, 1F);
            return true;
        }
        return false;
    }

    /* helpers ------------------------------------------------------- */
    private WaterType getWaterType(ItemStack st) {
        int t = st.getItemDamage() & 3;
        return t > 0 ? WaterType.values()[t - 1] : null;
    }
    private int getTimesUsed(ItemStack st) { return st.getItemDamage() >> 2; }

    /* vanilla hooks ------------------------------------------------- */
    @Override public int  getMaxItemUseDuration(ItemStack s){ return 32; }
    @Override public EnumAction getItemUseAction(ItemStack s){ return EnumAction.drink; }

    @Override
    public String getUnlocalizedName(ItemStack st) {
        WaterType wt = getWaterType(st);
        return wt == null ? "item.empty_canteen"
                          : "item." + wt.toString().toLowerCase() + "_water_canteen";
    }

    /* cosmetic durability bar (sips used) */
    @Override
    public double getDurabilityForDisplay(ItemStack st) {
        return (double) getTimesUsed(st) / getMaxDamage();
    }

    /* show only empty canteen in creative tab */
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));          // empty
    }
}
