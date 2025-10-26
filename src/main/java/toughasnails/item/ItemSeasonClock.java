package toughasnails.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toughasnails.api.season.SeasonHelper;
import toughasnails.season.SeasonTime;

/**
 * Season Clock – 24 frames: season_clock_00 … 23.
 * Uses item damage to point the dial, exactly like the vanilla clock.
 */
public class ItemSeasonClock extends Item {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;                // 0 … 23

    public ItemSeasonClock() {
        setMaxStackSize(1);
        setUnlocalizedName("season_clock");
        setHasSubtypes(true);
    }

    /* --------------------------------------------------------------------- */
    /* icon pool                                                             */
    /* --------------------------------------------------------------------- */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        icons = new IIcon[24];
        for (int i = 0; i < icons.length; i++) {
            String name = String.format("toughasnails:season_clock_%02d", i);
            icons[i] = reg.registerIcon(name);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int idx = MathHelper.clamp_int(meta, 0, icons.length - 1);
        return icons[idx];
    }

    /* --------------------------------------------------------------------- */
    /*  Frame update (client-side tick)                                      */
    /* --------------------------------------------------------------------- */
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        if (!world.isRemote) return;

        double rot = calcRotation(world, entity, stack.getItemDamage() / 24.0D);
        int frame  = MathHelper.floor_double(rot * 24.0D) & 23;   // 0 … 23
        stack.setItemDamage(frame);
    }

    /** vanilla-style smooth compass logic, adapted for seasons */
    private double calcRotation(World w, Entity e, double curRot) {

        /* who’s holding / displaying the item? */
        Entity holder = e != null ? e
                : (e instanceof EntityItemFrame ? e : null);
        if (holder == null) return curRot;

        double target;
        if (w.provider.isSurfaceWorld()) {
            int ticks = SeasonHelper.getSeasonData(w).getSeasonCycleTicks();
            target = (double) ticks / SeasonTime.TOTAL_CYCLE_TICKS;
        } else {
            target = w.rand.nextDouble();
        }

        /* smooth »rooster« algo like vanilla Clock */
        double delta = target - curRot;
        while (delta < -0.5D) delta += 1.0D;
        while (delta >= 0.5D) delta -= 1.0D;

        return (curRot + delta * 0.1D) % 1.0D;
    }
}
