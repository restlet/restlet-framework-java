/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.ext.jaxrs.util;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/*
 * Created on 12.06.2004
 */

/**
 * An ArrayIterator iterates over an array. If elements in the arrays are
 * changed, than this ArrayIterator will ignore it and continue if not happens.
 * 
 * @author Stephan Koops
 * @param <E>
 */
public class ArrayIterator<E> implements ListIterator<E> {
    private E[] array;

    /**
     * if you decremt 0.5, you have the position between this iterator is.
     */
    private int counter = 0;

    private int lastReturnedIndex = Integer.MIN_VALUE;

    /**
     * Creates a new ArrayIterator
     * 
     * @param array
     *                array to iterate over.
     */
    public ArrayIterator(E[] array) {
        this.array = array;
    }

    /**
     * Creates a new ArrayIterator
     * 
     * @param array
     *                array to iterate over.
     * @param startIndex
     *                index to start iterate.
     * @throws IndexOutOfBoundsException
     *                 if the startindex is invalid.
     */
    public ArrayIterator(E[] array, int startIndex)
            throws IndexOutOfBoundsException {
        if (startIndex < 0)
            throw new IndexOutOfBoundsException(
                    "The startIndex must be greater or equal zero");
        if (startIndex > array.length)
            throw new IndexOutOfBoundsException(
                    "The startIndex must not be greater than the array length");
        this.array = array;
        this.counter = startIndex;
    }

    /**
     * @throws UnsupportedOperationException
     *                 immer
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "You can not change the number of elements in the underlying array");
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return counter < array.length;
    }

    /**
     * @see java.util.Iterator#next()
     */
    public E next() throws NoSuchElementException {
        if (counter >= array.length)
            throw new NoSuchElementException("There are only " + array.length
                    + " elements");
        this.lastReturnedIndex = counter;
        E obj = array[counter];
        counter++;
        return obj;
    }

    /**
     * Creates a new ArrayIterator, that continues at the same position at this
     * Iterator.
     * 
     * @see java.lang.Object#clone()
     */
    public ArrayIterator<E> clone() {
        ArrayIterator<E> arrayIterator = new ArrayIterator<E>(this.array);
        arrayIterator.counter = this.counter;
        return arrayIterator;
    }

    public void add(E e) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "You can not change the number of elements in the underlying array");
    }

    public boolean hasPrevious() {
        return counter > 0;
    }

    public int nextIndex() {
        return counter;
    }

    public E previous() {
        if (counter < 0)
            throw new NoSuchElementException("index is already 0");
        counter--;
        this.lastReturnedIndex = counter;
        E obj = array[counter];
        return obj;
    }

    public int previousIndex() {
        return counter - 1;
    }

    public void set(E e) {
        if (lastReturnedIndex == Integer.MIN_VALUE) // not yet set.
            throw new IllegalStateException("You haven't read an element yet");
        this.array[lastReturnedIndex] = e;
    }
}