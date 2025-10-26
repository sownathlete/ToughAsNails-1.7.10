package toughasnails.handler.health;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import toughasnails.api.HealthHelper;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.util.RenderUtils;

/**
 * Backported Tough As Nails Health Overlay for Forge 1.7.10.
 * Replaces GlStateManager with GL11 and adjusts event fields for compatibility.
 */
public class HealthOverlayHandler {

    public static final ResourceLocation OVERLAY = new ResourceLocation("toughasnails:textures/gui/overlay.png");
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Random random = new Random();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        // Only handle the HEALTH element
        if (event.type != RenderGameOverlayEvent.ElementType.HEALTH)
            return;

        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_LOWERED_STARTING_HEALTH))
            return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        PlayerControllerMP controller = mc.playerController;

        // Equivalent of gameIsSurvivalOrAdventure() → NOT creative
        if (!controller.isInCreativeMode()) {
            ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int width = res.getScaledWidth();
            int height = res.getScaledHeight();

            mc.getTextureManager().bindTexture(OVERLAY);
            int inactiveHearts = HealthHelper.getInactiveHearts(player);
            drawInactiveHearts(width, height, inactiveHearts);
        }
    }

    private void drawInactiveHearts(int width, int height, int inactiveHearts) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        int left = width / 2 - 91;
        int top = height - 39;

        for (int i = 0; i < inactiveHearts; ++i) {
            int activeOffset = 8 * (10 - inactiveHearts);
            int startX = left + i * 8 + activeOffset;
            int startY = top;
            RenderUtils.drawTexturedModalRect(startX, startY, 0, 43, 9, 9);
        }

        minecraft.getTextureManager().bindTexture(Gui.icons);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
}
