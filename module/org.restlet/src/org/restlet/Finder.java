/*
 * Copyright 2005-2007 Noelios Consulting.
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
import java.util.Set;
import java.util.logging.Level;

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

/**
 * Restlet that can find the target resource that will concretely handle a call.
 * Based on a given resource class, it is also capable of instantiating the
 * resource with the call's context, request and response without requiring the
 * usage of a subclass. It will use either the constructor with three arguments:
 * context, request, response; or it will invoke the default constructor then
 * invoke the init() method with the same arguments.<br>
 * <br>
 * Once the target resource has been found, the call is automatically dispatched
 * to the appropriate handle*() method (where the '*' character corresponds to
 * the method name) if the corresponding allow*() method returns true.<br>
 * <br>
 * For example, if you want to support a MOVE method for a WebDAV server, you
 * just have to add a handleMove() method in your subclass of Resource and it
 * will be automatically be used by the Finder instance at runtime.<br>
 * <br>
 * If no matching handle*() method is found, then a
 * Status.CLIENT_ERROR_METHOD_NOT_ALLOWED is returned.
 * 
 * @see <a href="http://www.restlet.org/tutorial#part12">Tutorial: Reaching
 *      target Resources</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Finder extends Restlet {
    /** Target resource class. */
    private Class<? extends Resource> targetClass;

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
        this(context, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param targetClass
     *            The target resource class.
     */
    public Finder(Context context, Class<? extends Resource> targetClass) {
        super(context);
        this.targetClass = targetClass;
    }

    /**
     * Indicates if a method is allowed on a target resource.
     * 
     * @param method
     *            The method to test.
     * @param target
     *            The target resource.
     * @return True if a method is allowed on a target resource.
     */
    private boolean allowMethod(Method method, Resource target) {
        boolean result = false;

        if (target != null) {
            if (method.equals(Method.GET) || method.equals(Method.HEAD)) {
                result = target.allowGet();
            } else if (method.equals(Method.POST)) {
                result = target.allowPost();
            } else if (method.equals(Method.PUT)) {
                result = target.allowPut();
            } else if (method.equals(Method.DELETE)) {
                result = target.allowDelete();
            } else if (method.equals(Method.OPTIONS)) {
                result = true;
            } else {
                // Dynamically introspect the target resource to detect a
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
     * Finds the target Resource if available. The default behavior is to invoke
     * the {@link #createResource(Request, Response)} method.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The target resource if available or null.
     */
    public Resource findTarget(Request request, Response response) {
        return createResource(request, response);
    }

    /**
     * Creates a new instance of the resource class designated by the
     * "targetClass" property.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The created resource or null.
     */
    public Resource createResource(Request request, Response response) {
        Resource result = null;

        if (getTargetClass() != null) {
            Constructor constructor;
            try {
                constructor = getTargetClass().getConstructor(Context.class,
                        Request.class, Response.class);

                if (constructor != null) {
                    result = (Resource) constructor.newInstance(getContext(),
                            request, response);
                } else {
                    constructor = getTargetClass().getConstructor();

                    if (constructor != null) {
                        result = (Resource) constructor.newInstance();
                        result.init(getContext(), request, response);
                    }
                }
            } catch (Exception e) {
                getLogger()
                        .log(
                                Level.WARNING,
                                "Exception while instantiating the target resource.",
                                e);
            }
        }

        return result;
    }

    /**
     * Returns the allow method matching the given method name.
     * 
     * @param method
     *            The method to match.
     * @param target
     *            The target resource.
     * @return The allow method matching the given method name.
     */
    private java.lang.reflect.Method getAllowMethod(Method method,
            Resource target) {
        return getMethod("allow", method, target);
    }

    /**
     * Returns the handle method matching the given method name.
     * 
     * @param method
     *            The method to match.
     * @return The handle method matching the given method name.
     */
    private java.lang.reflect.Method getHandleMethod(Resource target,
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
            Object target, Class... classes) {
        java.lang.reflect.Method result = null;
        StringBuilder sb = new StringBuilder();
        String methodName = method.getName().toLowerCase();

        if ((methodName != null) && (methodName.length() > 0)) {
            sb.append("handle");
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
                    Level.WARNING,
                    "Couldn't find the " + prefix + " method for \"" + method
                            + "\"", e);
        }

        return result;
    }

    /**
     * Returns the target Resource class.
     * 
     * @return the target Resource class.
     */
    public Class<? extends Resource> getTargetClass() {
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
    public void handle(Request request, Response response) {
        init(request, response);

        if (isStarted()) {
            Resource target = findTarget(request, response);

            if (!response.getStatus().equals(Status.SUCCESS_OK)) {
                // Probably during the instantiation of the target resource, or
                // earlier the status was changed from the default one. Don't go
                // further.
            } else if (target == null) {
                // If the currrent status is a success but we couldn't find the
                // target resource for the request's resource URI, then we set
                // the response status to 404 (Not Found).
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                Method method = request.getMethod();

                if (method == null) {
                    response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                            "No method specified");
                } else {
                    if (!allowMethod(method, target)) {
                        response
                                .setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                        updateAllowedMethods(response, target);
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
                                invoke(this, handleMethod, request, response);
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
     * Updates the set of allowed methods on the response based on a target
     * resource.
     * 
     * @param response
     *            The response to update.
     * @param target
     *            The target resource.
     */
    private void updateAllowedMethods(Response response, Resource target) {
        Set<Method> allowedMethods = response.getAllowedMethods();
        for (java.lang.reflect.Method classMethod : target.getClass()
                .getMethods()) {
            if (classMethod.getName().startsWith("allow")
                    && (classMethod.getParameterTypes().length == 0)) {
                if ((Boolean) invoke(target, classMethod)) {
                    Method allowedMethod = Method.valueOf(classMethod.getName()
                            .substring(5));
                    allowedMethods.add(allowedMethod);
                }
            }
        }
    }

}
