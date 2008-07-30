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

package org.restlet.service;

/**
 * Generic service associated to a component or an application. The lifecycle of
 * a service is tigthly related to the one of the associated component or
 * application.
 * 
 * If you want to use a specific service, you can always disable it before it is
 * actually started via the {@link #setEnabled(boolean)} method.
 * 
 * @author Jerome Louvel
 */
public abstract class Service {
    /** Indicates if the service has been enabled. */
    private volatile boolean enabled;

    /** Indicates if the restlet was started. */
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
        this.enabled = enabled;
    }

    /**
     * Indicates if the Restlet is started.
     * 
     * @return True if the Restlet is started.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * Indicates if the Restlet is stopped.
     * 
     * @return True if the Restlet is stopped.
     */
    public boolean isStopped() {
        return !this.started;
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

    /**
     * Indicates if the service should be enabled.
     * 
     * @return True if the service should be enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
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

}
