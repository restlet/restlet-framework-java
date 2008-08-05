/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
