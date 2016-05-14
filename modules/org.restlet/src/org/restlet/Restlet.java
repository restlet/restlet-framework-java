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

package org.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 * Uniform class that provides a context and life cycle support. It has many
 * subclasses that focus on specific ways to process calls. The context property
 * is typically provided by a parent Component as a way to encapsulate access to
 * shared features such as logging and client connectors.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public abstract class Restlet implements Uniform {
    /** Error message. */
    private static final String UNABLE_TO_START = "Unable to start the Restlet";

    // [ifndef gwt] method
    /**
     * Indicates that a Restlet's context has changed.
     * 
     * @param restlet
     *            The Restlet with a changed context.
     * @param context
     *            The new context.
     */
    private static void fireContextChanged(Restlet restlet, Context context) {
        if (context != null) {
            if (context instanceof org.restlet.engine.util.ChildContext) {
                org.restlet.engine.util.ChildContext childContext = (org.restlet.engine.util.ChildContext) context;

                if (childContext.getChild() == null) {
                    childContext.setChild(restlet);
                }
            } else if (!(restlet instanceof Component)
                    && (context instanceof org.restlet.engine.component.ComponentContext)) {
                context.getLogger()
                        .severe("For security reasons, don't pass the component context to child Restlets anymore. Use the Context#createChildContext() method instead. "
                                + restlet.getClass());
            }
        }
    }

    /** The author(s). */
    private volatile String author;

    /** The context. */
    private volatile Context context;

    /** The description. */
    private volatile String description;

    // [ifndef gwt] member
    /** Finder class to instantiate. */
    private volatile Class<? extends org.restlet.resource.Finder> finderClass;

    /** The display name. */
    private volatile String name;

    /** The owner(s). */
    private volatile String owner;

    /** Indicates if the Restlet was started. */
    private volatile boolean started;

    /**
     * Constructor with null context.
     */
    public Restlet() {
        this(null);
    }

    /**
     * Constructor with the Restlet's context which can be the parent's
     * application context, but shouldn't be the parent Component's context for
     * security reasons.
     * 
     * @see Context#createChildContext()
     * 
     * @param context
     *            The context of the Restlet.
     * 
     */
    public Restlet(Context context) {
        // [ifndef gwt] instruction
        this.context = context;
        // [ifdef gwt] instruction uncomment
        // this.context = (context != null) ? context : new Context();
        this.started = false;
        this.name = toString();
        this.description = null;
        this.author = null;
        this.owner = null;

        // [ifndef gwt]
        this.finderClass = null;
        if (Engine.getInstance() == null) {
            Context.getCurrentLogger()
                    .severe("Unable to fully initialize the Restlet. No Restlet engine available.");
            throw new RuntimeException(
                    "Unable to fully initialize the Restlet. No Restlet engine available.");
        }

        fireContextChanged(this, context);
        // [enddef]
    }

    // [ifndef gwt] method
    /**
     * Creates a new finder instance based on the "targetClass" property. If
     * none is define, the {@link Application#createFinder(Class)} method is
     * invoked if available, otherwise the
     * {@link org.restlet.resource.Finder#createFinder(Class, Class, Context, Logger)}
     * method is called with the {@link org.restlet.resource.Finder} class as
     * parameter.
     * 
     * @param resourceClass
     *            The target {@link org.restlet.resource.ServerResource} class
     *            to find.
     * @return The new finder instance.
     * @see org.restlet.resource.Finder#createFinder(Class, Class, Context,
     *      Logger)
     */
    public org.restlet.resource.Finder createFinder(
            Class<? extends org.restlet.resource.ServerResource> resourceClass) {
        org.restlet.resource.Finder result = null;

        if (getFinderClass() != null) {
            result = org.restlet.resource.Finder.createFinder(resourceClass,
                    getFinderClass(), getContext(), getLogger());
        } else if ((getApplication() != null) && (getApplication() != this)) {
            result = getApplication().createFinder(resourceClass);
        } else {
            result = org.restlet.resource.Finder.createFinder(resourceClass,
                    org.restlet.resource.Finder.class, getContext(),
                    getLogger());
        }

        return result;
    }

    /**
     * Attempts to {@link #stop()} the Restlet if it is still started.
     */
    @Override
    protected void finalize() throws Throwable {
        if (isStarted()) {
            stop();
        }
        super.finalize();
    }

    // [ifndef gwt] method
    /**
     * Returns the parent application if it exists, or null.
     * 
     * @return The parent application if it exists, or null.
     */
    public Application getApplication() {
        return Application.getCurrent();
    }

    /**
     * Returns the author(s).
     * 
     * @return The author(s).
     */
    public String getAuthor() {
        return this.author;
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
     * Returns the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return this.description;
    }

    // [ifndef gwt] method
    /**
     * Returns the finder class used to instantiate resource classes. By
     * default, it returns the {@link org.restlet.resource.Finder} class. This
     * property is leveraged by {@link Application#setOutboundRoot(Class)} and
     * {@link Application#setInboundRoot(Class)} methods.
     * 
     * @return the finder class to instantiate.
     */
    public Class<? extends org.restlet.resource.Finder> getFinderClass() {
        return finderClass;
    }

    /**
     * Returns the context's logger.
     * 
     * @return The context's logger.
     */
    public Logger getLogger() {
        Logger result = null;
        Context context = getContext();

        if (context == null) {
            context = Context.getCurrent();
        }

        if (context != null) {
            result = context.getLogger();
        }

        if (result == null) {
            result = Engine.getLogger(this, "org.restlet");
        }

        return result;
    }

    /**
     * Returns the display name.
     * 
     * @return The display name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the owner(s).
     * 
     * @return The owner(s).
     */
    public String getOwner() {
        return this.owner;
    }

    // [ifndef gwt] method
    /**
     * Handles a call. Creates an empty {@link Response} object and then invokes
     * {@link #handle(Request, Response)}.
     * 
     * @param request
     *            The request to handle.
     * @return The returned response.
     */
    public final Response handle(Request request) {
        Response response = new Response(request);
        handle(request, response);
        return response;
    }

    /**
     * Handles a call. The default behavior is to initialize the Restlet by
     * setting the current context using the {@link Context#setCurrent(Context)}
     * method and by attempting to start it, unless it was already started. If
     * an exception is thrown during the start action, then the response status
     * is set to {@link Status#SERVER_ERROR_INTERNAL}.
     * <p>
     * Subclasses overriding this method should make sure that they call
     * super.handle(request, response) before adding their own logic.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        // [ifndef gwt]
        // Associate the response to the current thread
        Response.setCurrent(response);

        // Associate the context to the current thread
        if (getContext() != null) {
            Context.setCurrent(getContext());
        }
        // [enddef]

        // Check if the Restlet was started
        if (isStopped()) {
            try {
                start();
            } catch (Exception e) {
                // Occurred while starting the Restlet
                if (getContext() != null) {
                    getContext().getLogger().log(Level.WARNING,
                            UNABLE_TO_START, e);
                } else {
                    Context.getCurrentLogger().log(Level.WARNING,
                            UNABLE_TO_START, e);
                }

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
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param onResponseCallback
     *            The callback invoked upon response reception.
     */
    public final void handle(Request request, Response response,
            Uniform onResponseCallback) {
        request.setOnResponse(onResponseCallback);
        handle(request, response);
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param onReceivedCallback
     *            The callback invoked upon request reception.
     */
    public final void handle(Request request, Uniform onReceivedCallback) {
        Response response = new Response(request);
        handle(request, response, onReceivedCallback);
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
     * Sets the author(s).
     * 
     * @param author
     *            The author(s).
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Sets the context.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        this.context = context;
        // [ifndef gwt] instruction
        fireContextChanged(this, context);
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    // [ifndef gwt] method
    /**
     * Sets the finder class to instantiate. This property is leveraged by
     * {@link Application#setOutboundRoot(Class)} and
     * {@link Application#setInboundRoot(Class)} methods.
     * 
     * @param finderClass
     *            The finder class to instantiate.
     */
    public void setFinderClass(
            Class<? extends org.restlet.resource.Finder> finderClass) {
        this.finderClass = finderClass;
    }

    /**
     * Sets the display name.
     * 
     * @param name
     *            The display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the owner(s).
     * 
     * @param owner
     *            The owner(s).
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Starts the Restlet. By default its only sets "started" internal property
     * to true.
     * 
     * WARNING: this method must be called at the end of the starting process by
     * subclasses otherwise concurrent threads could enter into the call
     * handling logic too early.
     */
    public synchronized void start() throws Exception {
        this.started = true;
    }

    /**
     * Stops the Restlet. By default its only sets "started" internal property
     * to false.
     * 
     * WARNING: this method must be called at the beginning of the stopping
     * process by subclasses otherwise concurrent threads could continue to
     * (improperly) handle calls.
     */
    public synchronized void stop() throws Exception {
        this.started = false;
    }

}
