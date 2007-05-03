/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Base class exposing a uniform REST interface.<br/> <br/> "The central
 * feature that distinguishes the REST architectural style from other
 * network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to
 * the component interface, the overall system architecture is simplified and
 * the visibility of interactions is improved. Implementations are decoupled
 * from the services they provide, which encourages independent evolvability."
 * Roy T. Fielding<br/> <br/> It has many subclasses that focus on a specific
 * ways to handle calls like filtering, routing or finding a target resource.
 * The context property is typically provided by a parent container as a way to
 * give access to features such as logging and client connectors.
 * 
 * @see <a
 *      href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 *      dissertation</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Restlet {
    /** Error message. */
    private static final String UNABLE_TO_START = "Unable to start the Restlet";

    /** The context. */
    private Context context;

    /** Indicates if the restlet was started. */
    private boolean started;

    /**
     * Constructor.
     */
    public Restlet() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public Restlet(Context context) {
        this.context = context;
        this.started = false;
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        if (this.context == null)
            this.context = new Context(getClass().getCanonicalName());
        return this.context;
    }

    /**
     * Returns the context's logger.
     * 
     * @return The context's logger.
     */
    public Logger getLogger() {
        return (getContext() != null) ? getContext().getLogger() : null;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        init(request, response);
    }

    /**
     * Initialize the Restlet by attemting to start it.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void init(Request request, Response response) {
        // Check if the Restlet was started
        if (isStopped()) {
            try {
                start();
            } catch (Exception e) {
                // Occurred while starting the Restlet
                getContext().getLogger().log(Level.WARNING, UNABLE_TO_START, e);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }

            if (isStarted()) {
                // Everything went fine, no exception raised
                response.setStatus(Status.SUCCESS_OK);
            } else {
                // No exception raised but the Restlet somehow couldn't be
                // started
                getContext().getLogger().log(Level.WARNING, UNABLE_TO_START);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        }
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

    /**
     * Sets the context.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /** Starts the Restlet. */
    public void start() throws Exception {
        this.started = true;
    }

    /** Stops the Restlet. */
    public void stop() throws Exception {
        this.started = false;
    }

}
