/* ======================================================================
   BlockStateUtils – Forge 1.7.10 shim (final fix)
   ====================================================================== */
package toughasnails.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import net.minecraft.block.Block;
import toughasnails.api.ITANBlock;

public final class BlockStateUtils {

    /* -------------------------------------------------------- */
    /* very-lightweight property stub used for compilation only */
    /* -------------------------------------------------------- */
    public interface IProperty {
        String getName();
        Iterable<?> getAllowedValues();
        String getName(@SuppressWarnings("rawtypes") Comparable v);
    }

    private static final class NameProperty implements IProperty {
        private final String name;
        NameProperty(String n) { name = n; }
        @Override public String getName() { return name; }
        @Override public Iterable<?> getAllowedValues() { return ImmutableSet.of("dummy"); }
        @Override public String getName(@SuppressWarnings("rawtypes") Comparable v){ return v.toString(); }
        @Override public String toString() { return "Prop{" + name + '}'; }
    }

    /* ------------------------------------------------------------------ */
    /* ***  made public so other packages (e.g. VariantPagingHelper)   *** */
    /* ***  can construct states.                                      *** */
    /* ------------------------------------------------------------------ */
    public static final class CompatState {
        private final Block block;
        private final int   meta;
        private final ImmutableMap<IProperty, Comparable<?>> props;

        /** Public so helpers in other packages may build states. */
        public CompatState(Block b, int m,
                           ImmutableMap<IProperty, Comparable<?>> p) {
            block = b; meta = m; props = p;
        }

        public Block getBlock()                                   { return block; }
        public int   getMeta()                                    { return meta;  }
        public ImmutableMap<IProperty,Comparable<?>> getProperties(){ return props; }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public CompatState withProperty(IProperty p, Comparable v) {
            ImmutableMap.Builder<IProperty,Comparable<?>> b = ImmutableMap.builder();
            b.putAll(props).put(p, v);
            return new CompatState(block, meta, b.build());
        }

        @SuppressWarnings("rawtypes")
        public Comparable getValue(IProperty p) { return props.get(p); }
    }

    /* ------------------------------------------------------------------ */
    /* unchanged public helpers                                            */
    /* ------------------------------------------------------------------ */
    public static String getStateInfoAsString(CompatState st) {
        if (st == null) return "null";
        StringBuilder sb = new StringBuilder(st.getBlock().getClass().getName()).append('[');
        boolean first = true;
        for (IProperty p : st.getProperties().keySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(p.getName()).append('=').append(st.getValue(p));
        }
        return sb.append(']').toString();
    }

    public static ImmutableSet<CompatState> getStatesSet(CompatState base, IProperty... props) {
        if (base == null || props.length == 0) return ImmutableSet.of(base);
        Set<CompatState> out = new HashSet<CompatState>();
        Deque<IProperty> stack = new ArrayDeque<IProperty>(Arrays.asList(props));
        addStatesRecursive(base, out, stack);
        return ImmutableSet.copyOf(out);
    }

    private static void addStatesRecursive(CompatState st, Set<CompatState> list, Deque<IProperty> stack) {
        if (stack.isEmpty()) { list.add(st); return; }
        IProperty p = stack.pop();
        for (Object v : p.getAllowedValues())
            addStatesRecursive(st.withProperty(p, (Comparable<?>) v), list, stack);
        stack.push(p);
    }

    /** Accepts either IProperty[] or String[] returned by ITANBlock. */
    public static ImmutableSet<CompatState> getBlockPresets(Block blk) {
        if (!(blk instanceof ITANBlock))
            return ImmutableSet.of(new CompatState(blk, 0, ImmutableMap.<IProperty,Comparable<?>>of()));

        ITANBlock tan = (ITANBlock) blk;
        Object[] raw  = tan.getPresetProperties();
        List<IProperty> props = new ArrayList<IProperty>();

        for (Object o : raw)
            props.add(o instanceof IProperty ? (IProperty)o : new NameProperty(o.toString()));

        CompatState base = new CompatState(blk, 0, ImmutableMap.<IProperty,Comparable<?>>of());
        return getStatesSet(base, props.toArray(new IProperty[0]));
    }

    public static CompatState getPresetState(CompatState state) {
        if (state == null || !(state.getBlock() instanceof ITANBlock)) return state;
        ITANBlock tan = (ITANBlock) state.getBlock();

        CompatState out = new CompatState(state.getBlock(), state.getMeta(),
                                          ImmutableMap.<IProperty,Comparable<?>>of());
        for (Object o : tan.getPresetProperties())
            if (o instanceof IProperty)
                out = out.withProperty((IProperty)o, state.getValue((IProperty)o));
        return out;
    }

    public static IProperty getPropertyByName(CompatState st, String name) {
        for (IProperty p : st.getProperties().keySet())
            if (p.getName().equals(name)) return p;
        return null;
    }

    public static boolean isValidPropertyName(CompatState st, String name) {
        return getPropertyByName(st, name) != null;
    }

    @SuppressWarnings("rawtypes")
    public static Comparable getPropertyValueByName(CompatState st, IProperty p, String valueName) {
        for (Object v : p.getAllowedValues())
            if (v.toString().equals(valueName)) return (Comparable) v;
        return null;
    }

    private BlockStateUtils() {}  // util class
}
