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
package org.restlet.ext.jaxrs.internal.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator without any elements.
 * 
 * @author Stephan Koops
 * @param <T>
 */
public final class EmptyIterator<T> implements Iterator<T> {

    /**
     * Iterator without any element.
     */
    @SuppressWarnings("deprecation")
    public static final EmptyIterator<?> INSTANCE = new EmptyIterator<Object>();

    /**
     * returns an Iterator with no elements.
     * 
     * @param <A>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <A> EmptyIterator<A> get() {
        return (EmptyIterator<A>) INSTANCE;
    }

    /**
     * @see #get()
     */
    @Deprecated
    public EmptyIterator() {
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return false;
    }

    /**
     * @see java.util.Iterator#next()
     * @throws NoSuchElementException
     *             immer, weil der EmptyIterator keine Werte hat
     */
    public T next() throws NoSuchElementException {
        throw new NoSuchElementException("The EmptyIterator has no values");
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}