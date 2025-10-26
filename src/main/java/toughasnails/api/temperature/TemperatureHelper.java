package toughasnails.api.temperature;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import toughasnails.temperature.TemperatureHandler;
import toughasnails.temperature.TemperatureStorage;

/**
 * Forge 1.7.10 back-port helper for Tough As Nails temperature.
 *
 * - Stores a TemperatureHandler per-player in persistent Entity NBT.
 * - Rehydrates on demand.
 * - Exposes bootstrap() to register a player tick listener so temperature updates run.
 */
public final class TemperatureHelper {

    /** NBT key used to store the serialized handler on the player. */
    private static final String KEY_ROOT = "TAN_Temperature";

    /** Storage adapter reused for (de)serialization. */
    private static final TemperatureStorage STORAGE = new TemperatureStorage();

    /** Guard so we only register tick hooks once. */
    private static boolean BOOTSTRAPPED = false;

    private TemperatureHelper() {}

    /* ====================================================================== */
    /* Public API                                                             */
    /* ====================================================================== */

    /** Call once during mod init (client + server safe). */
    public static void bootstrap() {
        if (BOOTSTRAPPED) return;
        BOOTSTRAPPED = true;
        // PlayerTickEvent in 1.7.10 is posted on the FML bus
        FMLCommonHandler.instance().bus().register(new EventHooks());
    }

    /**
     * Returns a TemperatureHandler for this player. If none was saved yet,
     * a fresh handler (midpoint temperature) is returned.
     */
    public static TemperatureHandler getOrCreate(EntityPlayer player) {
        if (player == null) return new TemperatureHandler();
        NBTTagCompound root = player.getEntityData();

        if (!root.hasKey(KEY_ROOT)) {
            // No saved state yet — new handler with defaults.
            return new TemperatureHandler();
        }

        // Rehydrate from NBT using the same storage logic used on save.
        NBTTagCompound tag = root.getCompoundTag(KEY_ROOT);
        TemperatureHandler handler = new TemperatureHandler();
        STORAGE.readNBT(null, handler, null, tag);
        return handler;
    }

    /** Alias used by existing code paths (items, HUD). */
    public static TemperatureHandler getTemperatureData(EntityPlayer player) {
        return getOrCreate(player);
    }

    /** Saves the given handler back into the player's persistent NBT. */
    public static void save(EntityPlayer player, TemperatureHandler handler) {
        if (player == null || handler == null) return;
        NBTTagCompound tag = (NBTTagCompound) STORAGE.writeNBT(null, handler, null);
        player.getEntityData().setTag(KEY_ROOT, tag);
    }

    /* ====================================================================== */
    /* World helpers for temperature regulators (coils, etc.)                 */
    /* ====================================================================== */

    /** Returns all temperature regulators currently loaded in the world. */
    public static List<ITemperatureRegulator> getTemperatureRegulators(World world) {
        ArrayList<ITemperatureRegulator> list = new ArrayList<ITemperatureRegulator>();
        if (world == null) return list;
        @SuppressWarnings("unchecked")
        List<Object> tiles = world.loadedTileEntityList;
        for (Object o : tiles) {
            if (o instanceof ITemperatureRegulator) {
                list.add((ITemperatureRegulator) o);
            }
        }
        return list;
    }

    /**
     * Checks if the given coordinate is inside any regulator whose target
     * temperature is at least the provided temperature.
     */
    public static boolean isPosClimatisedForTemp(World world, int x, int y, int z, Temperature temperature) {
        if (world == null || temperature == null) return false;
        for (ITemperatureRegulator reg : getTemperatureRegulators(world)) {
            if (reg.getRegulatedTemperature().getRawValue() >= temperature.getRawValue()
                && reg.isPosRegulated(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    /* ====================================================================== */
    /* Tick bridge                                                            */
    /* ====================================================================== */

    /**
     * Listens to PlayerTickEvent and runs temperature logic.
     * Must be public & static so FML's ASM handler can access it (avoids IllegalAccessError).
     */
    public static final class EventHooks {
        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent e) {
            final EntityPlayer p = e.player;
            if (p == null) return;
            final World w = p.worldObj;

            // Pull current state, tick, then persist (only when changed)
            TemperatureHandler h = TemperatureHelper.getOrCreate(p);
            h.update(p, w, e.phase);

            if (e.phase == TickEvent.Phase.END && h.hasChanged()) {
                TemperatureHelper.save(p, h);
                h.onSendClientUpdate();
            }
        }
    }
}
