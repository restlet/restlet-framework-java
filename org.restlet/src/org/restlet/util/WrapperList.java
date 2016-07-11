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

package org.restlet.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * List wrapper. Modifiable list that delegates all methods to a wrapped list.
 * This allows an easy sub-classing. By default, it wraps a thread-safe
 * {@link Vector} instance.
 * 
 * @author Jerome Louvel
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 * @see java.util.Collections
 * @see java.util.List
 */
public class WrapperList<E> implements List<E>, Iterable<E> {
    /** The delegate list. */
    private final List<E> delegate;

    /**
     * Constructor. Uses a default initial capacity of 10 items.
     */
    public WrapperList() {
        this(10);
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity
     *            The initial list capacity.
     */
    public WrapperList(int initialCapacity) {
        this(new Vector<E>(initialCapacity));
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *            The delegate list.
     */
    public WrapperList(List<E> delegate) {
        this.delegate = delegate;
    }

    /**
     * Adds a element at the end of the list.
     * 
     * @return True (as per the general contract of the Collection.add method).
     */
    public boolean add(E element) {
        return getDelegate().add(element);
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * 
     * @param index
     *            The insertion position.
     * @param element
     *            The element to insert.
     */
    public void add(int index, E element) {
        getDelegate().add(index, element);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list.
     * 
     * @param elements
     *            The collection of elements to append.
     */
    public boolean addAll(Collection<? extends E> elements) {
        return getDelegate().addAll(elements);
    }

    /**
     * Inserts all of the elements in the specified collection into this list at
     * the specified position.
     * 
     * @param index
     *            The insertion position.
     * @param elements
     *            The collection of elements to insert.
     */
    public boolean addAll(int index, Collection<? extends E> elements) {
        return getDelegate().addAll(index, elements);
    }

    /**
     * Removes all of the elements from this list.
     */
    public void clear() {
        getDelegate().clear();
    }

    /**
     * Returns true if this list contains the specified element.
     * 
     * @param element
     *            The element to find.
     * @return True if this list contains the specified element.
     */
    public boolean contains(Object element) {
        return getDelegate().contains(element);
    }

    /**
     * Returns true if this list contains all of the elements of the specified
     * collection.
     * 
     * @param elements
     *            The collection of elements to find.
     * @return True if this list contains all of the elements of the specified
     *         collection.
     */
    public boolean containsAll(Collection<?> elements) {
        return getDelegate().containsAll(elements);
    }

    /**
     * Compares the specified object with this list for equality.
     * 
     * @param o
     *            The object to be compared for equality with this list.
     * @return True if the specified object is equal to this list.
     */
    @Override
    public boolean equals(Object o) {
        return getDelegate().equals(o);
    }

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index
     *            The element position.
     * @return The element at the specified position in this list.
     */
    public E get(int index) {
        return getDelegate().get(index);
    }

    /**
     * Returns the delegate list.
     * 
     * @return The delegate list.
     */
    protected List<E> getDelegate() {
        return this.delegate;
    }

    /**
     * Returns the hash code value for this list.
     * 
     * @return The hash code value for this list.
     */
    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     * 
     * @param element
     *            The element to find.
     * @return The index of the first occurrence.
     */
    public int indexOf(Object element) {
        return getDelegate().indexOf(element);
    }

    /**
     * Returns true if this list contains no elements.
     */
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * 
     * @return An iterator over the elements in this list in proper sequence.
     */
    public Iterator<E> iterator() {
        return getDelegate().iterator();
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object element) {
        return getDelegate().lastIndexOf(element);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence).
     * 
     * @return A list iterator of the elements in this list (in proper
     *         sequence).
     */
    public ListIterator<E> listIterator() {
        return getDelegate().listIterator();
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.
     * 
     * @param index
     *            The starting position.
     */
    public ListIterator<E> listIterator(int index) {
        return getDelegate().listIterator(index);
    }

    /**
     * Removes the element at the specified position in this list.
     * 
     * @return The removed element.
     */
    public E remove(int index) {
        return getDelegate().remove(index);
    }

    /**
     * Removes the first occurrence in this list of the specified element.
     * 
     * @return True if the list was changed.
     */
    public boolean remove(Object element) {
        return getDelegate().remove(element);
    }

    /**
     * Removes from this list all the elements that are contained in the
     * specified collection.
     * 
     * @param elements
     *            The collection of element to remove.
     * @return True if the list changed.
     */
    public boolean removeAll(Collection<?> elements) {
        return getDelegate().removeAll(elements);
    }

    /**
     * RemovesRetains only the elements in this list that are contained in the
     * specified collection.
     * 
     * @param elements
     *            The collection of element to retain.
     * @return True if the list changed.
     */
    public boolean retainAll(Collection<?> elements) {
        return getDelegate().retainAll(elements);
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     * 
     * @param index
     *            The position of the element to replace.
     * @param element
     *            The new element.
     */
    public E set(int index, E element) {
        return getDelegate().set(index, element);
    }

    /**
     * Returns the number of elements in this list.
     * 
     * @return The number of elements in this list.
     */
    public int size() {
        return getDelegate().size();
    }

    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     * 
     * @param fromIndex
     *            The start position.
     * @param toIndex
     *            The end position (exclusive).
     * @return The sub-list.
     */
    public List<E> subList(int fromIndex, int toIndex) {
        // [ifndef gwt] instruction
        return new WrapperList<E>(getDelegate().subList(fromIndex, toIndex));
        // [ifdef gwt] instruction uncomment
        // return org.restlet.engine.util.ListUtils.copySubList(this, fromIndex,
        // toIndex);
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence.
     * 
     * @return An array containing all of the elements in this list in proper
     *         sequence.
     */
    public Object[] toArray() {
        return getDelegate().toArray();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the specified
     * array.
     * 
     * @param a
     *            The sample array.
     */
    public <T> T[] toArray(T[] a) {
        return getDelegate().toArray(a);
    }

    /**
     * Returns a string representation of the list.
     * 
     * @return A string representation of the list.
     */
    @Override
    public String toString() {
        return getDelegate().toString();
    }
}
