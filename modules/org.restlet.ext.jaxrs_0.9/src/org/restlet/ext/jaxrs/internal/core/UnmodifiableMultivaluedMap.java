/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.ext.jaxrs.internal.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.Form;
import org.restlet.data.Parameter;

/**
 * An unmodifiable {@link MultivaluedMap}.
 * 
 * @author Stephan Koops
 * 
 * @param <K>
 * @param <V>
 */
public class UnmodifiableMultivaluedMap<K, V> implements MultivaluedMap<K, V> {

    /**
     * Creates a MultiValuedMap of unmodifiable Lists.
     */
    private static MultivaluedMapImpl<String, String> copyForm(Form form,
            boolean caseInsensitive) {
        final MultivaluedMapImpl<String, String> mmap = new MultivaluedMapImpl<String, String>();
        for (final Parameter param : form) {
            final String key = caseInsensitive ? param.getName().toLowerCase()
                    : param.getName();
            mmap.add(key, param.getValue());
        }
        for (final Map.Entry<String, List<String>> entry : mmap.entrySet()) {
            final List<String> unmodifiable = Collections
                    .unmodifiableList(entry.getValue());
            mmap.put(entry.getKey(), unmodifiable);
        }
        return mmap;
    }

    /**
     * Returns an UnmodifiableMultivaluedMap, that contains the content of the
     * given {@link MultivaluedMap}.
     * 
     * @param mmap
     * @return
     */
    public static UnmodifiableMultivaluedMap<String, String> get(
            MultivaluedMap<String, String> mmap) {
        return get(mmap, true);
    }

    /**
     * Returns an UnmodifiableMultivaluedMap, that contains the content of the
     * given {@link MultivaluedMap}.
     * 
     * @param mmap
     * @param caseSensitive
     * @return
     */
    public static UnmodifiableMultivaluedMap<String, String> get(
            MultivaluedMap<String, String> mmap, boolean caseSensitive) {
        if (mmap instanceof UnmodifiableMultivaluedMap) {
            return (UnmodifiableMultivaluedMap<String, String>) mmap;
        }
        if (mmap instanceof MultivaluedMapImpl) {
            return new UnmodifiableMultivaluedMap<String, String>(
                    (MultivaluedMapImpl<String, String>) mmap, caseSensitive);
        }
        return new UnmodifiableMultivaluedMap<String, String>(
                new MultivaluedMapImpl<String, String>(mmap), caseSensitive);
    }

    /**
     * Creates an UnmodifiableMultivaluedMap&lt;String, String;&gt; from the
     * given Form.
     * 
     * @param form
     * @param caseSensitive
     * @return
     */
    public static UnmodifiableMultivaluedMap<String, String> getFromForm(
            Form form, boolean caseSensitive) {
        return new UnmodifiableMultivaluedMap<String, String>(copyForm(form,
                !caseSensitive), caseSensitive);
    }

    private final MultivaluedMapImpl<K, V> mmap;

    private final boolean caseInsensitive;

    /**
     * Creates a new unmodifiable {@link MultivaluedMap}.
     * 
     * @param mmap
     * @param caseSensitive
     */
    private UnmodifiableMultivaluedMap(MultivaluedMapImpl<K, V> mmap,
            boolean caseSensitive) {
        this.mmap = mmap;
        this.caseInsensitive = !caseSensitive;
    }

    @Deprecated
    public void add(K key, V value) {
        throw throwUnmodifiable();
    }

    private Object caseInsensitive(Object key) {
        if (this.caseInsensitive && (key != null)) {
            key = key.toString().toLowerCase();
        }
        return key;
    }

    @Deprecated
    public void clear() throws UnsupportedOperationException {
        throw throwUnmodifiable();
    }

    public boolean containsKey(Object key) {
        if (this.caseInsensitive && (key != null)) {
            this.mmap.containsKey(caseInsensitive(key.toString()));
        }
        return this.mmap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        if (value instanceof List) {
            return this.mmap.containsValue(value);
        }
        for (final List<V> vList : this.mmap.values()) {
            if (vList.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public Set<java.util.Map.Entry<K, List<V>>> entrySet() {
        return Collections.unmodifiableSet(this.mmap.entrySet());
    }

    @Override
    public boolean equals(Object anotherObect) {
        if (anotherObect == this) {
            return true;
        }
        if (!(anotherObect instanceof MultivaluedMap)) {
            return false;
        }
        return this.mmap.equals(anotherObect);
    }

    public List<V> get(Object key) {
        return Collections
                .unmodifiableList(this.mmap.get(caseInsensitive(key)));
    }

    @SuppressWarnings("unchecked")
    public V getFirst(K key) {
        if (this.caseInsensitive && (key instanceof String)) {
            key = (K) key.toString().toLowerCase();
        }
        return this.mmap.getFirst(key);
    }

    /**
     * Returns the last element for the given key.
     * 
     * @param key
     * @return Returns the last element for the given key.
     */
    @SuppressWarnings("unchecked")
    public V getLast(K key) {
        if (this.caseInsensitive && (key instanceof String)) {
            key = (K) key.toString().toLowerCase();
        }
        return this.mmap.getLast(key);
    }

    @Override
    public int hashCode() {
        int hashCode = this.mmap.hashCode();
        if (this.caseInsensitive) {
            hashCode++;
        }
        return hashCode;
    }

    public boolean isEmpty() {
        return this.mmap.isEmpty();
    }

    public Set<K> keySet() {
        return Collections.unmodifiableSet(this.mmap.keySet());
    }

    @Deprecated
    public List<V> put(K key, List<V> value)
            throws UnsupportedOperationException {
        throw throwUnmodifiable();
    }

    @Deprecated
    public void putAll(Map<? extends K, ? extends List<V>> t)
            throws UnsupportedOperationException {
        throw throwUnmodifiable();
    }

    @Deprecated
    public void putSingle(K key, V value) throws UnsupportedOperationException {
        throw throwUnmodifiable();
    }

    @Deprecated
    public List<V> remove(Object key) throws UnsupportedOperationException {
        throw throwUnmodifiable();
    }

    public int size() {
        int size = 0;
        for (final List<V> l : this.mmap.values()) {
            size += l.size();
        }
        return size;
    }

    /**
     * @throws UnsupportedOperationException
     */
    private UnsupportedOperationException throwUnmodifiable()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }

    public Collection<List<V>> values() {
        return Collections.unmodifiableCollection(this.mmap.values());
    }
}