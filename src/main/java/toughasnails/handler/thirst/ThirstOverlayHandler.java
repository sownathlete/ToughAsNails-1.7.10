// File: toughasnails/handler/thirst/ThirstOverlayHandler.java
package toughasnails.handler.thirst;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import toughasnails.api.TANPotions;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.thirst.ThirstHandler;
import toughasnails.util.RenderUtils;

@SideOnly(Side.CLIENT)
public class ThirstOverlayHandler {

    public static final ResourceLocation OVERLAY = new ResourceLocation("toughasnails:textures/gui/overlay.png");

    private final Random random = new Random();
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private int updateCounter;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !minecraft.isGamePaused()) {
            ++updateCounter;
        }
    }

    /** Draw after vanilla bars so we can position relative to them. */
    @SubscribeEvent
    public void onPostRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) return;

        final Minecraft mc = Minecraft.getMinecraft();
        final EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) return;
        if (!mc.playerController.gameIsSurvivalOrAdventure()) return;

        // Read client-side copy (kept in sync by our server messages)
        ThirstHandler thirstStats = ThirstHandler.get(player);
        if (thirstStats == null) return;

        int thirstLevel = thirstStats.getThirst();
        float thirstHydration = thirstStats.getHydration();
        random.setSeed(updateCounter * 312871L);

        // Screen size
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        // ---- GL state guard ----
        TextureManager tm = mc.getTextureManager();
        tm.bindTexture(OVERLAY);

        GL11.glPushMatrix();
        try {
            GL11.glColor4f(1F, 1F, 1F, 1F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            drawThirst(width, height, thirstLevel, thirstHydration);
        } finally {
            // Restore state so other overlays aren’t affected (prevents black boxes)
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            GL11.glPopMatrix();
        }
    }

    private void drawThirst(int width, int height, int thirstLevel, float thirstHydrationLevel) {
        final int left = width / 2 + 91;
        final int top  = height - 49;

        final boolean isDehydrated = (thirstHydrationLevel <= 0.0F);
        final boolean hasThirstEffect = minecraft.thePlayer.isPotionActive(TANPotions.thirst);

        for (int i = 0; i < 10; ++i) {
            int dropletHalf = i * 2 + 1;
            int iconIndex = 0;       // 0.. overlay page X
            int bgU = 0;             // background U on overlay
            int x = left - i * 8 - 9;
            int y = top;

            if (hasThirstEffect) {
                iconIndex += 4;      // use “thirst” (blue) sheet for filled/half
                bgU += 117;          // and its matching background
            }

            // Jitter only when hydration is 0; compute fresh each frame so it doesn’t “stick”
            if (isDehydrated && ((updateCounter + i) % Math.max(1, thirstLevel * 3 + 1) == 0)) {
                y = top + (random.nextInt(3) - 1);
            }

            // background drop
            RenderUtils.drawTexturedModalRect(x, y, bgU, 16, 9, 9);

            // filled/half
            if (thirstLevel > dropletHalf) {
                RenderUtils.drawTexturedModalRect(x, y, (iconIndex + 4) * 9, 16, 9, 9);
            } else if (thirstLevel == dropletHalf) {
                RenderUtils.drawTexturedModalRect(x, y, (iconIndex + 5) * 9, 16, 9, 9);
            }
        }
    }
}
