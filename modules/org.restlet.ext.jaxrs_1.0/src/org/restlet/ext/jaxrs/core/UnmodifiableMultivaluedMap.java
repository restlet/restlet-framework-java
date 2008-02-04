package org.restlet.ext.jaxrs.core;

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
                caseSensitive), caseSensitive);
    }

    /**
     * Creates a new unmodifiable {@link MultivaluedMap}.
     * 
     * @param mmap
     * @param caseSensitive
     */
    public UnmodifiableMultivaluedMap(MultivaluedMap<K, V> mmap,
            boolean caseSensitive) {
        this.map = mmap;
        this.caseInsensitive = !caseSensitive;
    }

    private MultivaluedMap<K, V> map;

    private boolean caseInsensitive;

    // TODO TESTEN: Http-Headers soll Case-insensitiv sein

    @Deprecated
    @SuppressWarnings("unused")
    public void add(K key, V value) {
        throw unmodifiable();
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void clear() throws UnsupportedOperationException {
        throw unmodifiable();
    }

    public boolean containsKey(Object key) {
        if (!(key instanceof String))
            return false;
        if (caseInsensitive)
            key = ((String) key).toLowerCase();
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        if (value instanceof List) {
            return map.containsValue(value);
        }
        for (List<V> vList : map.values())
            if (vList.contains(value))
                return true;
        return false;
    }

    public Set<java.util.Map.Entry<K, List<V>>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    public List<V> get(Object key) {
        return Collections.unmodifiableList(map.get(key));
    }

    public V getFirst(K key) {
        return map.getFirst(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<K> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Deprecated
    @SuppressWarnings("unused")
    public List<V> put(K key, List<V> value)
            throws UnsupportedOperationException {
        throw unmodifiable();
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void putAll(Map<? extends K, ? extends List<V>> t)
            throws UnsupportedOperationException {
        throw unmodifiable();
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void putSingle(K key, V value) throws UnsupportedOperationException {
        throw unmodifiable();
    }

    @Deprecated
    @SuppressWarnings("unused")
    public List<V> remove(Object key) throws UnsupportedOperationException {
        throw unmodifiable();
    }

    public int size() {
        int size = 0;
        for (List<V> l : map.values())
            size += l.size();
        return size;
    }

    /**
     * @throws UnsupportedOperationException
     */
    private UnsupportedOperationException unmodifiable()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The HTTP headers are immutable");
    }

    public Collection<List<V>> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    /**
     * Creates a MultiValuedMap of unmodifiable Lists.
     */
    private static MultivaluedMap<String, String> copyForm(Form form,
            boolean caseInsensitive) {
        MultivaluedMap<String, String> mmap = new MultivaluedMapImpl<String, String>();
        for (Parameter param : form) {
            String key = caseInsensitive ? param.getName().toLowerCase()
                    : param.getName();
            mmap.add(key, param.getValue());
        }
        for (Map.Entry<String, List<String>> entry : mmap.entrySet()) {
            List<String> unmodifiable = Collections.unmodifiableList(entry
                    .getValue());
            mmap.put(entry.getKey(), unmodifiable);
        }
        return mmap;
    }
}