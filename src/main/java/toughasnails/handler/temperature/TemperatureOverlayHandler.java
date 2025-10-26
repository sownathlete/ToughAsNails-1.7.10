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
import net.minecraft.entity.player.PlayerCapabilities;
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

    public static final ResourceLocation OVERLAY = new ResourceLocation("toughasnails:textures/gui/overlay.png");
    public static final ResourceLocation ICE_VIGNETTE = new ResourceLocation("toughasnails:textures/gui/ice_vignette.png");
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
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();

        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) return;

        // Backported capability access
        TemperatureHandler temperatureStats = TemperatureHandler.get(player);
        if (temperatureStats == null) return;

        Temperature temperature = temperatureStats.getTemperature();
        random.setSeed(updateCounter * 312871);

        if (event.type == RenderGameOverlayEvent.ElementType.PORTAL && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
            if (!player.capabilities.isCreativeMode) {
                drawTemperatureVignettes(width, height, temperature);
            }
        } else if (event.type == RenderGameOverlayEvent.ElementType.EXPERIENCE && SyncedConfig.getBooleanValue(GameplayOption.ENABLE_TEMPERATURE)) {
            minecraft.getTextureManager().bindTexture(OVERLAY);
            if (minecraft.playerController.gameIsSurvivalOrAdventure()) {
                drawTemperature(width, height, temperature);
            }
        }
    }

    private void drawTemperature(int width, int height, Temperature temperature) {
        int left = width / 2 - 8;
        int top = height - 52;
        TemperatureScale.TemperatureRange range = temperature.getRange();

        // Shake effect when extreme
        if (range == TemperatureScale.TemperatureRange.ICY || range == TemperatureScale.TemperatureRange.HOT) {
            float shakeDelta = (range == TemperatureScale.TemperatureRange.ICY)
                    ? temperature.getRangeDelta(true)
                    : temperature.getRangeDelta(false);
            if (updateCounter % 1 == 0) {
                top += (random.nextInt(3) - 1) * Math.min(shakeDelta * 3.0F, 1.0F);
                left += (random.nextInt(3) - 1) * Math.min(shakeDelta * 1.5F, 1.0F);
            }
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

    private void drawTemperatureVignettes(int width, int height, Temperature temperature) {
        TemperatureScale.TemperatureRange range = temperature.getRange();
        float opacity = temperature.getRangeDelta(false);
        ResourceLocation vignette = null;

        if (range == TemperatureScale.TemperatureRange.ICY) {
            opacity = 1.0F - temperature.getRangeDelta(true);
            vignette = ICE_VIGNETTE;
        } else if (range == TemperatureScale.TemperatureRange.HOT) {
            vignette = FIRE_VIGNETTE;
        }

        if (vignette != null) {
            minecraft.getTextureManager().bindTexture(vignette);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1F, 1F, 1F, opacity);

            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            tess.addVertexWithUV(0, height, -90, 0, 1);
            tess.addVertexWithUV(width, height, -90, 1, 1);
            tess.addVertexWithUV(width, 0, -90, 1, 0);
            tess.addVertexWithUV(0, 0, -90, 0, 0);
            tess.draw();

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glColor4f(1F, 1F, 1F, 1F);
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
        BALL(1, 10, TemperatureScale.TemperatureRange.COOL, TemperatureScale.TemperatureRange.WARM),
        FIRE(2, 11, TemperatureScale.TemperatureRange.HOT);

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
}
