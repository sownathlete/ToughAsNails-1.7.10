package toughasnails.util;

import net.minecraft.client.renderer.Tessellator;

/**
 * 1.7.10-friendly rendering helper.
 *
 * <p>The 1.9+ version used {@code VertexBuffer} and
 * {@code DefaultVertexFormats}. Those classes do not exist in
 * Minecraft 1.7.10, so we fall back to the classic
 * {@link Tessellator#addVertexWithUV} API.</p>
 */
public final class RenderUtils {

    /**
     * Draws a textured quad at screen-space coordinates (x,y) with the
     * given UV window from the bound texture.
     *
     * @param x         left screen X
     * @param y         top  screen Y
     * @param textureX  u-origin (pixels) in texture
     * @param textureY  v-origin (pixels) in texture
     * @param width     quad width  (pixels)
     * @param height    quad height (pixels)
     */
    public static void drawTexturedModalRect(int x, int y,
                                             int textureX, int textureY,
                                             int width, int height) {

        final float uScale = 1.0F / 256.0F;   // 16-bit atlas in 1.7
        final float vScale = 1.0F / 256.0F;

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        /* bottom-left */
        tess.addVertexWithUV(x,         y + height, 0,
                             (textureX)         * uScale,
                             (textureY + height)* vScale);

        /* bottom-right */
        tess.addVertexWithUV(x + width, y + height, 0,
                             (textureX + width) * uScale,
                             (textureY + height)* vScale);

        /* top-right */
        tess.addVertexWithUV(x + width, y,          0,
                             (textureX + width) * uScale,
                             (textureY)         * vScale);

        /* top-left */
        tess.addVertexWithUV(x,         y,          0,
                             (textureX)         * uScale,
                             (textureY)         * vScale);

        tess.draw();
    }

    private RenderUtils() {} // utility class
}
