package toughasnails.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

/**
 * Backported color-handler interface for 1.7.10.
 * In this version, color tinting is handled by getColorFromItemStack(int pass)
 * rather than IItemColor.
 */
public interface IColoredItem {

    /**
     * Returns the tint color (ARGB) for this item on the given render pass.
     * Called from Item#getColorFromItemStack(ItemStack, int).
     *
     * @param stack The item stack being rendered.
     * @param pass  The render pass (0 for base layer, 1+ for overlays).
     * @return The color as an integer in 0xRRGGBB or 0xAARRGGBB format.
     */
    int getColorFromItemStack(ItemStack stack, int pass);

    /**
     * Optional hook for items that use multiple icons or custom layers.
     * Implementers can use this to register variant icons for tinting.
     */
    void registerIcons(IIconRegister register);

    /**
     * Optional: return an alternate icon per render pass (for layered tint).
     */
    IIcon getIconFromPass(int pass);
}
