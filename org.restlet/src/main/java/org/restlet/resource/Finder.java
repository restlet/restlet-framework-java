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

package org.restlet.resource;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;

/**
 * Restlet that can find the target server resource that will effectively handle
 * incoming calls. By default, based on a given {@link ServerResource} subclass
 * available via the {@link #getTargetClass()} method, it automatically
 * instantiates for each incoming call the target resource class using its
 * default constructor and invoking the
 * {@link ServerResource#init(Context, Request, Response)} method.<br>
 * <br>
 * Once the target has been created, the call is automatically dispatched to the
 * {@link ServerResource#handle()} method.<br>
 * <br>
 * Once the call is handled, the {@link ServerResource#release()} method is
 * invoked to permit clean-up actions.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class Finder extends Restlet {
    /**
     * Creates a new finder instance based on the "targetClass" property.
     * 
     * @param targetClass
     *            The target Resource class to attach.
     * @param finderClass
     *            The optional finder class to instantiate.
     * @param logger
     *            The logger.
     * @return The new finder instance.
     */
    public static Finder createFinder(
            Class<? extends ServerResource> targetClass,
            Class<? extends Finder> finderClass, Context context, Logger logger) {
        Finder result = null;

        if (finderClass != null) {
            try {
                Constructor<? extends Finder> constructor = finderClass
                        .getConstructor(Context.class, Class.class);

                if (constructor != null) {
                    result = constructor.newInstance(context, targetClass);
                }
            } catch (Exception e) {
                if (logger != null) {
                    logger.log(Level.WARNING,
                            "Exception while instantiating the finder.", e);
                }
            }
        } else {
            result = new Finder(context, targetClass);
        }

        return result;
    }

    /** Target {@link ServerResource} subclass. */
    private volatile Class<? extends ServerResource> targetClass;

    /**
     * Constructor.
     */
    public Finder() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public Finder(Context context) {
        super(context);
        this.targetClass = null;
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param targetClass
     *            The target {@link ServerResource} subclass.
     */
    public Finder(Context context, Class<? extends ServerResource> targetClass) {
        super(context);
        this.targetClass = targetClass;
    }

    /**
     * Creates a new instance of a given {@link ServerResource} subclass. Note
     * that {@link Error} and {@link RuntimeException} thrown by
     * {@link ServerResource} constructors are re-thrown by this method. Other
     * exception are caught and logged.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The created resource or null.
     */
    public ServerResource create(Class<? extends ServerResource> targetClass,
            Request request, Response response) {
        ServerResource result = null;

        if (targetClass != null) {
            try {
                // Invoke the default constructor
                result = targetClass.newInstance();
            } catch (Exception e) {
                getLogger()
                        .log(Level.WARNING,
                                "Exception while instantiating the target server resource.",
                                e);
            }
        }

        return result;
    }

    /**
     * Creates a new instance of the {@link ServerResource} subclass designated
     * by the "targetClass" property. The default behavior is to invoke the
     * {@link #create(Class, Request, Response)} with the "targetClass" property
     * as a parameter.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The created resource or null.
     */
    public ServerResource create(Request request, Response response) {
        ServerResource result = null;

        if (getTargetClass() != null) {
            result = create(getTargetClass(), request, response);
        }

        return result;
    }

    /**
     * Finds the target {@link ServerResource} if available. The default
     * behavior is to invoke the {@link #create(Request, Response)} method.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The target resource if available or null.
     */
    public ServerResource find(Request request, Response response) {
        return create(request, response);
    }

    /**
     * Returns the target resource class which must be either a subclass of
     * {@link ServerResource}.
     * 
     * @return the target Handler class.
     */
    public Class<? extends ServerResource> getTargetClass() {
        return this.targetClass;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (isStarted()) {
            ServerResource targetResource = find(request, response);

            if (targetResource == null) {
                // If the current status is a success but we couldn't
                // find the target resource for the request's URI,
                // then we set the response status to 404 (Not Found).
                if (getLogger().isLoggable(Level.WARNING)) {
                    getLogger().warning(
                            "No target resource was defined for this finder: "
                                    + toString());
                }

                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                targetResource.init(getContext(), request, response);

                if ((response == null) || response.getStatus().isSuccess()) {
                    targetResource.handle();
                } else {
                    // Probably during the instantiation of the target
                    // server resource, or earlier the status was
                    // changed from the default one. Don't go further.
                }

                targetResource.release();
            }
        }
    }

    /**
     * Sets the target resource class which must be a subclass of
     * {@link ServerResource}.
     * 
     * @param targetClass
     *            The target resource class. It must be a subclass of
     *            {@link ServerResource}.
     */
    public void setTargetClass(Class<? extends ServerResource> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public String toString() {
        return getTargetClass() == null ? "Finder with no target class"
                : "Finder for " + getTargetClass().getSimpleName();
    }

}
