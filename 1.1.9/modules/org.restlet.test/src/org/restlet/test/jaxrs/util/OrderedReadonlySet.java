/*
 * Copyright 2005-2008 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.test.jaxrs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Stephan
 *
 */
public class OrderedReadonlySet<E> implements Set<E> {

    private final List<E> elements;
    
    /**
     * 
     * @param data will not check, if no duplicates are in the data
     */
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