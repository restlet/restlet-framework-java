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
import org.restlet.Client;
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
    /** The WAR client. */
    private volatile Client warClient;

    /** The application delegate. */
    private volatile Application application;

    /** The parent context. */
    private volatile Context parentContext;

    /** The client dispatcher. */
    private volatile Uniform clientDispatcher;

    /** The server dispatcher. */
    private volatile Uniform serverDispatcher;

    /**
     * Constructor.
     * 
     * @param application
     *                The application.
     * @param parentContext
     *                The parent context.
     * @param logger
     *                The logger instance of use.
     */
    public ApplicationContext(Application application, Context parentContext,
            Logger logger) {
        super(getLoggerName(application));
        this.application = application;
        this.parentContext = parentContext;
        this.warClient = null;
        this.clientDispatcher = new ApplicationClientDispatcher(this);
        this.serverDispatcher = (getParentContext() != null) ? getParentContext()
                .getServerDispatcher()
                : null;
        // this.warClient = new Client(Protocol.WAR);

        // Set the application as an attribute for usage by other services
        // like the ConnectorService
        getAttributes().put(Application.KEY, application);
    }

    /**
     * Returns a non-null logger name.
     * 
     * @param application
     *                The application.
     * @return The logger name.
     */
    private static String getLoggerName(Application application) {
        String result = application.getClass().getCanonicalName();
        if (result == null)
            result = "org.restlet.application";
        return result;
    }

    @Override
    public Uniform getClientDispatcher() {
        return this.clientDispatcher;
    }

    @Override
    public Uniform getServerDispatcher() {
        return this.serverDispatcher;
    }

    /**
     * Returns the application.
     * 
     * @return the application.
     */
    @Override
    public Application getApplication() {
        return this.application;
    }

    /**
     * Returns the WAR client.
     * 
     * @return the WAR client.
     */
    protected Client getWarClient() {
        return this.warClient;
    }

    /**
     * Sets the WAR client.
     * 
     * @param warClient
     *                the WAR client.
     */
    public void setWarClient(Client warClient) {
        this.warClient = warClient;
    }

    /**
     * Returns the parent context.
     * 
     * @return The parent context.
     */
    protected Context getParentContext() {
        return this.parentContext;
    }

}
