package toughasnails.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import toughasnails.config.GameplayConfigurationHandler;

/**
 * Forge 1.7.10-compatible configuration GUI for Tough As Nails.
 */
public class GuiTANConfig extends GuiConfig {

    public GuiTANConfig(GuiScreen parentScreen) {
        super(parentScreen,
              getConfigElements(),
              "ToughAsNails",
              false,
              false,
              StatCollector.translateToLocal("toughasnails.config.title"));
    }

    private static List<IConfigElement> getConfigElements() {
        ArrayList<IConfigElement> list = new ArrayList<IConfigElement>();

        List<IConfigElement> survivalSettings = new ConfigElement(
                GameplayConfigurationHandler.config.getCategory("survival settings")).getChildElements();

        list.add(new DummyConfigElement.DummyCategoryElement(
                StatCollector.translateToLocal("config.category.survivalSettings.title"),
                "config.category.survivalSettings",
                survivalSettings));

        return list;
    }
}
