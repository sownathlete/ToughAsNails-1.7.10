/*
 * Decompiled with CFR 0.148.
 */
package toughasnails.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MapUtils {
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        ArrayList<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(map.size());
        list.addAll(map.entrySet());
        ValueComparator comparator = new ValueComparator();
        Collections.sort(list, comparator);
        LinkedHashMap result = new LinkedHashMap();
        for (Map.Entry entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static class ValueComparator<K, V extends Comparable<? super V>>
    implements Comparator<Map.Entry<K, V>> {
        private ValueComparator() {
        }

        @Override
        public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
            return -((Comparable)o1.getValue()).compareTo(o2.getValue());
        }
    }

}

