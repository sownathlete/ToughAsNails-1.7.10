package toughasnails.core;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import toughasnails.entities.EntityFreeze;
import toughasnails.entities.RenderFreeze;
import toughasnails.entities.projectile.EntityIceball;
import toughasnails.entities.projectile.RenderIceball;
import toughasnails.handler.health.HealthOverlayHandler;
import toughasnails.handler.temperature.TemperatureOverlayHandler;
import toughasnails.handler.thirst.ThirstOverlayHandler;
import toughasnails.particle.EntitySnowflakeFX;
import toughasnails.particle.TANParticleTypes;

import java.lang.reflect.Constructor;
import java.util.Random;

public class ClientProxy extends CommonProxy {

    // keep the original field name
    public static final ResourceLocation particleTexturesLocation =
            new ResourceLocation("toughasnails:textures/particles/particles.png");

    @Override
    public void registerRenderers() {
        // entity renderers
        registerEntityRenderer(EntityIceball.class, RenderIceball.class);
        registerEntityRenderer(EntityFreeze.class, RenderFreeze.class);

        // HUD overlays (client-side)
        MinecraftForge.EVENT_BUS.register(new TemperatureOverlayHandler());
        MinecraftForge.EVENT_BUS.register(new ThirstOverlayHandler());
        MinecraftForge.EVENT_BUS.register(new HealthOverlayHandler());
    }

    /**
     * 1.7.10 doesn't use model variants or ModelLoader.
     * Items are already registered in ModItems, so this is a NO-OP.
     */
    @Override
    public void registerItemVariantModel(Item item, String name, int metadata) {
        // no-op on 1.7.10
    }

    @Override
    public void registerNonRenderingProperties(Block block) {
        // Not applicable to 1.7.10.
    }

    @Override
    public void registerFluidBlockRendering(Block block, String name) {
        // Not applicable to 1.7.10.
    }

    @Override
    public void spawnParticle(TANParticleTypes type, double x, double y, double z, Object... info) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        if (world == null) return;

        EntityFX fx = null;
        Random rand = world.rand;

        switch (type) {
            case SNOWFLAKE:
                fx = new EntitySnowflakeFX(
                        world,
                        x, y, z,
                        rand.nextGaussian() * 0.03,
                        -0.02,
                        rand.nextGaussian() * 0.03
                );
                break;
            default:
                break;
        }

        if (fx != null) {
            mc.effectRenderer.addEffect(fx);
        }
    }

    /** Registers an entity rendering handler (1.7.10 Forge API style). */
    private static <E extends Entity> void registerEntityRenderer(
            Class<E> entityClass, Class<? extends Render> renderClass) {
        try {
            Constructor<? extends Render> ctor = renderClass.getConstructor();
            RenderingRegistry.registerEntityRenderingHandler(entityClass, ctor.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
