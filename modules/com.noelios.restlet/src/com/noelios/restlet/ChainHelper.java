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

package com.noelios.restlet;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.service.LogService;
import org.restlet.util.Helper;

/**
 * Chain helper serving as base class for Application and Component helpers.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class ChainHelper extends Helper {
    /** The first Restlet. */
    private Restlet first;

    /** The last Filter. */
    private Filter last;

    /** The parent context, typically the component's context. */
    private Context parentContext;

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The parent context, typically the component's context.
     */
    public ChainHelper(Context parentContext) {
        this.parentContext = parentContext;
        this.first = null;
    }

    /**
     * Returns the parent context, typically the component's context.
     * 
     * @return The parent context.
     */
    public Context getParentContext() {
        return this.parentContext;
    }

    /**
     * Adds a new filter to the chain.
     * 
     * @param filter
     *            The filter to add.
     */
    protected void addFilter(Filter filter) {
        if (getLast() != null) {
            getLast().setNext(filter);
            setLast(filter);
        } else {
            setFirst(filter);
            setLast(filter);
        }
    }

    /**
     * Creates a new log filter. Allows overriding.
     * 
     * @param context
     *            The context.
     * @param logService
     *            The log service descriptor.
     * @return The new log filter.
     */
    protected Filter createLogFilter(Context context, LogService logService) {
        return new LogFilter(context, logService);
    }

    /**
     * Returns the first Restlet.
     * 
     * @return the first Restlet.
     */
    protected Restlet getFirst() {
        return this.first;
    }

    /**
     * Sets the first Restlet.
     * 
     * @param first
     *            The first Restlet.
     */
    protected void setFirst(Restlet first) {
        this.first = first;
    }

    /**
     * Returns the last Filter.
     * 
     * @return the last Filter.
     */
    private Filter getLast() {
        return this.last;
    }

    /**
     * Sets the last Filter.
     * 
     * @param last
     *            The last Filter.
     */
    private void setLast(Filter last) {
        this.last = last;
    }

}
