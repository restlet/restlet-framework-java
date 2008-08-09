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

package org.restlet.gwt;

import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.data.Status;

/**
 * Uniform class that provides a context and life cycle support. It has many
 * subclasses that focus on specific ways to process calls. The context property
 * is typically provided by a parent Component as a way to encapsulate access to
 * shared features such as logging and client connectors.
 * 
 * @author Jerome Louvel
 */
public class Restlet extends Uniform {
    /** Error message. */
    private static final String UNABLE_TO_START = "Unable to start the Restlet";

    /** The context. */
    private Context context;

    /** Indicates if the restlet was started. */
    private boolean started;

    /**
     * Constructor. Note that usage of this constructor is not recommended as
     * the Restlet won't have a proper context set. In general you will prefer
     * to use the other constructor and pass it the parent application's context
     * or eventually the parent component's context if you don't use
     * applications.
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
        if (context == null) {
            this.context = new Context();
        } else {
            this.context = context;
        }

        this.started = false;
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
     * Handles a call. Subclasses overriding this method should make sure that
     * they call super.handle(request, response) before adding their own logic.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param callback
     *            The callback invoked upon request completion.
     */
    @Override
    public void handle(Request request, Response response, Callback callback) {
        // Check if the Restlet was started
        if (isStopped()) {
            try {
                start();
            } catch (final Exception e) {
                // Occurred while starting the Restlet
                System.err.println(UNABLE_TO_START);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }

            if (!isStarted()) {
                // No exception raised but the Restlet somehow couldn't be
                // started
                System.err.println(UNABLE_TO_START);
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
    public synchronized void start() throws Exception {
        this.started = true;
    }

    /** Stops the Restlet. */
    public synchronized void stop() throws Exception {
        this.started = false;
    }

}
