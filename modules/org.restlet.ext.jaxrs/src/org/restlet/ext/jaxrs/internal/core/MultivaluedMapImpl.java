/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs.internal.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Implementation of the JAX-RS interface {@link MultivaluedMap}.
 * 
 * @author Stephan Koops
 * 
 * @param <K>
 * @param <V>
 */
public class MultivaluedMapImpl<K, V> extends HashMap<K, List<V>> implements
        MultivaluedMap<K, V> {

    private static final long serialVersionUID = 6228280442855580961L;

    /**
     * Creates a new empty {@link MultivaluedMapImpl}
     */
    public MultivaluedMapImpl() {
    }

    /**
     * Creates a copy of the given {@link MultivaluedMap}.
     * 
     * @param old
     *            the {@link MultivaluedMap} to copy. The values are not cloned.
     */
    public MultivaluedMapImpl(MultivaluedMap<K, V> old) {
        for (final Map.Entry<K, List<V>> entry : old.entrySet()) {
            final List<V> value = entry.getValue();
            put(entry.getKey(), new LinkedList<V>(value));
        }
    }

    /**
     * Add a value to the current list of values for the supplied key.
     * 
     * @param key
     *            the key
     * @param value
     *            the value to be added.
     * @see MultivaluedMap#add(Object, Object)
     */
    public void add(K key, V value) {
        List<V> list = get(key);
        if (list == null) {
            list = new LinkedList<V>();
            put(key, list);
        }
        list.add(value);
    }

    /**
     * Creates a clone of this map. The contained values are not cloned.
     * 
     * @return A copy of this map
     */
    @Override
    public MultivaluedMapImpl<K, V> clone() {
        return new MultivaluedMapImpl<K, V>(this);
    }

    /**
     * A shortcut to get the first value of the supplied key.
     * 
     * @param key
     *            the key
     * @return the first value for the specified key or null if the key is not
     *         in the map.
     * @see MultivaluedMap#getFirst(Object)
     */
    public V getFirst(K key) {
        final List<V> list = get(key);
        if ((list == null) || list.isEmpty()) {
            return null;
        }
        return Util.getFirstElement(list);
    }

    /**
     * A shortcut to get the last value of the supplied key.
     * 
     * @param key
     *            the key
     * @return the last value for the specified key or null if the key is not in
     *         the map.
     */
    public V getLast(K key) {
        final List<V> list = get(key);
        if ((list == null) || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Set the key's value to be a one item list consisting of the supplied
     * value. Any existing values will be replaced.
     * 
     * @param key
     *            the key
     * @param value
     *            the single value of the key
     * @see MultivaluedMap#putSingle(Object, Object)
     */
    public void putSingle(K key, V value) {
        final List<V> list = new LinkedList<V>();
        list.add(value);
        put(key, list);
    }
}