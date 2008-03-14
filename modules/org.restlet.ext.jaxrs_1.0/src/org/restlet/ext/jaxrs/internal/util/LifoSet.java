/*
 * Created on 05.11.2003
 */
package org.restlet.ext.jaxrs.internal.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * In this {@link Set} the Iterator first return the objects that are add last
 * added (last in, first out). This {@link Set} has therefore some {@link List}
 * trait, because the Elements are ordered.
 * 
 * @author Stephan Koops
 * @param <E>
 *                The type of the data in this Set.
 * @see Set
 */
public class LifoSet<E> implements Set<E> {
    private List<E> list;

    /**
     * Creates a LifoSet. If useGivenList is true, the given List is used as
     * data list, without reordering or anything else. If useGivenList is false,
     * a new data List is created and the order reversed.
     * 
     * @param data
     * @param useGivenList
     */
    protected LifoSet(List<E> data, boolean useGivenList) {
        if (useGivenList) {
            this.list = data;
        } else {
            this.list = new LinkedList<E>();
            this.list.addAll(data);
        }
    }

    /**
     * Creates an new LifoSet
     */
    public LifoSet() {
        this.list = new LinkedList<E>();
    }

    /**
     * 
     * @param c
     *                Collection&lt;E&gt;
     */
    public LifoSet(Collection<E> c) {
        this();
        addAll(c);
    }

    /**
     * @see java.util.Collection#size()
     */
    public int size() {
        return list.size();
    }

    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return list.contains(o);
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator<E> iterator() {
        return list.iterator();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return list.toArray();
    }

    /**
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    /**
     * Adds the element to the list.
     * 
     * @return true, if the Element was added (Collection changed) or false, if
     *         it was alreadey contained (Collection not changed).
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E object) {
        if (this.contains(object))
            return false;
        if (!(list instanceof LinkedList))
            this.list = new LinkedList<E>(this.list);
        ((LinkedList<E>) list).addFirst(object);
        return true;
    }

    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return list.remove(o);
    }

    /**
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    /**
     * @see java.util.Collection#addAll(java.util.Collection)
     * @return true, if this Set was changed.
     * @param coll
     *                Collection&lt;? extends E&gt;
     */
    public boolean addAll(Collection<? extends E> coll) {
        boolean changed = false;
        Iterator<? extends E> iter = coll.iterator();
        while (iter.hasNext()) {
            changed |= this.add(iter.next());
        }
        return changed;
    }

    /**
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    /**
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear() {
        list.clear();
    }

    @Override
    public String toString() {
        return this.list.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LifoSet))
            return false;
        return this.list.equals(((LifoSet<?>) object).list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    /**
     * Returns the element at the given index.
     * 
     * @param index
     * @return the element at the given index.
     * @throws IndexOutOfBoundsException
     * @see java.util.List#get(int)
     */
    public E get(int index) throws IndexOutOfBoundsException {
        return this.list.get(index);
    }
}