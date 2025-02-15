/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
 * @param <E>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
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
