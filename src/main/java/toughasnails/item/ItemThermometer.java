package toughasnails.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureHandler;

/**
 * 1.7.10 back-port – renders 21-frame thermometer dial
 * (textures/items/thermometer_00.png … _20.png).
 *
 * IMPORTANT: We read the *current temperature level* from the handler,
 * not the debugger’s target (which often stays 0 if config disables temp
 * or no tick has run yet).
 */
public class ItemThermometer extends Item {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons; // 0 … 20

    public ItemThermometer() {
        setMaxStackSize(1);
        setUnlocalizedName("thermometer");
        setHasSubtypes(true); // damage = frame index
    }

    /* ------------------------------------------------------------ */
    /* Icons                                                        */
    /* ------------------------------------------------------------ */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        icons = new IIcon[21];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = reg.registerIcon(String.format("toughasnails:thermometer_%02d", i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int idx = MathHelper.clamp_int(meta, 0, icons.length - 1);
        return icons[idx];
    }

    /* ------------------------------------------------------------ */
    /* Behaviour                                                    */
    /* ------------------------------------------------------------ */
    /**
     * Update the displayed frame based on the *current* temperature level.
     * We change the damage on the **server** (it syncs to clients).
     */
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        if (world.isRemote || !(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;
        TemperatureHandler h = TemperatureHelper.getOrCreate(player);

        // Use the actual level, not debugger.targetTemperature
        int current = h.getTemperature().getRawValue();
        int total   = TemperatureScale.getScaleTotal();

        double norm = (total <= 0) ? 0.0 : Math.max(0.0, Math.min(1.0, current / (double) total));
        int frame   = (int) Math.round(norm * 20.0); // 0 … 20

        stack.setItemDamage(frame);

        // Persist updated handler so client-side HUD/items read consistent state
        TemperatureHelper.save(player, h);
    }

    /** Right-click prints the current temperature level (not the target). */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            TemperatureHandler h = TemperatureHelper.getOrCreate(player);
            int current = h.getTemperature().getRawValue();
            int total   = TemperatureScale.getScaleTotal();
            double pct  = (total <= 0) ? 0.0 : (100.0 * current / total);

            player.addChatMessage(new ChatComponentText(
                String.format("Temperature : %.0f%%  (%d / %d)", pct, current, total)
            ));

            // Save after reading (harmless if unchanged)
            TemperatureHelper.save(player, h);
        }
        return stack;
    }

    /* Hide from vanilla misc tab (TAN has its own tab) */
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) { /* none */ }

    /* Optional helper for overlays/HUDs */
    @SideOnly(Side.CLIENT)
    public static float getTemperatureNormalized(EntityPlayer p) {
        TemperatureHandler h = TemperatureHelper.getOrCreate(p);
        int current = h.getTemperature().getRawValue();
        int total   = TemperatureScale.getScaleTotal();
        if (total <= 0) return 0F;
        return (float) Math.max(0.0, Math.min(1.0, current / (double) total));
    }
}
