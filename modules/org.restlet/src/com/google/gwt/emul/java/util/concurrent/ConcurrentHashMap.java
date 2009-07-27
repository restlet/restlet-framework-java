package com.google.gwt.emul.java.util.concurrent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Emulate the ConcurrentHashMap class, especially for the GWT module.
 * 
 * @author Thierry Boileau
 */
public class ConcurrentHashMap<K, V> implements Map<K, V> {

    Map<K, V> map;

    public ConcurrentHashMap() {
        super();
        map = new HashMap<K, V>();
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    public V get(Object key) {
        return map.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public V put(K key, V value) {
        return map.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        map.putAll(t);
    }

    public V remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Collection<V> values() {
        return map.values();
    }

}
