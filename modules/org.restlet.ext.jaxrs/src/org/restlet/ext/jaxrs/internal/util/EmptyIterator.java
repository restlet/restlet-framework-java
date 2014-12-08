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
    public static final EmptyIterator<?> INSTANCE = new EmptyIterator<Object>();

    /**
     * returns an Iterator with no elements.
     * 
     * @param <A>
     * @return an empty iterator
     */
    @SuppressWarnings("unchecked")
    public static <A> EmptyIterator<A> get() {
        return (EmptyIterator<A>) INSTANCE;
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
