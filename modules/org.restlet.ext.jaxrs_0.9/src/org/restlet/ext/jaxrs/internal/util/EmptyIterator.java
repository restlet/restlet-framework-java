/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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