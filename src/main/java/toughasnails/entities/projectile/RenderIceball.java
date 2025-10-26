package toughasnails.entities.projectile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import toughasnails.api.item.TANItems;

public class RenderIceball extends Render {

    public RenderIceball() {
        this.shadowSize = 0.0F;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z,
                         float yaw, float partialTicks) {

        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glEnable(GL11.GL_NORMALIZE);

        float scale = 0.5F;
        GL11.glScalef(scale, scale, scale);

        // make the quad face the camera
        GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

        this.bindTexture(TextureMap.locationItemsTexture);

        Tessellator tessellator = Tessellator.instance;
        ItemStack stack = new ItemStack(TANItems.ice_charge, 1, 0);

        // Render a simple 2D quad for the item
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.25F, 0.0F);
        ItemRenderer.renderItemIn2D(
                tessellator,
                0.0F, 0.0F, 1.0F, 1.0F,
                16, 16, 0.0625F
        );
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_NORMALIZE);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationItemsTexture;
    }
}
