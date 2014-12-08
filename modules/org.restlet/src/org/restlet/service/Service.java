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

package org.restlet.service;

import org.restlet.Context;

/**
 * Generic service associated to a component or an application. The life cycle
 * of a service is tightly related to the one of the associated component or
 * application.<br>
 * <br>
 * If you want to use a specific service, you can always disable it before it is
 * actually started via the {@link #setEnabled(boolean)} method.
 * 
 * @author Jerome Louvel
 */
public abstract class Service {

    /** The context. */
    private volatile Context context;

    /** Indicates if the service has been enabled. */
    private volatile boolean enabled;

    /** Indicates if the service was started. */
    private volatile boolean started;

    /**
     * Constructor. Enables the service by default.
     */
    public Service() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public Service(boolean enabled) {
        this.context = null;
        this.enabled = enabled;
    }

    // [ifndef gwt] method
    /**
     * Create the filter that should be invoked for incoming calls.
     * 
     * @param context
     *            The current context.
     * @return The new filter or null.
     */
    public org.restlet.routing.Filter createInboundFilter(
            org.restlet.Context context) {
        return null;
    }

    // [ifndef gwt] method
    /**
     * Create the filter that should be invoked for outgoing calls.
     * 
     * @param context
     *            The current context.
     * @return The new filter or null.
     * @see Context#getClientDispatcher()
     */
    public org.restlet.routing.Filter createOutboundFilter(
            org.restlet.Context context) {
        return null;
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Indicates if the service should be enabled.
     * 
     * @return True if the service should be enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Indicates if the service is started.
     * 
     * @return True if the service is started.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * Indicates if the service is stopped.
     * 
     * @return True if the service is stopped.
     */
    public boolean isStopped() {
        return !this.started;
    }

    /**
     * Sets the context.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Indicates if the service should be enabled.
     * 
     * @param enabled
     *            True if the service should be enabled.
     */
    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Starts the Restlet. */
    public synchronized void start() throws Exception {
        if (isEnabled()) {
            this.started = true;
        }
    }

    /** Stops the Restlet. */
    public synchronized void stop() throws Exception {
        if (isEnabled()) {
            this.started = false;
        }
    }

}
