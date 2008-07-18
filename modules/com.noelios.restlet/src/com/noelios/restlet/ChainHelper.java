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

package com.noelios.restlet;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.service.LogService;
import org.restlet.util.Helper;

/**
 * Chain helper serving as base class for Application and Component helpers.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class ChainHelper<T extends Restlet> extends Helper<T> {
    /** The first Restlet. */
    private volatile Restlet first;

    /** The last Filter. */
    private volatile Filter last;

    /** The parent context, typically the component's context. */
    private volatile Context parentContext;

    /**
     * Constructor.
     * 
     * @param helped
     *            The helped Restlet.
     */
    public ChainHelper(T helped) {
        this(helped, null);
    }

    /**
     * Constructor.
     * 
     * @param helpedRestlet
     *            The helped Restlet.
     * @param parentContext
     *            The parent context, typically the component's context.
     */
    public ChainHelper(T helpedRestlet, Context parentContext) {
        super(helpedRestlet);
        this.parentContext = parentContext;
        this.first = null;
    }

    /**
     * Adds a new filter to the chain.
     * 
     * @param filter
     *            The filter to add.
     */
    protected synchronized void addFilter(Filter filter) {
        if (getLast() != null) {
            getLast().setNext(filter);
            setLast(filter);
        } else {
            setFirst(filter);
            setLast(filter);
        }
    }

    /**
     * Clears the chain. Sets the first and last filters to null.
     */
    public void clear() {
        setFirst(null);
        setNext(null);
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
     * Returns the last Filter.
     * 
     * @return the last Filter.
     */
    protected Filter getLast() {
        return this.last;
    }

    /**
     * Returns the parent context, typically the component's context.
     * 
     * @return The parent context.
     */
    public Context getParentContext() {
        return this.parentContext;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getFirst() != null) {
            getFirst().handle(request, response);
        } else {
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            getHelped()
                    .getLogger()
                    .log(
                            Level.SEVERE,
                            "The "
                                    + getHelped().getClass().getName()
                                    + " class has no Restlet defined to process calls. Maybe it wasn't properly started.");
        }
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
     * Sets the last Filter.
     * 
     * @param last
     *            The last Filter.
     */
    protected void setLast(Filter last) {
        this.last = last;
    }

    /**
     * Sets the next Restlet after the chain.
     * 
     * @param next
     *            The Restlet to process after the chain.
     */
    protected synchronized void setNext(Restlet next) {
        if (getFirst() == null) {
            setFirst(next);
        } else {
            getLast().setNext(next);
        }
    }

}
