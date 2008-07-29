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

package com.noelios.restlet.application;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Uniform;

/**
 * Context based on a parent component's context but dedicated to an
 * application. This is important to allow contextual access to application's
 * resources.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ApplicationContext extends Context {

    /**
     * Returns the standard logger name for the given application.
     * 
     * @param application
     *            The application to log about.
     * @return The standard logger name.
     */
    public static String getLoggerName(Application application) {
        String result = null;
        Context context = application.getContext();

        if (context != null) {
            result = application.getClass().getCanonicalName();

            if (result == null) {
                result = "org.restlet.application";
            }

            result += "#" + application.hashCode();
        }

        return result;
    }

    /** The application delegate. */
    private volatile Application application;

    /** The client dispatcher. */
    private volatile ApplicationClientDispatcher clientDispatcher;

    /** The parent context. */
    private volatile Context parentContext;

    /** The server dispatcher. */
    private volatile Uniform serverDispatcher;

    /**
     * Constructor.
     * 
     * @param application
     *            The application.
     * @param parentContext
     *            The parent context.
     * @param logger
     *            The logger instance of use.
     */
    public ApplicationContext(Application application, Context parentContext,
            Logger logger) {
        super(getLoggerName(application));
        this.application = application;
        this.parentContext = parentContext;
        this.clientDispatcher = new ApplicationClientDispatcher(this);
        this.serverDispatcher = (getParentContext() != null) ? getParentContext()
                .getServerDispatcher()
                : null;
    }

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The parent context.
     */
    public ApplicationContext(Context parentContext) {
        super("org.restlet.application");
        this.parentContext = parentContext;
        this.clientDispatcher = new ApplicationClientDispatcher(this);
        this.serverDispatcher = (getParentContext() != null) ? getParentContext()
                .getServerDispatcher()
                : null;
    }

    /**
     * Returns the application.
     * 
     * @return the application.
     */
    public Application getApplication() {
        return this.application;
    }

    @Override
    public ApplicationClientDispatcher getClientDispatcher() {
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
     * Sets the application.
     * 
     * @param application
     *            The application.
     */
    public void setApplication(Application application) {
        this.application = application;
    }

}
