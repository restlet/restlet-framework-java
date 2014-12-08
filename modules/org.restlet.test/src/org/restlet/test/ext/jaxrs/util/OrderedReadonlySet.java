/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Stephan Koops
 */
public class OrderedReadonlySet<E> implements Set<E> {

    private final List<E> elements;

    /**
     * 
     * @param data
     *            will not check, if no duplicates are in the data
     */
    @SafeVarargs
    public OrderedReadonlySet(E... data) {
        this.elements = Arrays.asList(data);
    }

    /**
     * @see java.util.Set#add(java.lang.Object)
     */
    public boolean add(E o) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    /**
     * @see java.util.Set#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    /**
     * @see java.util.Set#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException("unmodifiable");
    }

    /**
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * @see java.util.Set#iterator()
     */
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    /**
     * @see java.util.Set#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    /**
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    /**
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    /**
     * @see java.util.Set#size()
     */
    public int size() {
        return elements.size();
    }

    /**
     * @see java.util.Set#toArray()
     */
    public Object[] toArray() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * @see java.util.Set#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
