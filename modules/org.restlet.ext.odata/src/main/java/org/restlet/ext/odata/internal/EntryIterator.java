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

package org.restlet.ext.odata.internal;

import java.util.Iterator;

import org.restlet.data.Reference;
import org.restlet.ext.odata.Query;
import org.restlet.ext.odata.Service;

/**
 * Iterator that transparently supports sever-side paging.
 * 
 * @author Thierry Boileau
 * 
 * @param <T>
 */
public class EntryIterator<E> implements Iterator<E> {

    /** The class of the listed objects. */
    private Class<?> entityClass;

    /** The inner iterator. */
    private Iterator<E> iterator;

    /** The reference to the next page. */
    private Reference nextPage;

    /** The underlying service. */
    private Service service;

    /**
     * Constructor.
     * 
     * @param service
     *            The underlying service.
     * @param iterator
     *            The inner iterator.
     * @param nextPage
     *            The reference to the next page.
     * @param entityClass
     *            The class of the listed objects.
     */
    public EntryIterator(Service service, Iterator<E> iterator,
            Reference nextPage, Class<?> entityClass) {
        super();
        this.iterator = iterator;
        this.nextPage = nextPage;
        this.service = service;
        this.entityClass = entityClass;
    }

    @SuppressWarnings("unchecked")
    public boolean hasNext() {
        boolean result = false;

        if (iterator != null) {
            result = iterator.hasNext();
        }

        if (!result && nextPage != null) {
            // Get the next page.
            Query<E> query = service.createQuery(nextPage.toString(),
                    (Class<E>) entityClass);
            iterator = query.iterator();

            if (iterator != null) {
                result = iterator.hasNext();
            }

            // Set the reference to the next page
            nextPage = null;
        }

        return result;
    }

    public E next() {
        E result = null;
        if (iterator != null) {
            if (iterator.hasNext()) {
                result = iterator.next();
            }
        }
        return result;
    }

    public void remove() {
        if (iterator != null) {
            iterator.remove();
        }
    }
}
