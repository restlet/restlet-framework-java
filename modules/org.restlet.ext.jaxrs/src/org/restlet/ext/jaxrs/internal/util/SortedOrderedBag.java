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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A sorted ordered bag allows multiple elements, where the {@link Comparator}
 * says that they are equal. In this case an element added later than another
 * element is put behind the first added element.
 * 
 * @author Stephan Koops
 * @param <E>
 */
public class SortedOrderedBag<E> implements Collection<E> {

    private static final class Compar<A extends Comparable<A>> implements
            Comparator<A> {

        /***/
        public Compar() {
        }

        public int compare(A o1, A o2) {
            return o1.compareTo(o2);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final Comparator<?> DEFAULT_COMPARATOR = new Compar();

    private final Comparator<E> comp;

    private final LinkedList<E> elements = new LinkedList<E>();

    /**
     * Creates a new sorted ordered bag.
     */
    @SuppressWarnings("unchecked")
    public SortedOrderedBag() {
        this.comp = (Comparator<E>) DEFAULT_COMPARATOR;
    }

    /**
     * Creates a new sorted ordered bag.
     * 
     * @param coll
     */
    @SuppressWarnings("unchecked")
    public SortedOrderedBag(Collection<E> coll) {
        this.comp = (Comparator<E>) DEFAULT_COMPARATOR;
        this.addAll(coll);
    }

    /**
     * Creates a new sorted ordered bag.
     * 
     * @param comp
     *            the {@link Comparator} to use
     */
    public SortedOrderedBag(Comparator<E> comp) {
        this.comp = comp;
    }

    /**
     * Creates a new sorted ordered bag.
     * 
     * @param comp
     *            the {@link Comparator} to use
     * @param coll
     */
    public SortedOrderedBag(Comparator<E> comp, Collection<E> coll) {
        this.comp = comp;
        this.addAll(coll);
    }

    /**
     * uses bubble sort
     * 
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E add) {
        if (this.elements.isEmpty()) {
            this.elements.add(add);
            return true;
        }
        final ListIterator<E> listIter;
        listIter = this.elements.listIterator(this.elements.size());
        while (listIter.hasPrevious()) {
            E current = listIter.previous();
            if (comp.compare(add, current) >= 0) {
                listIter.next();
                listIter.add(add);
                return true;
            }
        }
        this.elements.addFirst(add);
        return true;
    }

    /**
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
        for (E e : c)
            this.add(e);
        return true;
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear() {
        this.elements.clear();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return this.elements.contains(o);
    }

    /**
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return this.elements.containsAll(c);
    }

    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return this.elements.isEmpty();
    }

    /**
     * @param index
     * @return the element at the given position
     * @see java.util.List#get(int)
     */
    public E get(int index) {
        return this.elements.get(index);
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator<E> iterator() {
        return this.elements.iterator();
    }

    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return this.elements.remove(o);
    }

    /**
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        return this.elements.removeAll(c);
    }

    /**
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return this.elements.retainAll(c);
    }

    /**
     * @see java.util.Collection#size()
     */
    public int size() {
        return this.elements.size();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return this.elements.toArray();
    }

    /**
     * @see java.util.Collection#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        return this.elements.toArray(a);
    }

    @Override
    public String toString() {
        return this.elements.toString();
    }
}