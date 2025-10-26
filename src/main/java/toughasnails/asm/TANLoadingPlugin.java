package toughasnails.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

/**
 * Backported coremod loading plugin for Forge 1.7.10.
 *
 * This class registers ASM transformers for Tough As Nails during startup.
 */
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"toughasnails.asm"})
public class TANLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "toughasnails.asm.transformer.BlockCropsTransformer",
            "toughasnails.asm.transformer.EntityRendererTransformer",
            "toughasnails.asm.transformer.WorldTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null; // No ModContainer for this coremod
    }

    @Override
    public String getSetupClass() {
        return null; // No setup class used
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // Optional: FML passes the coremod’s classloader, gameDir, etc. here
        // Example: LaunchClassLoader classLoader = (LaunchClassLoader) data.get("classLoader");
    }

    @Override
    public String getAccessTransformerClass() {
        return null; // No access transformer
    }
}
