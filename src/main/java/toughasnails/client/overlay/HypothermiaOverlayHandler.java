package toughasnails.client.overlay;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import toughasnails.api.TANPotions;

/** Draws the hypothermia vignette in the PORTAL pass (Forge 1.7.10). */
@SideOnly(Side.CLIENT)
public final class HypothermiaOverlayHandler {

    private static final Logger LOG = LogManager.getLogger("TAN-HypoOverlay");
    private static final ResourceLocation TEX =
            new ResourceLocation("toughasnails:textures/misc/hypothermia_outline.png");

    @SubscribeEvent
    public void onOverlayPost(RenderGameOverlayEvent.Post e) {
        if (e.type != ElementType.PORTAL) return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen != null || mc.gameSettings.hideGUI) return;

        final EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        if (TANPotions.hypothermia == null) return;
        if (!player.isPotionActive(TANPotions.hypothermia)) return;

        // Alpha scales with amplifier
        int amp = player.getActivePotionEffect(TANPotions.hypothermia).getAmplifier();
        float alpha = Math.min(1.0F, 0.55F + amp * 0.15F);

        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();

        // GL state
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(1F, 1F, 1F, alpha);

        // draw full-screen textured quad
        try {
            mc.getTextureManager().bindTexture(TEX);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.addVertexWithUV(0,   h, -90, 0, 1);
            t.addVertexWithUV(w,   h, -90, 1, 1);
            t.addVertexWithUV(w,   0, -90, 1, 0);
            t.addVertexWithUV(0,   0, -90, 0, 0);
            t.draw();
        } catch (Throwable th) {
            LOG.warn("Failed to draw hypothermia overlay {}", TEX, th);
        }

        // restore GL
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
