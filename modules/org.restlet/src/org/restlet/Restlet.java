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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Engine;

/**
 * Uniform class that provides a context and life cycle support. It has many
 * subclasses that focus on specific ways to process calls. The context property
 * is typically provided by a parent Component as a way to encapsulate access to
 * shared features such as logging and client connectors.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Restlet extends Uniform {
    /** Error message. */
    private static final String UNABLE_TO_START = "Unable to start the Restlet";

    /** The context. */
    private volatile Context context;

    /** Indicates if the restlet was started. */
    private volatile boolean started;

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
        this.context = context;
        this.started = false;
        Engine.getInstance().fireContextChanged(this, context);
    }

    /**
     * Returns the parent application if it exists, or null.
     * 
     * @return The parent application if it exists, or null.
     */
    public Application getApplication() {
        return Application.getCurrent();
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
     * Returns the context's logger.
     * 
     * @return The context's logger.
     */
    public Logger getLogger() {
        return (getContext() != null) ? getContext().getLogger() : Logger
                .getLogger(getClass().getCanonicalName());
    }

    /**
     * Handles a call. The default behavior is to call the (now deprecated)
     * {@link #init(Request, Response)} method. In next version,
     * {@link #init(Request, Response)} will be removed and its logic directly
     * added to this method instead.
     * <p>
     * Subclasses overriding this method should make sure that they call
     * super.handle(request, response) before adding their own logic.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        init(request, response);
    }

    /**
     * Initialize the Restlet by setting the current context using the
     * {@link Context#setCurrent(Context)} method and by attempting to start it,
     * unless it was already started. If an exception is thrown during the start
     * action, then the response status is set to
     * {@link Status#SERVER_ERROR_INTERNAL}.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @deprecated Instead, make sure that you call the
     *             {@link #handle(Request, Response)} method from your Restlet
     *             superclass.
     */
    @Deprecated
    protected synchronized void init(Request request, Response response) {
        // Associate the response to the current thread
        Response.setCurrent(response);

        // Associate the context to the current thread
        Context.setCurrent(getContext());

        // Check if the Restlet was started
        if (isStopped()) {
            try {
                start();
            } catch (final Exception e) {
                // Occurred while starting the Restlet
                getContext().getLogger().log(Level.WARNING, UNABLE_TO_START, e);
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }

            if (!isStarted()) {
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
        Engine.getInstance().fireContextChanged(this, context);
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
