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

package org.restlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Restlet that can find the target handler that will effectively handle the
 * call. Based on a given Handler subclass, it is also capable of instantiating
 * the handler with the call's context, request and response without requiring
 * the usage of a Finder subclass. It will use either the constructor with three
 * arguments: context, request, response; or it will invoke the default
 * constructor then invoke the init() method with the same arguments.<br>
 * <br>
 * Once the target handler has been found, the call is automatically dispatched
 * to the appropriate handle*() method (where the '*' character corresponds to
 * the method name) if the corresponding allow*() method returns true.<br>
 * <br>
 * For example, if you want to support a MOVE method for a WebDAV server, you
 * just have to add a handleMove() method in your subclass of Handler and it
 * will be automatically be used by the Finder instance at runtime.<br>
 * <br>
 * If no matching handle*() method is found, then a
 * Status.CLIENT_ERROR_METHOD_NOT_ALLOWED is returned.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://www.restlet.org/documentation/1.1/tutorial#part12">Tutorial:
 *      Reaching target Resources</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Finder extends Restlet {
    /** Target handler class. */
    private volatile Class<? extends Handler> targetClass;

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
     *                The context.
     */
    public Finder(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param targetClass
     *                The target handler class.
     */
    public Finder(Context context, Class<? extends Handler> targetClass) {
        super(context);
        this.targetClass = targetClass;
    }

    /**
     * Indicates if a method is allowed on a target handler.
     * 
     * @param method
     *                The method to test.
     * @param target
     *                The target handler.
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
                java.lang.reflect.Method allowMethod = getAllowMethod(method,
                        target);
                if (allowMethod != null) {
                    result = (Boolean) invoke(target, allowMethod);
                }
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
     *                The request to handle.
     * @param response
     *                The response to update.
     * @return The created handler or null.
     * @deprecated Use the {@link #createTarget(Request, Response)} instead.
     */
    @Deprecated
    public Handler createResource(Request request, Response response) {
        return createTarget(getTargetClass(), request, response);
    }

    /**
     * Creates a new instance of a given handler class. Note that Error and
     * RuntimeException thrown by Handler constructors are rethrown by this
     * method. Other exception are caught and logged.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @return The created handler or null.
     */
    public Handler createTarget(Class<? extends Handler> targetClass,
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
     *                The request to handle.
     * @param response
     *                The response to update.
     * @return The created handler or null.
     */
    protected Handler createTarget(Request request, Response response) {
        return createTarget(getTargetClass(), request, response);
    }

    /**
     * Finds the target Handler if available. The default behavior is to invoke
     * the {@link #createTarget(Request, Response)} method.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @return The target handler if available or null.
     */
    protected Handler findTarget(Request request, Response response) {
        return createTarget(request, response);
    }

    /**
     * Returns the allow method matching the given method name.
     * 
     * @param method
     *                The method to match.
     * @param target
     *                The target handler.
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
     *                The method to match.
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
     *                The method prefix to match (ex: "allow" or "handle").
     * @param method
     *                The method to match.
     * @return The method matching the given prefix and method name.
     */
    private java.lang.reflect.Method getMethod(String prefix, Method method,
            Object target, Class<?>... classes) {
        java.lang.reflect.Method result = null;
        StringBuilder sb = new StringBuilder();
        String methodName = method.getName().toLowerCase();

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
     * Returns the target Handler class.
     * 
     * @return the target Handler class.
     */
    public Class<? extends Handler> getTargetClass() {
        return this.targetClass;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (isStarted()) {
            Handler target = findTarget(request, response);

            if (!response.getStatus().equals(Status.SUCCESS_OK)) {
                // Probably during the instantiation of the target handler, or
                // earlier the status was changed from the default one. Don't go
                // further.
            } else if (target == null) {
                // If the currrent status is a success but we couldn't find the
                // target handler for the request's resource URI, then we set
                // the response status to 404 (Not Found).
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                Method method = request.getMethod();

                if (method == null) {
                    response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                            "No method specified");
                } else {
                    if (!allow(method, target)) {
                        response
                                .setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                        target.updateAllowedMethods();
                    } else {
                        if (method.equals(Method.GET)) {
                            target.handleGet();
                        } else if (method.equals(Method.HEAD)) {
                            target.handleHead();
                        } else if (method.equals(Method.POST)) {
                            target.handlePost();
                        } else if (method.equals(Method.PUT)) {
                            target.handlePut();
                        } else if (method.equals(Method.DELETE)) {
                            target.handleDelete();
                        } else if (method.equals(Method.OPTIONS)) {
                            target.handleOptions();
                        } else {
                            java.lang.reflect.Method handleMethod = getHandleMethod(
                                    target, method);
                            if (handleMethod != null) {
                                invoke(target, handleMethod);
                            } else {
                                response
                                        .setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Invokes a method with the given arguments.
     * 
     * @param target
     *                The target object.
     * @param method
     *                The method to invoke.
     * @param args
     *                The arguments to pass.
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
     *                The target Handler class.
     */
    public void setTargetClass(Class<? extends Handler> targetClass) {
        this.targetClass = targetClass;
    }

}
