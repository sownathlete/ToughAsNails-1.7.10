package toughasnails.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public abstract class TANPotion extends Potion {

    private static final ResourceLocation POTIONS_LOCATION =
            new ResourceLocation("toughasnails:textures/potions/tanpotionfx.png");

    /**
     * 1.7.10-compatible constructor.
     * @param id The potion ID
     * @param isBadEffect Whether it is a negative effect
     * @param liquidColor Potion color (hex)
     */
    protected TANPotion(int id, boolean isBadEffect, int liquidColor) {
        super(id, isBadEffect, liquidColor);
    }

    @Override
    public boolean hasStatusIcon() {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(POTIONS_LOCATION);
        return true;
    }
}
