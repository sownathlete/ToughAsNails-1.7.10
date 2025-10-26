// File: toughasnails/item/ItemThermometer.java
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
 * Server-authoritative thermometer:
 * - Icon frame (0..20) is updated on the SERVER (and syncs via item damage).
 * - Right click prints temperature from the SERVER so values aren't stuck at defaults.
 */
public class ItemThermometer extends Item {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons; // frames 0..20

    public ItemThermometer() {
        setMaxStackSize(1);
        setUnlocalizedName("thermometer");
        setHasSubtypes(true);
    }

    // ---------- Icons ----------

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

    // ---------- Runtime ----------

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        // Only do logic on the SERVER so we reflect the true server-side temperature.
        if (world.isRemote || !(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;
        TemperatureHandler h = TemperatureHelper.getOrCreate(player);

        int current = h.getTemperature().getRawValue();
        int total   = TemperatureScale.getScaleTotal();

        double norm = (total <= 0) ? 0.0 : Math.max(0.0, Math.min(1.0, current / (double) total));
        int frame   = (int)Math.round(norm * 20.0); // map to 0..20

        stack.setItemDamage(frame);                 // syncs to client automatically
        TemperatureHelper.save(player, h);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Print from the SERVER so numbers aren't stuck at client defaults.
        if (!world.isRemote) {
            TemperatureHandler h = TemperatureHelper.getOrCreate(player);

            final int current = h.getTemperature().getRawValue();
            final int total   = TemperatureScale.getScaleTotal();
            final double pct  = (total <= 0) ? 0.0 : (100.0 * current / total);

            // Debug data (computed on server by the handler)
            final int target = h.debugger.targetTemperature;
            final int rate   = h.debugger.changeTicks;
            final int timer  = h.debugger.temperatureTimer;

            player.addChatMessage(new ChatComponentText(String.format(
                "Temperature : %.0f%%  (%d / %d)  target=%d  rateTicks=%d  timer=%d",
                pct, current, total, target, rate, timer
            )));

            TemperatureHelper.save(player, h);
        }
        return stack;
    }

    // Show ONE entry in the Creative tab so it appears there.
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        // Only add to our own tab (prevents clutter in other tabs)
        if (tab == this.getCreativeTab()) {
            list.add(new ItemStack(item, 1, 0)); // representative icon (frame 0)
        }
    }
}
