package toughasnails.handler.temperature;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureHandler;
import toughasnails.util.RenderUtils;

@SideOnly(Side.CLIENT)
public class TemperatureOverlayHandler {

    public static final ResourceLocation OVERLAY       = new ResourceLocation("toughasnails:textures/gui/overlay.png");
    public static final ResourceLocation ICE_VIGNETTE  = new ResourceLocation("toughasnails:textures/gui/ice_vignette.png");
    public static final ResourceLocation FIRE_VIGNETTE = new ResourceLocation("toughasnails:textures/gui/fire_vignette.png");

    private final Random random = new Random();
    private final Minecraft minecraft = Minecraft.getMinecraft();

    private int updateCounter;
    private FlashType flashType = FlashType.INCREASE;
    private int flashCounter = -1;
    private int prevTemperatureLevel = -1;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !minecraft.isGamePaused()) {
            ++updateCounter;
        }
    }

    @SubscribeEvent
    public void onPostRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (!SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) return;

        // 1.7.10-style access
        TemperatureHandler temperatureStats = TemperatureHandler.get(player);
        if (temperatureStats == null) return;

        Temperature temperature = temperatureStats.getTemperature();

        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int width  = res.getScaledWidth();
        int height = res.getScaledHeight();

        // Draw the vignette for ALL (guaranteed) so it always appears
        if (event.type == RenderGameOverlayEvent.ElementType.ALL && !player.capabilities.isCreativeMode) {
            drawTemperatureVignette(width, height, temperature);
        }

        // Draw the icon on EXPERIENCE (kept original placement)
        if (event.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            minecraft.getTextureManager().bindTexture(OVERLAY);
            if (minecraft.playerController.gameIsSurvivalOrAdventure()) {
                drawTemperatureIcon(width, height, temperature);
            }
        }
    }

    /* ---------------- Icon (unchanged behaviour) ---------------- */

    private void drawTemperatureIcon(int width, int height, Temperature temperature) {
        int left = width / 2 - 8;
        int top  = height - 52;

        TemperatureScale.TemperatureRange range = temperature.getRange();

        // Small shake in extreme ranges
        if (range == TemperatureScale.TemperatureRange.ICY || range == TemperatureScale.TemperatureRange.HOT) {
            float shakeDelta = (range == TemperatureScale.TemperatureRange.ICY)
                    ? temperature.getRangeDelta(true)
                    : temperature.getRangeDelta(false);
            top  += (random.nextInt(3) - 1) * Math.min(shakeDelta * 3.0F, 1.0F);
            left += (random.nextInt(3) - 1) * Math.min(shakeDelta * 1.5F, 1.0F);
        }

        int tempLevel = temperature.getRawValue();
        if (prevTemperatureLevel == -1) prevTemperatureLevel = tempLevel;

        if (tempLevel > prevTemperatureLevel) {
            flashCounter = updateCounter + 16;
            flashType = FlashType.INCREASE;
        } else if (tempLevel < prevTemperatureLevel) {
            flashCounter = updateCounter + 16;
            flashType = FlashType.DECREASE;
        }
        prevTemperatureLevel = tempLevel;

        TemperatureIcon icon = getTemperatureIcon(tempLevel);
        int updateDelta = flashCounter - updateCounter;

        RenderUtils.drawTexturedModalRect(left, top, 16 * icon.backgroundIndex, 0, 16, 16);
        RenderUtils.drawTexturedModalRect(left, top, 16 * icon.foregroundIndex, 0, 16, 16);

        if (icon == TemperatureIcon.BALL) {
            renderColouredBall(left, top, temperature, 0);
        }

        if (flashCounter > updateCounter) {
            if (updateDelta > 6 && (updateDelta / 3) % 2 == 1) {
                RenderUtils.drawTexturedModalRect(left, top, 16 * (icon.backgroundIndex + flashType.backgroundShift), 0, 16, 16);
                RenderUtils.drawTexturedModalRect(left, top, 16 * (icon.foregroundIndex + flashType.foregroundShift), 0, 16, 16);
                if (icon == TemperatureIcon.BALL) {
                    renderColouredBall(left, top, temperature, 2);
                }
            }

            GL11.glPushMatrix();
            if (flashType == FlashType.INCREASE) {
                GL11.glTranslatef(left + 16.0F, top + 16.0F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            } else {
                GL11.glTranslatef(left, top, 0.0F);
            }
            RenderUtils.drawTexturedModalRect(0, 0, 16 * (16 - updateDelta), 240, 16, 16);
            GL11.glPopMatrix();
        }
    }

    private void renderColouredBall(int x, int y, Temperature temperature, int textureShift) {
        TemperatureScale.TemperatureRange range = temperature.getRange();
        float delta = (range == TemperatureScale.TemperatureRange.COOL)
                ? temperature.getRangeDelta(true)
                : temperature.getRangeDelta(false);

        if (range != TemperatureScale.TemperatureRange.MILD) {
            GL11.glPushMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, delta);
            RenderUtils.drawTexturedModalRect(x, y, 16 * ((range == TemperatureScale.TemperatureRange.COOL ? 8 : 9) + textureShift), 16, 16, 16);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            GL11.glPopMatrix();
        }
    }

    private static TemperatureIcon getTemperatureIcon(int scalePos) {
        if (scalePos < 0 || scalePos > TemperatureScale.getScaleTotal())
            return TemperatureIcon.BALL;
        for (TemperatureIcon icon : TemperatureIcon.values()) {
            if (TemperatureScale.isScalePosInRange(scalePos, icon.startRange, icon.endRange))
                return icon;
        }
        return TemperatureIcon.BALL;
    }

    private static enum FlashType {
        INCREASE(3, 3), DECREASE(3, 3);
        public final int backgroundShift, foregroundShift;
        private FlashType(int bg, int fg) { this.backgroundShift = bg; this.foregroundShift = fg; }
    }

    private static enum TemperatureIcon {
        SNOWFLAKE(0, 9, TemperatureScale.TemperatureRange.ICY),
        BALL     (1,10, TemperatureScale.TemperatureRange.COOL, TemperatureScale.TemperatureRange.WARM),
        FIRE     (2,11, TemperatureScale.TemperatureRange.HOT);

        public final int backgroundIndex, foregroundIndex;
        public final TemperatureScale.TemperatureRange startRange, endRange;

        private TemperatureIcon(int bg, int fg, TemperatureScale.TemperatureRange start, TemperatureScale.TemperatureRange end) {
            this.backgroundIndex = bg;
            this.foregroundIndex = fg;
            this.startRange = start;
            this.endRange = end;
        }
        private TemperatureIcon(int bg, int fg, TemperatureScale.TemperatureRange range) {
            this(bg, fg, range, range);
        }
    }

    /* ---------------- Vignette (new logic) ---------------- */

    /**
     * Draw a full-screen vignette for COOL/WARM (subtle) and ICY/HOT (stronger).
     * Runs in Post/ALL so it always renders on top of the world but under HUD.
     */
    private void drawTemperatureVignette(int width, int height, Temperature temperature) {
        TemperatureScale.TemperatureRange range = temperature.getRange();

        ResourceLocation tex = null;
        float alpha = 0.0F;

        switch (range) {
            case COOL: {
                // Increase from 0 up to ~0.35 as you approach ICY
                float t = clamp01(temperature.getRangeDelta(true));
                tex = ICE_VIGNETTE;
                alpha = 0.35F * t;
                break;
            }
            case ICY: {
                // Stronger opacity in ICY: 0.35..0.85 as you get colder
                float t = clamp01(1.0F - temperature.getRangeDelta(true));
                tex = ICE_VIGNETTE;
                alpha = 0.35F + 0.50F * t;
                break;
            }
            case WARM: {
                float t = clamp01(temperature.getRangeDelta(false));
                tex = FIRE_VIGNETTE;
                alpha = 0.30F * t;
                break;
            }
            case HOT: {
                float t = clamp01(temperature.getRangeDelta(false));
                tex = FIRE_VIGNETTE;
                alpha = 0.30F + 0.55F * t;
                break;
            }
            default:
                break;
        }

        if (tex == null || alpha <= 0.01F) return;

        // --- GL state ---
        minecraft.getTextureManager().bindTexture(tex);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1F, 1F, 1F, alpha);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(0,     height, -90, 0, 1);
        t.addVertexWithUV(width, height, -90, 1, 1);
        t.addVertexWithUV(width, 0,      -90, 1, 0);
        t.addVertexWithUV(0,     0,      -90, 0, 0);
        t.draw();

        // --- restore ---
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private static float clamp01(float f) { return f < 0 ? 0 : (f > 1 ? 1 : f); }
}
