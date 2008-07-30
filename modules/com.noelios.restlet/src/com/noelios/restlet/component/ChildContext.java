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

package com.noelios.restlet.component;

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Uniform;

/**
 * Context based on a parent component's context but dedicated to a child
 * Restlet, typically to an application.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ChildContext extends Context {

    /**
     * Returns the standard logger name for the given child Restlet.
     * 
     * @param child
     *            The child to log about.
     * @return The standard logger name.
     */
    public static String getLoggerName(Restlet child) {
        String result = null;

        if (child != null) {
            Context context = child.getContext();

            if (context != null) {
                result = child.getClass().getCanonicalName();

                if (result == null) {
                    result = "org.restlet.restlet (";
                }

                result = result + child.hashCode() + ")";
            }
        } else {
            result = "org.restlet.restlet";
        }

        return result;
    }

    /** The child delegate, typically an application. */
    private volatile Restlet child;

    /** The client dispatcher. */
    private volatile ChildClientDispatcher clientDispatcher;

    /** The parent context. */
    private volatile Context parentContext;

    /** The server dispatcher. */
    private volatile Uniform serverDispatcher;

    /**
     * Constructor.
     * 
     * @param child
     *            The child.
     * @param parentContext
     *            The parent context.
     */
    public ChildContext(Restlet child, Context parentContext) {
        this(child, parentContext, Logger.getLogger(getLoggerName(child)));
    }

    /**
     * Constructor.
     * 
     * @param child
     *            The child.
     * @param parentContext
     *            The parent context.
     * @param logger
     *            The logger instance of use.
     */
    public ChildContext(Restlet child, Context parentContext, Logger logger) {
        super(logger);
        this.child = child;
        this.parentContext = parentContext;
        this.clientDispatcher = new ChildClientDispatcher(this);
        this.serverDispatcher = (getParentContext() != null) ? getParentContext()
                .getServerDispatcher()
                : null;
    }

    /**
     * Returns the child.
     * 
     * @return the child.
     */
    public Restlet getChild() {
        return this.child;
    }

    @Override
    public ChildClientDispatcher getClientDispatcher() {
        return this.clientDispatcher;
    }

    /**
     * Returns the parent context.
     * 
     * @return The parent context.
     */
    protected Context getParentContext() {
        return this.parentContext;
    }

    @Override
    public Uniform getServerDispatcher() {
        return this.serverDispatcher;
    }

    /**
     * Sets the child.
     * 
     * @param child
     *            The child.
     */
    public void setChild(Restlet child) {
        this.child = child;
        setLogger(Logger.getLogger(getLoggerName(child)));
    }

}
