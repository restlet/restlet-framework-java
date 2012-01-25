/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaxrs.internal.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator that iterates over exact one element.
 * 
 * @author Stephan Koops
 * @param <T>
 *            The type of the contained object.
 */
public class OneElementIterator<T> implements Iterator<T> {

    private final T element;

    private boolean hasNext = true;

    /**
     * @param element
     *            The element to iterate over. May be null.
     */
    public OneElementIterator(T element) {
        this.element = element;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return this.hasNext;
    }

    /**
     * @see java.util.Iterator#next()
     */
    public T next() throws NoSuchElementException {
        if (!this.hasNext) {
            throw new NoSuchElementException("The element was already returned");
        }
        this.hasNext = false;
        return this.element;
    }

    /**
     * @see java.util.Iterator#remove()
     * @throws UnsupportedOperationException
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The OneElementIterator is not modifiable");
    }
}
