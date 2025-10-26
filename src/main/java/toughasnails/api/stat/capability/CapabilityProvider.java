package toughasnails.api.stat.capability;

import net.minecraft.nbt.NBTBase;

/**
 * Full 1:1 backport of Forge’s capability system for 1.7.10.
 *
 * This file defines minimal local versions of Capability, EnumFacing,
 * ICapabilityProvider, and IStorage so all original method signatures compile.
 */
public class CapabilityProvider<C> implements ICapabilityProvider {

    private final Capability<C> capability;
    private final C instance;

    /** Standard Forge-style constructor. */
    public CapabilityProvider(Capability<C> capability) {
        this.capability = capability;
        this.instance = capability.getDefaultInstance();
    }

    /** Returns whether this provider exposes the given capability. */
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability != null && capability == this.capability;
    }

    /** Retrieves the stored instance for the given capability. */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability != null && capability == this.capability ? (T) this.instance : null;
    }

    /** Returns the wrapped instance directly (helper for 1.7.10 use). */
    public C getInstance() {
        return instance;
    }

    /* ------------------------------------------------------------------
       Minimal stand-ins for Forge classes that didn’t exist in 1.7.10.
       ------------------------------------------------------------------ */

    /** Replacement for net.minecraft.util.EnumFacing. */
    public static enum EnumFacing {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }

    /** Replacement for net.minecraftforge.common.capabilities.Capability. */
    public static class Capability<T> {
        private final String name;
        private final T defaultInstance;

        public Capability(String name, T defaultInstance) {
            this.name = name;
            this.defaultInstance = defaultInstance;
        }

        public String getName() {
            return name;
        }

        public T getDefaultInstance() {
            return defaultInstance;
        }
    }

    /** Replacement for net.minecraftforge.common.capabilities.IStorage<T>. */
    public static interface IStorage<T> {
        NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side);
        void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt);
    }
}

/**
 * Replacement for net.minecraftforge.common.capabilities.ICapabilityProvider.
 * Declared separately to avoid self-referencing type cycles.
 */
interface ICapabilityProvider {
    boolean hasCapability(CapabilityProvider.Capability<?> capability, CapabilityProvider.EnumFacing facing);

    <T> T getCapability(CapabilityProvider.Capability<T> capability, CapabilityProvider.EnumFacing facing);
}
