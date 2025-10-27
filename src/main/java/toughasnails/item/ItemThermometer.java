// File: toughasnails/item/ItemThermometer.java
package toughasnails.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureHandler;

/**
 * Thermometer behavior:
 * - Inventory/held items: server updates an NBT frame (no damage used) -> no re-equip/name flicker.
 * - Creative-tab sample: renders in "dynamic" mode using the local client's temperature,
 *   so each player sees live, per-frame updates even in the creative list.
 */
public class ItemThermometer extends Item {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons; // frames 0..20

    /** NBT keys */
    private static final String NBT_FRAME   = "ThermoFrame";   // stored frame (0..20)
    private static final String NBT_DYNAMIC = "ThermoDynamic"; // true => render from client temp live

    /** Default neutral frame when nothing else is available. */
    private static final int DEFAULT_FRAME = 10;

    public ItemThermometer() {
        setMaxStackSize(1);
        setUnlocalizedName("thermometer");
        setHasSubtypes(false); // we don't use damage for frames
    }

    /* ---------------- Icons ---------------- */

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        icons = new IIcon[21];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = reg.registerIcon(String.format("toughasnails:thermometer_%02d", i));
        }
        this.itemIcon = icons[Math.min(DEFAULT_FRAME, icons.length - 1)];
    }

    /**
     * Core render hook. We do **not** mutate NBT here; just decide which frame to show.
     * - If stack is marked ThermoDynamic=true OR has no NBT (e.g., creative sample),
     *   we compute the frame from the local client player's temperature so it updates live.
     * - Otherwise, we use the stored NBT frame (server-authoritative for inventory items).
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        int idx;

        boolean dynamic = isDynamic(stack);
        if (dynamic) {
            idx = frameFromClientPlayer();
        } else {
            idx = getStoredFrame(stack);
        }

        if (icons == null) return super.getIconIndex(stack);
        idx = MathHelper.clamp_int(idx, 0, icons.length - 1);
        return icons[idx];
    }

    /* Also handle every other render path (equipped hand / entity renders) */

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return getIconIndex(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        return (icons == null) ? super.getIconFromDamage(damage)
                               : icons[Math.min(DEFAULT_FRAME, icons.length - 1)];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        return getIconFromDamage(damage);
    }

    /* ---------------- Runtime (server authoritative for inventory items) ---------------- */

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        // CLIENT: touch the icon when held/selected so the hand view refreshes immediately
        if (world.isRemote) {
            if (held || (entity instanceof EntityPlayer && ((EntityPlayer) entity).inventory.currentItem == slot)) {
                getIconIndex(stack); // no state change, ensures latest icon path is used this frame
            }
            return;
        }

        // SERVER: compute & store the authoritative frame, and mark as non-dynamic
        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;
        TemperatureHandler h = TemperatureHelper.getOrCreate(player);

        int current = h.getTemperature().getRawValue();
        int total   = TemperatureScale.getScaleTotal();
        double norm = (total <= 0) ? 0.0 : Math.max(0.0, Math.min(1.0, current / (double) total));
        int frame   = MathHelper.clamp_int((int)Math.round(norm * 20.0), 0, 20);

        ensureTag(stack).setBoolean(NBT_DYNAMIC, false);

        if (frame != getStoredFrame(stack)) {
            setStoredFrame(stack, frame);

            // Push the change to client when in a player inventory/container.
            if (player instanceof EntityPlayerMP) {
                Container c = ((EntityPlayerMP) player).inventoryContainer;
                if (c != null) c.detectAndSendChanges();
            }
        }

        TemperatureHelper.save(player, h);
    }

    /* ---------------- Creative listing ---------------- */

    /**
     * One entry in our tab, explicitly marked dynamic so it renders from *this client's*
     * temperature and updates live while the Creative tab is open.
     */
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        if (tab == this.getCreativeTab()) {
            ItemStack sample = new ItemStack(item, 1, 0);
            ensureTag(sample).setBoolean(NBT_DYNAMIC, true);
            // no NBT_FRAME set -> strictly dynamic
            list.add(sample);
        }
    }

    /* ---------------- Client helpers ---------------- */

    @SideOnly(Side.CLIENT)
    private static int frameFromClientPlayer() {
        try {
            EntityPlayer p = Minecraft.getMinecraft().thePlayer;
            if (p == null) return DEFAULT_FRAME;

            TemperatureHandler h = TemperatureHelper.getOrCreate(p);
            if (h == null) return DEFAULT_FRAME;

            int current = h.getTemperature().getRawValue();
            int total   = TemperatureScale.getScaleTotal();
            if (total <= 0) return DEFAULT_FRAME;

            double norm = Math.max(0.0, Math.min(1.0, current / (double) total));
            return MathHelper.clamp_int((int)Math.round(norm * 20.0), 0, 20);
        } catch (Throwable t) {
            return DEFAULT_FRAME;
        }
    }

    /* ---------------- NBT helpers ---------------- */

    private static net.minecraft.nbt.NBTTagCompound ensureTag(ItemStack st) {
        if (!st.hasTagCompound()) {
            st.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        }
        return st.getTagCompound();
    }

    private static int getStoredFrame(ItemStack st) {
        if (st == null || !st.hasTagCompound() || !st.getTagCompound().hasKey(NBT_FRAME)) {
            return DEFAULT_FRAME;
        }
        return MathHelper.clamp_int(st.getTagCompound().getInteger(NBT_FRAME), 0, 20);
    }

    private static void setStoredFrame(ItemStack st, int frame) {
        ensureTag(st).setInteger(NBT_FRAME, MathHelper.clamp_int(frame, 0, 20));
    }

    private static boolean isDynamic(ItemStack st) {
        if (st == null) return false;
        if (!st.hasTagCompound()) {
            // No NBT at all? Treat as dynamic (this covers the client-built creative list).
            return true;
        }
        return st.getTagCompound().getBoolean(NBT_DYNAMIC);
    }
}
