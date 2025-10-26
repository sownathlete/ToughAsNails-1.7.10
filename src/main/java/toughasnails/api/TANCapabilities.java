package toughasnails.api;

import java.util.HashMap;
import java.util.Map;

import toughasnails.api.stat.capability.CapabilityProvider.Capability;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.stat.capability.IThirst;

/**
 * Public entry-point that exposes the {@link Capability} handles used by
 * Tough As Nails in the 1.7.10 back-port.  
 *
 * <p>The <strong>only</strong> purpose of these objects is to act as
 * strongly-typed keys; the real data lives in the capability instances you
 * attach to each player entity.</p>
 */
public final class TANCapabilities {

    /** Capability key for player‐temperature data. */
    public static final Capability<ITemperature> TEMPERATURE;

    /** Capability key for player‐thirst data. */
    public static final Capability<IThirst> THIRST;

    /* ------------------------------------------------------------------ */
    /* Small, self-contained registry that guarantees we hand out exactly
       one Capability instance per id – important so “==” comparisons
       still work exactly like on modern Forge versions.                   */
    /* ------------------------------------------------------------------ */
    private static final class Registry {
        private static final Map<String, Capability<?>> MAP = new HashMap<String, Capability<?>>();

        private Registry() {}                         // no instances

        @SuppressWarnings("unchecked")
        private static <T> Capability<T> get(String id) {
            Capability<?> existing = MAP.get(id);
            if (existing != null) {
                return (Capability<T>) existing;
            }
            /* The stub Capability in CapabilityProvider only stores the id
               and an optional default instance – we pass <code>null</code>
               here because the real default is supplied by the concrete
               handler class when the capability is attached.             */
            Capability<T> created = new Capability<T>(id, null);
            MAP.put(id, created);
            return created;
        }
    }

    /* -------- static initialisation ------------------------------------ */
    static {
        TEMPERATURE = Registry.get("toughasnails:temperature");
        THIRST      = Registry.get("toughasnails:thirst");
    }

    private TANCapabilities() {}     // utility class – never instantiated
}
