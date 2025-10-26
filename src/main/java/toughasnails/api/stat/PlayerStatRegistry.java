package toughasnails.api.stat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import toughasnails.api.stat.capability.CapabilityProvider;
import toughasnails.api.stat.capability.CapabilityProvider.Capability;

/**
 * Backported PlayerStatRegistry for Forge 1.7.10 with per-player
 * handler instance management. Keeps the capability-like API surface
 * for compatibility with backported code, while actually storing
 * handler instances in-memory and persisting via NBT.
 */
public class PlayerStatRegistry {

    /** Maps capability identifiers to their handler implementation classes. */
    private static final Map<String, Class<? extends StatHandlerBase>> PLAYER_STATS = Maps.newHashMap();

    /** Maps identifiers to registered capability wrappers (stubbed for 1.7.10). */
    private static final Map<String, Capability<? extends IPlayerStat>> STAT_CAPABILITIES = Maps.newHashMap();

    /** Per-player live instances of stat handlers. */
    private static final WeakHashMap<EntityPlayer, Map<String, StatHandlerBase>> INSTANCES = new WeakHashMap<EntityPlayer, Map<String, StatHandlerBase>>();

    /**
     * Registers a new player stat handler and its capability.
     */
    public static <T extends IPlayerStat> void addStat(
            Class<T> capabilityClass,
            Capability<T> capability,
            Class<? extends StatHandlerBase> implementationClass) {

        String identifier = capabilityClass.getName();
        if (identifier == null) {
            throw new RuntimeException("Stat identifier cannot be null!");
        }
        if (PLAYER_STATS.containsKey(identifier)) {
            throw new RuntimeException("Stat with identifier " + identifier + " already exists!");
        }

        PLAYER_STATS.put(identifier, implementationClass);
        STAT_CAPABILITIES.put(identifier, capability);
    }

    /**
     * Registers a standalone capability (alternative path).
     */
    public static void registerCapability(Capability<? extends IPlayerStat> capability) {
        STAT_CAPABILITIES.put(capability.getName(), capability);
    }

    /**
     * Creates a capability provider for the given identifier.
     * (Kept for API compatibility with backported code.)
     */
    @SuppressWarnings("unchecked")
    public static CapabilityProvider<IPlayerStat> createCapabilityProvider(String identifier, EntityPlayer player) {
        Capability<? extends IPlayerStat> cap = STAT_CAPABILITIES.get(identifier);
        if (cap == null) throw new RuntimeException("Unknown capability: " + identifier);
        return new CapabilityProvider<IPlayerStat>((Capability<IPlayerStat>) cap);
    }

    /**
     * Retrieves a registered capability by its identifier.
     */
    public static Capability<?> getCapability(String identifier) {
        return STAT_CAPABILITIES.get(identifier);
    }

    /**
     * Returns an immutable copy of all capability mappings.
     */
    public static ImmutableMap<String, Capability<? extends IPlayerStat>> getCapabilityMap() {
        return ImmutableMap.copyOf(STAT_CAPABILITIES);
    }

    /**
     * Returns the live handler instance for a player + identifier.
     * Creates & caches it on first access.
     */
    public static StatHandlerBase getStat(EntityPlayer player, String identifier) {
        Map<String, StatHandlerBase> map = INSTANCES.get(player);
        if (map == null) {
            map = Maps.newHashMap();
            INSTANCES.put(player, map);
        }

        StatHandlerBase handler = map.get(identifier);
        if (handler == null) {
            Class<? extends StatHandlerBase> impl = PLAYER_STATS.get(identifier);
            if (impl == null) {
                throw new IllegalStateException("No stat implementation registered for identifier: " + identifier);
            }
            try {
                handler = impl.newInstance(); // requires a public no-arg constructor
            } catch (Throwable t) {
                throw new RuntimeException("Failed to instantiate stat handler for " + identifier + ": " + impl.getName(), t);
            }
            map.put(identifier, handler);
        }
        return handler;
    }

    /**
     * Returns (and ensures) a map of all live stat handlers for a player.
     * Handlers are created lazily and cached.
     */
    public static Map<String, StatHandlerBase> getStatHandlers(EntityPlayer player) {
        Map<String, StatHandlerBase> map = INSTANCES.get(player);
        if (map == null) {
            map = Maps.newHashMap();
            INSTANCES.put(player, map);
        }
        // Ensure all registered stat types exist for this player
        for (Map.Entry<String, Class<? extends StatHandlerBase>> e : PLAYER_STATS.entrySet()) {
            String id = e.getKey();
            if (!map.containsKey(id)) {
                try {
                    StatHandlerBase handler = e.getValue().newInstance();
                    map.put(id, handler);
                } catch (Throwable t) {
                    throw new RuntimeException("Failed to instantiate stat handler for " + id + ": " + e.getValue().getName(), t);
                }
            }
        }
        return map;
    }

    /* ------------------------------------------------------------------
       Optional NBT persistence helpers (unchanged)
       ------------------------------------------------------------------ */

    public static NBTTagCompound saveToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, Class<? extends StatHandlerBase>> e : PLAYER_STATS.entrySet()) {
            tag.setString(e.getKey(), e.getValue().getName());
        }
        return tag;
    }

    public static void loadFromNBT(NBTTagCompound tag, EntityPlayer player) {
        // Implement if your individual stats require it.
    }
}
