package toughasnails.handler.temperature;

import java.util.Iterator;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.temperature.TemperatureDebugger;
import toughasnails.temperature.TemperatureHandler;

@SideOnly(Side.CLIENT)
public class TemperatureDebugOverlayHandler {

    @SubscribeEvent
    public void onPostRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null)
            return;

        // 1.7.10-style access instead of getCapability()
        TemperatureHandler temperatureStats = TemperatureHandler.get(player);
        if (temperatureStats == null || temperatureStats.debugger == null)
            return;

        TemperatureDebugger debugger = temperatureStats.debugger;
        if (debugger.isGuiVisible()) {
            ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int width = resolution.getScaledWidth();
            int height = resolution.getScaledHeight();
            drawModifierTable(width, height, temperatureStats.getTemperature(), debugger);
        }
    }

    private void drawModifierTable(int width, int height, Temperature temperature, TemperatureDebugger debugger) {
        Map<TemperatureDebugger.Modifier, Integer> rateModifiers = debugger.modifiers[0];
        Map<TemperatureDebugger.Modifier, Integer> targetModifiers = debugger.modifiers[1];

        if (targetModifiers != null && rateModifiers != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            int targetTableHeight = getTableHeight(targetModifiers);
            int totalTableHeight = targetTableHeight + getTableHeight(rateModifiers) + 2;
            int startY = height / 2 - totalTableHeight / 2;

            String targetProgress = "§c" + temperature.getRawValue() + "/" + debugger.targetTemperature + getCappedText(debugger.targetTemperature);
            String rateProgress = "§c" + debugger.temperatureTimer + "/" + debugger.changeTicks;

            drawTable("Target " + targetProgress, 1, startY, targetModifiers);
            drawTable("Rate " + rateProgress, 1, startY + targetTableHeight + 2, rateModifiers);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void drawTable(String title, int x, int y, Map<TemperatureDebugger.Modifier, Integer> contents) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int lineWidth = getLineWidth(title, contents);
        int textStart = x + 2;
        int textEnd = textStart + lineWidth;

        Iterator<Map.Entry<TemperatureDebugger.Modifier, Integer>> iterator = contents.entrySet().iterator();

        for (int i = 0; i < contents.size() + 1; ++i) {
            int rowTopY = y + i * fontRenderer.FONT_HEIGHT + 1;
            int rowBottomY = rowTopY + fontRenderer.FONT_HEIGHT;

            if (i == 0) {
                Gui.drawRect(x, y, textEnd, rowBottomY, 0x60000000);
                fontRenderer.drawString(title, textStart + (lineWidth / 2) - (fontRenderer.getStringWidth(title) / 2), y + 1, 0x20FFFFFF);
                continue;
            }

            Map.Entry<TemperatureDebugger.Modifier, Integer> entry = iterator.next();
            String name = entry.getKey().name;
            int value = entry.getValue();
            String formattedValue = getFormattedInt(value);

            Gui.drawRect(x, rowTopY, textEnd, rowBottomY, 0x50000000);
            fontRenderer.drawString(name, textStart, rowTopY, 0x20FFFFFF);
            fontRenderer.drawString(formattedValue, textEnd - fontRenderer.getStringWidth(formattedValue), rowTopY, 0x20FFFFFF);
        }
    }

    @SideOnly(Side.CLIENT)
    private static int getTableHeight(Map<?, ?> contents) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        return (contents.size() + 1) * fontRenderer.FONT_HEIGHT + 1;
    }

    @SideOnly(Side.CLIENT)
    private static int getLineWidth(String title, Map<TemperatureDebugger.Modifier, Integer> elements) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int lineWidth = fontRenderer.getStringWidth(title);

        for (Map.Entry<TemperatureDebugger.Modifier, Integer> entry : elements.entrySet()) {
            int value = entry.getValue();
            String str = entry.getKey().name + ": " + getFormattedInt(value);
            lineWidth = Math.max(fontRenderer.getStringWidth(str), lineWidth);
        }
        return lineWidth;
    }

    private static String getCappedText(int targetTemperature) {
        if (targetTemperature < 0)
            return "§9 (0)";
        if (targetTemperature > TemperatureScale.getScaleTotal())
            return "§9 (" + TemperatureScale.getScaleTotal() + ")";
        return "";
    }

    private static String getFormattedInt(int i) {
        String color = i > 0 ? "§c" : (i < 0 ? "§9" : "§r");
        return color + getNumberSign(i) + i;
    }

    private static char getNumberSign(int i) {
        return i > 0 ? '+' : ' ';
    }
}
