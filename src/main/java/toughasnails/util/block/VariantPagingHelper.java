package toughasnails.util.block;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import toughasnails.util.BlockStateUtils;
import toughasnails.util.BlockStateUtils.CompatState;
import toughasnails.util.BlockStateUtils.IProperty;

/**
 * Generic helper that splits a large enum of variants across several
 * Block instances (“pages”) so that Forge 1.7.10’s 16-meta limit is
 * respected while keeping the modern API shape intact.
 *
 * <p>All modern classes that disappeared before 1.8
 * (PropertyEnum, IBlockState …) are replaced with tiny
 * local shims so the original method signatures stay identical.</p>
 */
public class VariantPagingHelper<B extends Block,
                                 V extends Enum<V> & VariantPagingHelper.IPagedVariants> {

    /* --------------------------------------------------------------------- */
    /* Lightweight substitute for net.minecraft.block.properties.PropertyEnum */
    /* --------------------------------------------------------------------- */
    private static final class PropertyEnumStub<E extends Enum<E>> implements IProperty {

        private final String          name;
        private final Class<E>        enumClass;
        private final ImmutableSet<E> allowed;

        private PropertyEnumStub(String n, Class<E> c, Collection<E> vals) {
            name      = n;
            enumClass = c;
            allowed   = ImmutableSet.copyOf(vals);
        }

        static <T extends Enum<T>>
        PropertyEnumStub<T> create(String name, Class<T> cls, Collection<T> vals) {
            return new PropertyEnumStub<T>(name, cls, vals);
        }

        /* ---------- IProperty (shim) ---------- */
        @Override public String        getName()                    { return name; }
        @Override public Iterable<E>   getAllowedValues()           { return allowed; }
        @Override public String        getName(Comparable value)    { return ((Enum<?>)value).name().toLowerCase(); }
        @Override public String        toString()                   { return "PropertyEnumStub{"+name+'}'; }
    }

    /* ---------- data containers ---------- */
    private final int                 variantsPerPage;
    private final Class<V>            variantsEnumClass;

    private final Map<Integer, PropertyEnumStub<V>> pageNumToProperty  = new HashMap<Integer, PropertyEnumStub<V>>();
    private final Map<V, PageIndex>                 variantToPageIndex = new HashMap<V, PageIndex>();
    private final Map<Integer, V>                   masterIndexToVar   = new HashMap<Integer, V>();

    private final Map<Integer, B> pageNumToBlock = new HashMap<Integer, B>();
    private final Map<B, Integer> blockToPageNum = new HashMap<B, Integer>();

    /* ------------------------------------------------------------------ */
    /* ctor                                                                */
    /* ------------------------------------------------------------------ */
    public VariantPagingHelper(int perPage, Class<V> enumClass) {
        this(perPage, enumClass, Predicates.<V>alwaysTrue());
    }

    public VariantPagingHelper(int perPage,
                               Class<V> enumClass,
                               Predicate<V> filter) {

        variantsPerPage   = perPage;
        variantsEnumClass = enumClass;

        final V[] all = enumClass.getEnumConstants();
        if (all == null)
            throw new IllegalArgumentException("variantsEnumClass is not an enum!");

        List<V> current = new ArrayList<V>(perPage);
        int     page    = 0;

        for (V v : all) {
            if (!filter.apply(v)) continue;

            current.add(v);
            if (current.size() == perPage) {
                registerPage(page++, current);
                current = new ArrayList<V>(perPage);
            }
        }
        if (!current.isEmpty()) registerPage(page, current);
    }

    /* ------------------------------------------------------------------ */
    /* page registration helpers                                           */
    /* ------------------------------------------------------------------ */
    private void registerPage(int pageNum, List<V> values) {

        /* map enums -> index/page */
        for (int i = 0; i < values.size(); i++) {
            V v = values.get(i);
            PageIndex idx = new PageIndex(pageNum, i);
            variantToPageIndex.put(v, idx);
            masterIndexToVar.put(pageNum * variantsPerPage + i, v);
        }

        /* create synthetic PropertyEnum */
        pageNumToProperty.put(
                pageNum,
                PropertyEnumStub.create("variant", variantsEnumClass, values));
    }

    /* ------------------------------------------------------------------ */
    /* external registration                                               */
    /* ------------------------------------------------------------------ */
    public void addBlock(int pageNum, B block) {
        pageNumToBlock.put(pageNum, block);
        blockToPageNum.put(block, pageNum);
    }

    /* ------------------------------------------------------------------ */
    /* public API – information look-ups                                   */
    /* ------------------------------------------------------------------ */
    public int  getVariantsPerPage()         { return variantsPerPage; }
    public int  getNumPages()                { return pageNumToProperty.size(); }
    public PropertyEnumStub<V>
                 getVariantProperty(int p)   { return pageNumToProperty.get(p); }

    public int  getIndex(V v)                { return variantToPageIndex.get(v).index; }
    public int  getPageNum(V v)              { return variantToPageIndex.get(v).pageNum; }
    public int  getPageNum(B block)          { return blockToPageNum.get(block); }

    public V getVariant(int page, int index) {
        return masterIndexToVar.get(page * variantsPerPage + index);
    }
    public V getVariant(B block, int meta) {
        return getVariant(getPageNum(block), meta);
    }

    public B getBlock(int page)            { return pageNumToBlock.get(page); }
    public B getBlock(V variant)           { return pageNumToBlock.get(getPageNum(variant)); }

    /* ------------------------------------------------------------------ */
    /* Legacy-friendly “state” / item helpers                              */
    /* ------------------------------------------------------------------ */
    public CompatState getVariantState(V variant) {
        B blk  = getBlock(variant);
        PropertyEnumStub<V> prop = getVariantProperty(getPageNum(variant));

        /* CompatState is a minimal immutable holder defined in BlockStateUtils */
        CompatState base = new CompatState(blk, 0, ImmutableMap.<IProperty,Comparable<?>>of());
        return base.withProperty(prop, variant);
    }

    public ItemStack getVariantItem(V variant)            { return getVariantItem(variant, 1); }
    public ItemStack getVariantItem(V variant, int count) {
        return new ItemStack(getBlock(variant), count, getIndex(variant));
    }

    /* ------------------------------------------------------------------ */
    /* helper container                                                    */
    /* ------------------------------------------------------------------ */
    public final class PageIndex {
        private final int pageNum;
        private final int index;
        PageIndex(int p, int i){ pageNum=p; index=i; }
        public int  getPageNum(){ return pageNum; }
        public int  getIndex()  { return index;   }
        @Override public String toString(){ return "page:"+pageNum+" index:"+index; }
    }

    /* Marker interface expected by mod code                              */
    public interface IPagedVariants {}
}
