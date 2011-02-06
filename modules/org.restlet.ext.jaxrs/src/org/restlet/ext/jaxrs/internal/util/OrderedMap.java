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

package org.restlet.ext.jaxrs.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;

/**
 * This Map contains its values in the given order.<br>
 * Multiple values for one key is allowed.
 * 
 * @author Stephan Koops
 * @param <K>
 * @param <V>
 */
public class OrderedMap<K, V> implements Map<K, V> {

    private static class Entry<K, V> implements Map.Entry<K, V> {

        private final K key;

        private V value;

        /**
         * @param key
         * @param value
         */
        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * @see java.util.Map.Entry#getKey()
         */
        public K getKey() {
            return this.key;
        }

        /**
         * @see java.util.Map.Entry#getValue()
         */
        public V getValue() {
            return this.value;
        }

        /**
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        public V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        @Override
        public String toString() {
            return this.key + " -> " + this.value;
        }
    }

    private final class KeySetIterator<KK, VV> extends
            KeyValueSetIterator<KK, VV> implements Iterator<KK> {

        private KeySetIterator(Iterator<Map.Entry<KK, VV>> entryIter) {
            super(entryIter);
        }

        public KK next() {
            return this.entryIter.next().getKey();
        }
    }

    /**
     * @author Stephan Koops
     */
    private abstract class KeyValueSetIterator<KK, VV> {

        final Iterator<Map.Entry<KK, VV>> entryIter;

        KeyValueSetIterator(Iterator<Map.Entry<KK, VV>> entryIter) {
            this.entryIter = entryIter;
        }

        /** @return the next element
         *  @see Iterator#hasNext() */
        public final boolean hasNext() {
            return this.entryIter.hasNext();
        }

        /** @see Iterator#remove() */
        public final void remove() {
            this.entryIter.remove();
        }
    }

    /**
     * This list has the interface of a Set, but wraps a List
     * 
     * @author Stephan Koops
     */
    private static final class ListWrappingSet<E> implements Set<E> {

        private final List<E> elements;

        private ListWrappingSet(List<E> elements) {
            this.elements = elements;
        }

        public boolean add(E o) {
            return elements.add(o);
        }

        public boolean addAll(Collection<? extends E> c) {
            return elements.addAll(c);
        }

        public void clear() {
            elements.clear();
        }

        public boolean contains(Object o) {
            return elements.contains(o);
        }

        public boolean containsAll(Collection<?> c) {
            return elements.containsAll(c);
        }

        public boolean isEmpty() {
            return elements.isEmpty();
        }

        public Iterator<E> iterator() {
            return elements.iterator();
        }

        public boolean remove(Object o) {
            return elements.remove(o);
        }

        public boolean removeAll(Collection<?> c) {
            return elements.removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            return elements.retainAll(c);
        }

        public int size() {
            return elements.size();
        }

        public Object[] toArray() {
            return elements.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return elements.toArray(a);
        }
    }

    private final class ValueSetIterator<KK, VV> extends
            KeyValueSetIterator<KK, VV> implements Iterator<VV> {

        private ValueSetIterator(Iterator<Map.Entry<KK, VV>> entryIter) {
            super(entryIter);
        }

        public VV next() {
            return this.entryIter.next().getValue();
        }
    }

    private final List<Map.Entry<? extends K, ? extends V>> elements = new ArrayList<Map.Entry<? extends K, ? extends V>>();

    /**
     * adds the given entry.
     * 
     * @param key
     * @param value
     * @return ever true (the value is added ever)
     */
    public boolean add(K key, V value) {
        this.elements.add(new Entry<K, V>(key, value));
        return true;
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        this.elements.clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        // NICE OrderedMap.containsKey()
        throw new UnsupportedOperationException("OrderedMap.containsKey()");
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        // NICE OrderedMap.containsValue()
        throw new UnsupportedOperationException("OrderedMap.containsValue()");
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<Map.Entry<K, V>> entrySet() {
        return new ListWrappingSet<Map.Entry<K, V>>((List) this.elements);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get(Object key) {
        // NICE OrderedMap.get()
        throw new UnsupportedOperationException("OrderedMap.get()");
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {
        return new Set<K>() {

            public boolean add(K o) {
                throw new UnsupportedOperationException(
                        "the addition of keys only to a map is not possible");
            }

            public boolean addAll(Collection<? extends K> c) {
                throw new UnsupportedOperationException(
                        "the addition of keys only to a map is not possible");
            }

            public void clear() {
                elements.clear();
            }

            public boolean contains(Object o) {
                return OrderedMap.this.containsKey(o);
            }

            public boolean containsAll(Collection<?> c) {
                for (Object e : c)
                    if (!OrderedMap.this.containsKey(e))
                        return false;
                return true;
            }

            public boolean isEmpty() {
                return elements.isEmpty();
            }

            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Iterator<K> iterator() {
                return new KeySetIterator<K, V>((Iterator) elements.iterator());
            }

            public boolean remove(Object o) {
                throw new NotYetImplementedException();
            }

            public boolean removeAll(Collection<?> c) {
                throw new NotYetImplementedException();
            }

            public boolean retainAll(Collection<?> c) {
                throw new NotYetImplementedException();
            }

            public int size() {
                return elements.size();
            }

            public Object[] toArray() {
                throw new NotYetImplementedException();
            }

            public <T> T[] toArray(T[] a) {
                throw new NotYetImplementedException();
            }
        };
    }

    /**
     * adds the given entry. Will ever return {@code null}, also if there is an
     * element with the given value.
     * 
     * @see Map#put(Object, Object)
     * @see #add(Object, Object)
     */
    public V put(K key, V value) {
        this.add(key, value);
        return null;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            this.elements.add(e);
        }
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {
        // NICE OrderedMap.remove()
        throw new UnsupportedOperationException("OrderedMap.remove()");
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return this.elements.size();
    }

    @Override
    public String toString() {
        return this.elements.toString();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<V> values() {
        return new Collection<V>() {

            public boolean add(V o) {
                throw new UnsupportedOperationException(
                        "the addition of values only to a map is not possible");
            }

            public boolean addAll(Collection<? extends V> c) {
                throw new UnsupportedOperationException(
                        "the addition of values only to a map is not possible");
            }

            public void clear() {
                elements.clear();
            }

            public boolean contains(Object value) {
                return OrderedMap.this.containsValue(value);
            }

            public boolean containsAll(Collection<?> c) {
                for (Object v : c)
                    if (!OrderedMap.this.containsValue(v))
                        return false;
                return true;
            }

            public boolean isEmpty() {
                return elements.isEmpty();
            }

            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Iterator<V> iterator() {
                return new ValueSetIterator<K, V>((Iterator) elements
                        .iterator());
            }

            public boolean remove(Object o) {
                throw new NotYetImplementedException();
            }

            public boolean removeAll(Collection<?> c) {
                boolean removed = false;
                for (Object o : c)
                    removed |= this.remove(o);
                return removed;
            }

            public boolean retainAll(Collection<?> c) {
                throw new NotYetImplementedException();
            }

            public int size() {
                return elements.size();
            }

            public Object[] toArray() {
                throw new NotYetImplementedException();
            }

            public <T> T[] toArray(T[] a) {
                throw new NotYetImplementedException();
            }
        };
    }
}