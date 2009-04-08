/**
 * Copyright 2005-2009 Noelios Technologies.
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

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Restlet that can find the target server resource or handler that will
 * effectively handle the call. Based on a given {@link ServerResource} or
 * {@link Handler} subclass, it is also capable of instantiating the target with
 * the context, request and response without requiring the usage of a Finder
 * subclass. It will use the default constructor then invoke the
 * {@link ServerResource#init(Context, Request, Response)} method.<br>
 * <br>
 * Once the target has been found, the call is automatically dispatched to the
 * appropriate {@link ServerResource#handle()} method or for {@link Handler}
 * subclasses to the handle*() method (where the '*' character corresponds to
 * the method name) if the corresponding allow*() method returns true.<br>
 * <br>
 * For example, if you want to support a MOVE method for a WebDAV server, you
 * just have to add a handleMove() method in your subclass of Handler and it
 * will be automatically be used by the Finder instance at runtime.<br>
 * <br>
 * If no matching handle*() method is found, then a
 * Status.CLIENT_ERROR_METHOD_NOT_ALLOWED is returned.<br>
 * <br>
 * Once the call is handled, the {@link ServerResource#destroy()} method is
 * invoked to permit clean-up actions.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://www.restlet.org/documentation/1.1/tutorial#part12">Tutorial:
 *      Reaching target Resources</a>
 * @author Jerome Louvel
 */
public class Finder extends Restlet {
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
                // Invoke the constructor with Context, Request and Response
                // parameters
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
        return create((Class<? extends ServerResource>) getTargetClass(),
                request, response);
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
     */
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
     */
    @SuppressWarnings("unchecked")
    protected Handler createTarget(Request request, Response response) {
        return createTarget((Class<? extends Handler>) getTargetClass(),
                request, response);
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
     */
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
     * Returns the target Handler class. It will be either a subclass of
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
    @SuppressWarnings("unchecked")
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (isStarted()) {
            if (getTargetClass() == null) {
                getLogger().warning(
                        "No target class was defined for this finder: "
                                + toString());
            } else {
                if ((getTargetClass() == null)
                        | Handler.class
                                .isAssignableFrom((Class<? extends Handler>) getTargetClass())) {
                    final Handler targetHandler = findTarget(request, response);

                    if (!response.getStatus().equals(Status.SUCCESS_OK)) {
                        // Probably during the instantiation of the target
                        // handler,
                        // or earlier the status was changed from the default
                        // one.
                        // Don't go further.
                    } else {
                        final Method method = request.getMethod();

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
                    final ServerResource targetResource = find(request,
                            response);
                    targetResource.init(getContext(), request, response);

                    if (!response.getStatus().equals(Status.SUCCESS_OK)) {
                        // Probably during the instantiation of the target
                        // server
                        // resource, or earlier the status was changed from the
                        // default one. Don't go further.
                    } else if (targetResource == null) {
                        // If the current status is a success but we couldn't
                        // find
                        // the target handler for the request's resource URI,
                        // then
                        // we set the response status to 404 (Not Found).
                        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                    } else {
                        targetResource.handle();
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
     * Sets the target Handler class.
     * 
     * @param targetClass
     *            The target Handler class. It must be either a subclass of
     *            {@link Handler} or of {@link ServerResource}.
     */
    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

}
