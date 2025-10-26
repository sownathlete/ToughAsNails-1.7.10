package toughasnails.core;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.particle.TANParticleTypes;
import toughasnails.thirst.ThirstHandler;

/**
 * Shared (server + client) proxy.
 * ClientProxy overrides the rendering/particle bits.
 * This base proxy also bootstraps the stat systems so temperature & thirst tick.
 */
public class CommonProxy {

    /* ------------------------------------------------------------------
     * Lifecycle – call these from your main mod class.
     * ------------------------------------------------------------------ */
    public void preInit(FMLPreInitializationEvent e) {
        // nothing common-only here yet
    }

    public void init(FMLInitializationEvent e) {
        // Ensure our per-player stat systems are attached & ticking.
        // Calls are idempotent (guarded inside helpers), so safe on both sides.
        TemperatureHelper.bootstrap();
        ThirstHandler.bootstrap();
    }

    public void postInit(FMLPostInitializationEvent e) {
        // nothing common-only here yet
    }

    /* ------------------------------------------------------------------
     * Rendering / model registration stubs (client overrides).
     * ------------------------------------------------------------------ */
    public void registerRenderers() { /* no-op on server */ }

    public void registerItemVariantModel(Item item, String name, int metadata) { /* 1.7.10: no-op here */ }

    public void registerNonRenderingProperties(Block block) { /* 1.7.10: no-op */ }

    public void registerFluidBlockRendering(Block block, String name) { /* 1.7.10: no-op */ }

    public void spawnParticle(TANParticleTypes type, double x, double y, double z, Object... info) { /* server: no-op */ }

    /* ------------------------------------------------------------------
     * Safe accessors (client overrides return real values).
     * ------------------------------------------------------------------ */
    public World getClientWorld() { return null; }

    public EntityPlayer getClientPlayer() { return null; }
}
