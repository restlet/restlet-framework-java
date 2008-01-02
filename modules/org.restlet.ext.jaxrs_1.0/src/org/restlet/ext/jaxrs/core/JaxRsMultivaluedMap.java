/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.jaxrs.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Implementation of the JAX-RS interface {@link MultivaluedMap}
 * 
 * @author Stephan
 * 
 * @param <K>
 * @param <V>
 */
public class JaxRsMultivaluedMap<K, V> implements MultivaluedMap<K, V> {
    /**
     * The Lists must not be emtpy. If they are empty, they must removed from
     * the map.
     */
    private Map<K, List<V>> entries = new HashMap<K, List<V>>();

    public V getFirst(K key) {
        List<V> entr = entries.get(key);
        if (entr == null)
            return null;
        return entr.get(0);
    }

    public void clear() {
        entries.clear();
    }

    public boolean containsKey(Object key) {
        return entries.containsKey(key);
    }

    public boolean containsValue(Object value) {
        for (List<V> values : entries.values())
            if (values.contains(value))
                return true;
        return false;
    }

    public Set<java.util.Map.Entry<K, List<V>>> entrySet() {
        return entries.entrySet();
    }

    public List<V> get(Object key) {
        return entries.get(key);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public Set<K> keySet() {
        return entries.keySet();
    }

    public void putSingle(K key, V value) {
        List<V> list = new ArrayList<V>(1);
        list.add(value);
        entries.put(key, list);
    }

    public void add(K key, V value) {
        List<V> list = entries.get(key);
        if (list == null) {
            list = new ArrayList<V>(1);
            entries.put(key, list);
        }
        list.add(value);
    }

    /**
     * Because it is not specified, what should happens,
     */
    public List<V> put(K key, List<V> value) {
        return entries.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends List<V>> m) {
        for (Map.Entry<? extends K, ? extends List<V>> entry : m.entrySet())
            this.put(entry.getKey(), entry.getValue());
    }

    public List<V> remove(Object key) {
        List<V> old = entries.remove(key);
        if (old == null)
            return new ArrayList<V>();
        return old;
    }

    public int size() {
        int size = 0;
        for (List<V> list : entries.values())
            size += list.size();
        return size;
    }

    public Collection<List<V>> values() {
        return entries.values();
    }
}