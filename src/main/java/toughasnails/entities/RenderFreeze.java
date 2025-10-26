package toughasnails.entities;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderFreeze extends RenderLiving {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("toughasnails:textures/entity/freeze.png");

    public RenderFreeze(ModelBase model, float shadowSize) {
        super(model, shadowSize);
    }

    public RenderFreeze() {
        this(new ModelFreeze(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }
}
