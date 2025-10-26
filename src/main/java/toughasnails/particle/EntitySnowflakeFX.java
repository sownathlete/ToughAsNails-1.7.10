package toughasnails.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import toughasnails.core.ClientProxy;

public class EntitySnowflakeFX extends EntityFX {

    public EntitySnowflakeFX(World world, double x, double y, double z,
                             double motionX, double motionY, double motionZ) {
        this(world, x, y, z, motionX, motionY, motionZ, 1.0F);
    }

    public EntitySnowflakeFX(World world, double x, double y, double z,
                             double motionX, double motionY, double motionZ, float scale) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);

        this.particleTextureIndexX = 7;
        this.particleTextureIndexY = 0;
        this.motionX *= 0.1D;
        this.motionY *= 0.1D;
        this.motionZ *= 0.1D;
        this.motionX += motionX;
        this.motionY += motionY;
        this.motionZ += motionZ;
        this.particleScale *= 0.75F * scale;

        this.particleMaxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D) * 8.0D);
        this.particleMaxAge *= scale;
        this.particleAge = this.rand.nextInt(2);
        this.particleAlpha = 1.0F;
        this.particleGravity = 0.02F;
    }

    @Override
    public int getFXLayer() {
        // custom particle texture sheet
        return 2;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }

        this.particleTextureIndexX = 7 - this.particleAge * 8 / this.particleMaxAge;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.posY == this.prevPosY) {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.96D;
        this.motionY *= 0.96D;
        this.motionZ *= 0.96D;

        if (this.onGround) {
            this.motionX *= 0.7D;
            this.motionZ *= 0.7D;
        }
    }

    @Override
    public void renderParticle(Tessellator tessellator, float partialTicks,
                               float rotX, float rotXZ, float rotZ, float rotYZ, float rotXY) {
        Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.particleTexturesLocation);

        float ageScale = ((float) this.particleAge + partialTicks) / (float) this.particleMaxAge * 32.0F;
        ageScale = Math.min(1.0F, Math.max(0.0F, ageScale));
        float scale = this.particleScale * ageScale;

        float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);

        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, this.particleAlpha);

        float minU = this.particleTextureIndexX / 8.0F;
        float maxU = minU + 0.125F;
        float minV = this.particleTextureIndexY / 8.0F;
        float maxV = minV + 0.125F;

        float s = 0.1F * scale;

        tessellator.startDrawingQuads();
        tessellator.setBrightness(240);
        tessellator.addVertexWithUV(x - rotX * s - rotYZ * s, y - rotXZ * s, z - rotZ * s - rotXY * s, maxU, maxV);
        tessellator.addVertexWithUV(x - rotX * s + rotYZ * s, y + rotXZ * s, z - rotZ * s + rotXY * s, maxU, minV);
        tessellator.addVertexWithUV(x + rotX * s + rotYZ * s, y + rotXZ * s, z + rotZ * s + rotXY * s, minU, minV);
        tessellator.addVertexWithUV(x + rotX * s - rotYZ * s, y - rotXZ * s, z + rotZ * s - rotXY * s, minU, maxV);
        tessellator.draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }
}
