/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Restlet that can find the target server resource that will effectively handle
 * incoming calls. By default, based on a given {@link ServerResource} (or the
 * now deprecated {@link Handler}) subclass available via the
 * {@link #getTargetClass()} method, it automatically instantiates for each
 * incoming call the target resource class using its default constructor and
 * invoking the {@link ServerResource#init(Context, Request, Response)} method.<br>
 * <br>
 * Once the target has been created, the call is automatically dispatched to the
 * {@link ServerResource#handle()} method (or for {@link Handler} subclasses to
 * the handle*() method (where the '*' character corresponds to the method name)
 * if the corresponding allow*() method returns true).<br>
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
    @SuppressWarnings("deprecation")
    public static Finder createFinder(Class<?> targetClass,
            Class<? extends Finder> finderClass, Context context, Logger logger) {
        Finder result = null;
        if (Resource.class.isAssignableFrom(targetClass)
                || ServerResource.class.isAssignableFrom(targetClass)) {
            if (finderClass != null) {
                try {
                    final Constructor<? extends Finder> constructor = finderClass
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
        } else {
            if (logger != null) {
                logger
                        .log(
                                Level.WARNING,
                                "Cannot create a Finder for the given target class, since it is neither a subclass of Resource nor a subclass of ServerResource.");
            }
        }
        return result;
    }

    /** Target {@link Handler} or {@link ServerResource} subclass. */
    private volatile Class<?> targetClass;

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
     *            The target handler class. It must be either a subclass of
     *            {@link Handler} or of {@link ServerResource}.
     */
    public Finder(Context context, Class<?> targetClass) {
        super(context);
        this.targetClass = targetClass;
    }

    /**
     * Indicates if a method is allowed on a target handler.
     * 
     * @param method
     *            The method to test.
     * @param target
     *            The target handler.
     * @return True if a method is allowed on a target handler.
     */
    @SuppressWarnings("deprecation")
    private boolean allow(Method method, Handler target) {
        boolean result = false;

        if (target != null) {
            if (method.equals(Method.GET)) {
                result = target.allowGet();
            } else if (method.equals(Method.POST)) {
                result = target.allowPost();
            } else if (method.equals(Method.PUT)) {
                result = target.allowPut();
            } else if (method.equals(Method.DELETE)) {
                result = target.allowDelete();
            } else if (method.equals(Method.HEAD)) {
                result = target.allowHead();
            } else if (method.equals(Method.OPTIONS)) {
                result = target.allowOptions();
            } else {
                // Dynamically introspect the target handler to detect a
                // matching "allow" method.
                final java.lang.reflect.Method allowMethod = getAllowMethod(
                        method, target);
                if (allowMethod != null) {
                    result = (Boolean) invoke(target, allowMethod);
                }
            }
        }

        return result;
    }

    /**
     * Creates a new instance of a given {@link ServerResource} subclass. Note
     * that Error and RuntimeException thrown by {@link ServerResource}
     * constructors are re-thrown by this method. Other exception are caught and
     * logged.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The created handler or null.
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
                        .log(
                                Level.WARNING,
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
     * @return The created handler or null.
     */
    @SuppressWarnings("unchecked")
    public ServerResource create(Request request, Response response) {
        ServerResource result = null;

        if ((getTargetClass() != null)
                && ServerResource.class.isAssignableFrom(getTargetClass())) {
            result = create((Class<? extends ServerResource>) getTargetClass(),
                    request, response);
        }

        return result;
    }

    /**
     * Creates a new instance of a given handler class. Note that Error and
     * RuntimeException thrown by Handler constructors are re-thrown by this
     * method. Other exception are caught and logged.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The created handler or null.
     * @deprecated Use {@link #create(Request, Response)} instead.
     */
    @Deprecated
    protected Handler createTarget(Class<? extends Handler> targetClass,
            Request request, Response response) {
        Handler result = null;

        if (targetClass != null) {
            try {
                Constructor<?> constructor;
                try {
                    // Invoke the constructor with Context, Request and Response
                    // parameters
                    constructor = targetClass.getConstructor(Context.class,
                            Request.class, Response.class);
                    result = (Handler) constructor.newInstance(getContext(),
                            request, response);
                } catch (NoSuchMethodException nsme) {
                    // Invoke the default constructor then the init(Context,
                    // Request, Response) method.
                    constructor = targetClass.getConstructor();
                    if (constructor != null) {
                        result = (Handler) constructor.newInstance();
                        result.init(getContext(), request, response);
                    }
                }
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                } else if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    getLogger()
                            .log(
                                    Level.WARNING,
                                    "Exception while instantiating the target handler.",
                                    e);
                }
            } catch (Exception e) {
                getLogger().log(Level.WARNING,
                        "Exception while instantiating the target handler.", e);
            }
        }

        return result;
    }

    /**
     * Creates a new instance of the handler class designated by the
     * "targetClass" property. The default behavior is to invoke the
     * {@link #createTarget(Class, Request, Response)} with the "targetClass"
     * property as a parameter.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The created handler or null.
     * @deprecated Use {@link #create(Request, Response)} instead.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    protected Handler createTarget(Request request, Response response) {
        Handler result = null;

        if ((getTargetClass() != null)
                && Handler.class.isAssignableFrom(getTargetClass())) {
            result = createTarget((Class<? extends Handler>) getTargetClass(),
                    request, response);
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
     * @return The target handler if available or null.
     */
    public ServerResource find(Request request, Response response) {
        return create(request, response);
    }

    /**
     * Finds the target {@link Handler} if available. The default behavior is to
     * invoke the {@link #createTarget(Request, Response)} method.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The target handler if available or null.
     * @deprecated Use {@link #find(Request, Response)} instead.
     */
    @Deprecated
    public Handler findTarget(Request request, Response response) {
        return createTarget(request, response);
    }

    /**
     * Returns the allow method matching the given method name.
     * 
     * @param method
     *            The method to match.
     * @param target
     *            The target handler.
     * @return The allow method matching the given method name.
     */
    @SuppressWarnings("deprecation")
    private java.lang.reflect.Method getAllowMethod(Method method,
            Handler target) {
        return getMethod("allow", method, target);
    }

    /**
     * Returns the handle method matching the given method name.
     * 
     * @param method
     *            The method to match.
     * @return The handle method matching the given method name.
     */
    @SuppressWarnings("deprecation")
    private java.lang.reflect.Method getHandleMethod(Handler target,
            Method method) {
        return getMethod("handle", method, target);
    }

    /**
     * Returns the method matching the given prefix and method name.
     * 
     * @param prefix
     *            The method prefix to match (ex: "allow" or "handle").
     * @param method
     *            The method to match.
     * @return The method matching the given prefix and method name.
     */
    private java.lang.reflect.Method getMethod(String prefix, Method method,
            Object target, Class<?>... classes) {
        java.lang.reflect.Method result = null;
        final StringBuilder sb = new StringBuilder();
        final String methodName = method.getName().toLowerCase();

        if ((methodName != null) && (methodName.length() > 0)) {
            sb.append(prefix);
            sb.append(Character.toUpperCase(methodName.charAt(0)));
            sb.append(methodName.substring(1));
        }

        try {
            result = target.getClass().getMethod(sb.toString(), classes);
        } catch (SecurityException e) {
            getLogger().log(
                    Level.WARNING,
                    "Couldn't access the " + prefix + " method for \"" + method
                            + "\"", e);
        } catch (NoSuchMethodException e) {
            getLogger().log(
                    Level.INFO,
                    "Couldn't find the " + prefix + " method for \"" + method
                            + "\"", e);
        }

        return result;
    }

    /**
     * Returns the target handler class which must be either a subclass of
     * {@link Handler} or of {@link ServerResource}.
     * 
     * @return the target Handler class.
     */
    public Class<?> getTargetClass() {
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
    @SuppressWarnings("deprecation")
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (isStarted()) {
            Handler targetHandler = findTarget(request, response);

            if (targetHandler != null) {
                if (!response.getStatus().equals(Status.SUCCESS_OK)) {
                    // Probably during the instantiation of the target
                    // handler, or earlier the status was changed from the
                    // default one. Don't go further.
                } else {
                    Method method = request.getMethod();

                    if (method == null) {
                        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                                "No method specified");
                    } else {
                        if (!allow(method, targetHandler)) {
                            response
                                    .setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                            targetHandler.updateAllowedMethods();
                        } else {

                            if (method.equals(Method.GET)) {
                                targetHandler.handleGet();
                            } else if (method.equals(Method.HEAD)) {
                                targetHandler.handleHead();
                            } else if (method.equals(Method.POST)) {
                                targetHandler.handlePost();
                            } else if (method.equals(Method.PUT)) {
                                targetHandler.handlePut();
                            } else if (method.equals(Method.DELETE)) {
                                targetHandler.handleDelete();
                            } else if (method.equals(Method.OPTIONS)) {
                                targetHandler.handleOptions();
                            } else {
                                final java.lang.reflect.Method handleMethod = getHandleMethod(
                                        targetHandler, method);
                                if (handleMethod != null) {
                                    invoke(targetHandler, handleMethod);
                                } else {
                                    response
                                            .setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                                }
                            }
                        }
                    }
                }
            } else {
                ServerResource targetResource = find(request, response);

                if (targetResource == null) {
                    // If the current status is a success but we couldn't
                    // find the target handler for the request's resource
                    // URI, then we set the response status to 404 (Not
                    // Found).
                    getLogger().warning(
                            "No target resource was defined for this finder: "
                                    + toString());
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
    }

    /**
     * Invokes a method with the given arguments.
     * 
     * @param target
     *            The target object.
     * @param method
     *            The method to invoke.
     * @param args
     *            The arguments to pass.
     * @return Invocation result.
     */
    private Object invoke(Object target, java.lang.reflect.Method method,
            Object... args) {
        Object result = null;

        if (method != null) {
            try {
                result = method.invoke(target, args);
            } catch (Exception e) {
                getLogger().log(
                        Level.WARNING,
                        "Couldn't invoke the handle method for \"" + method
                                + "\"", e);
            }
        }

        return result;
    }

    /**
     * Sets the target handler class which must be either a subclass of
     * {@link Handler} or of {@link ServerResource}.
     * 
     * @param targetClass
     *            The target handler class. It must be either a subclass of
     *            {@link Handler} or of {@link ServerResource}.
     */
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

}
