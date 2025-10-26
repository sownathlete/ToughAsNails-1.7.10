package toughasnails.entities;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelFreeze extends ModelBase {

    private ModelRenderer[] freezeSticks = new ModelRenderer[12];
    private ModelRenderer freezeHead;

    public ModelFreeze() {
        textureWidth = 64;
        textureHeight = 32;

        for (int i = 0; i < this.freezeSticks.length; ++i) {
            this.freezeSticks[i] = new ModelRenderer(this, 0, 16);
            this.freezeSticks[i].addBox(0.0F, 0.0F, 0.0F, 2, 8, 2);
        }

        this.freezeHead = new ModelRenderer(this, 0, 0);
        this.freezeHead.addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount,
                       float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.freezeHead.render(scale);
        for (int i = 0; i < this.freezeSticks.length; ++i) {
            this.freezeSticks[i].render(scale);
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {

        float f = ageInTicks * (float)Math.PI * -0.1F;

        // Top 4 sticks
        for (int i = 0; i < 4; ++i) {
            this.freezeSticks[i].rotationPointY = -2.0F + MathHelper.cos(((i * 2.0F) + ageInTicks) * 0.25F);
            this.freezeSticks[i].rotationPointX = MathHelper.cos(f) * 9.0F;
            this.freezeSticks[i].rotationPointZ = MathHelper.sin(f) * 9.0F;
            f += 1.0F;
        }

        // Middle 4 sticks
        f = 0.7853982F + ageInTicks * (float)Math.PI * 0.03F;
        for (int j = 4; j < 8; ++j) {
            this.freezeSticks[j].rotationPointY = 2.0F + MathHelper.cos(((j * 2.0F) + ageInTicks) * 0.25F);
            this.freezeSticks[j].rotationPointX = MathHelper.cos(f) * 7.0F;
            this.freezeSticks[j].rotationPointZ = MathHelper.sin(f) * 7.0F;
            f += 1.0F;
        }

        // Bottom 4 sticks
        f = 0.47123894F + ageInTicks * (float)Math.PI * -0.05F;
        for (int k = 8; k < 12; ++k) {
            this.freezeSticks[k].rotationPointY = 11.0F + MathHelper.cos(((k * 1.5F) + ageInTicks) * 0.5F);
            this.freezeSticks[k].rotationPointX = MathHelper.cos(f) * 5.0F;
            this.freezeSticks[k].rotationPointZ = MathHelper.sin(f) * 5.0F;
            f += 1.0F;
        }

        this.freezeHead.rotateAngleY = netHeadYaw / (180F / (float)Math.PI);
        this.freezeHead.rotateAngleX = headPitch / (180F / (float)Math.PI);
    }
}
