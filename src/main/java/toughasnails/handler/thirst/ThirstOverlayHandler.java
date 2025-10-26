package toughasnails.handler.thirst;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.TANPotions;
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

    @SubscribeEvent
    public void onPreRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.AIR && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, -10.0F, 0.0F);
        }
    }

    @SubscribeEvent
    public void onPostRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) return;

        // 1.7.10-style temperature/thirst retrieval
        ThirstHandler thirstStats = ThirstHandler.get(player);
        if (thirstStats == null) return;

        int thirstLevel = thirstStats.getThirst();
        float thirstHydration = thirstStats.getHydration();

        random.setSeed(updateCounter * 312871L);

        if (event.type == RenderGameOverlayEvent.ElementType.AIR && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) {
            GL11.glPopMatrix();
        } else if (event.type == RenderGameOverlayEvent.ElementType.EXPERIENCE && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_THIRST)) {
            TextureManager tm = minecraft.getTextureManager();
            tm.bindTexture(OVERLAY);
            if (minecraft.playerController.gameIsSurvivalOrAdventure()) {
                drawThirst(width, height, thirstLevel, thirstHydration);
            }
        }
    }

    private void drawThirst(int width, int height, int thirstLevel, float thirstHydrationLevel) {
        int left = width / 2 + 91;
        int top = height - 49;

        for (int i = 0; i < 10; ++i) {
            int dropletHalf = i * 2 + 1;
            int iconIndex = 0;
            int backgroundOffset = 0;
            int startX = left - i * 8 - 9;
            int startY = top;

            if (minecraft.thePlayer.isPotionActive(TANPotions.thirst)) {
                iconIndex += 4;
                backgroundOffset += 117;
            }

            if (thirstHydrationLevel <= 0.0F && updateCounter % (thirstLevel * 3 + 1) == 0) {
                startY = top + (random.nextInt(3) - 1);
            }

            RenderUtils.drawTexturedModalRect(startX, startY, backgroundOffset, 16, 9, 9);

            if (thirstLevel > dropletHalf) {
                RenderUtils.drawTexturedModalRect(startX, startY, (iconIndex + 4) * 9, 16, 9, 9);
            }

            if (thirstLevel == dropletHalf) {
                RenderUtils.drawTexturedModalRect(startX, startY, (iconIndex + 5) * 9, 16, 9, 9);
            }
        }
    }
}
