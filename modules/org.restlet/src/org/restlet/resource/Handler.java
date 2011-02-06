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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Template;

/**
 * Final handler of calls typically created by Finders. Handler instances allow
 * the processing of a call in a thread-safe context. This is different from the
 * Uniform subclasses like Restlet, Filter and Router which can be invoked by
 * multiple threads at the same time. However, as they offer a rather low-level
 * API and its subclass {@link org.restlet.resource.Resource} is often preferred
 * for concrete handlers.<br>
 * <br>
 * This class exposes a different set of handle*() and allow*() Java methods for
 * each type of Uniform method supported by your handler. It has a predefined
 * set for common methods like GET, POST, PUT, DELETE, HEAD and OPTIONS.
 * Extension methods like MOVE or PATCH are automatically supported using Java
 * introspection. The actual dispatching of the call to those methods is
 * dynamically done by the {@link org.restlet.resource.Finder} class.<br>
 * <br>
 * The HEAD method has a default implementation based on the GET method and the
 * OPTIONS method automatically updates the list of allowed methods in the
 * response, as required by the HTTP specification.<br>
 * <br>
 * Also, you can declare which REST methods are allowed by your Handler by
 * overriding the matching allow*() method. By default, allowOptions() returns
 * true, but all other allow*() methods will return false. Therefore, if you
 * want to accept MOVE method calls, just override allowMove() and return true.
 * Again, the invoking Finder will be able to detect this method and know
 * whether or not your Handler should be invoked. It is also used by the
 * handleOptions() method to return the list of allowed methods.<br>
 * <br>
 * Concurrency note: typically created by Finders, Handler instances are the
 * final handlers of requests. Unlike the other processors in the Restlet chain,
 * a Handler instance is not reused by several calls and is only invoked by one
 * thread. Therefore, it doesn't have to be thread-safe.<br>
 * 
 * @see org.restlet.resource.Finder
 * @author Jerome Louvel
 * @deprecated Use the new {@link ServerResource} class instead.
 */
@Deprecated
public abstract class Handler {

    /**
     * Workaround limitation in Java reflection.
     * 
     * @param method
     *            The method to invoke, potentially in a protected class
     * @return The equivalent method in a public ancestor class.
     * @throws NoSuchMethodException
     * @see <a
     *      href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4071957">Bug
     *      #4071957</a>
     */
    private static java.lang.reflect.Method getAncestorMethod(
            java.lang.reflect.Method method) throws NoSuchMethodException {
        while (!java.lang.reflect.Modifier.isPublic(method.getDeclaringClass()
                .getModifiers())) {
            method = method.getDeclaringClass().getSuperclass().getMethod(
                    method.getName(), method.getParameterTypes());
        }

        return method;
    }

    /** The parent context. */
    private volatile Context context;

    /** The handled request. */
    private volatile Request request;

    /** The returned response. */
    private volatile Response response;

    /**
     * Special constructor used by IoC frameworks. Note that the init() method
     * MUST be invoked right after the creation of the handler in order to keep
     * a behavior consistent with the normal three arguments constructor.
     */
    public Handler() {
    }

    /**
     * Normal constructor.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public Handler(Context context, Request request, Response response) {
        this.context = context;
        this.request = request;
        this.response = response;
    }

    /**
     * Indicates if DELETE calls are allowed. The default value is false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowDelete() {
        return false;
    }

    /**
     * Indicates if GET calls are allowed. The default value is false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowGet() {
        return false;
    }

    /**
     * Indicates if HEAD calls are allowed. The default behavior is to call
     * allowGet().
     * 
     * @return True if the method is allowed.
     */
    public boolean allowHead() {
        return allowGet();
    }

    /**
     * Indicates if OPTIONS calls are allowed. The default value is true.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowOptions() {
        return true;
    }

    /**
     * Indicates if POST calls are allowed. The default value is false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPost() {
        return false;
    }

    /**
     * Indicates if PUT calls are allowed. The default value is false.
     * 
     * @return True if the method is allowed.
     */
    public boolean allowPut() {
        return false;
    }

    /**
     * Generates a reference based on a template URI. Note that you can leverage
     * all the variables defined in the Template class as they will be resolved
     * using the resource's request and response properties.
     * 
     * @param uriTemplate
     *            The URI template to use for generation.
     * @return The generated reference.
     */
    public Reference generateRef(String uriTemplate) {
        final Template tplt = new Template(uriTemplate);
        tplt.setLogger(getLogger());
        return new Reference(tplt.format(getRequest(), getResponse()));
    }

    /**
     * Returns the set of allowed methods.
     * 
     * @return The set of allowed methods.
     */
    public Set<Method> getAllowedMethods() {
        final Set<Method> result = new HashSet<Method>();
        updateAllowedMethods(result);
        return result;
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
        return (this.context != null) ? this.context : Context.getCurrent();
    }

    /**
     * Returns the logger to use.
     * 
     * @return The logger to use.
     */
    public Logger getLogger() {
        return (getContext() != null) ? getContext().getLogger() : Context
                .getCurrentLogger();
    }

    /**
     * Returns the optional matrix of the request's target resource reference as
     * a form (series of parameters).
     * 
     * @return The parsed query.
     * @see Reference#getMatrixAsForm()
     */
    public Form getMatrix() {
        return getRequest().getResourceRef().getMatrixAsForm();
    }

    /**
     * Returns the parsed query of the request's target resource reference as a
     * form (series of parameters).
     * 
     * @return The parsed query.
     * @see Reference#getQueryAsForm()
     */
    public Form getQuery() {
        return getRequest().getResourceRef().getQueryAsForm();
    }

    /**
     * Returns the request.
     * 
     * @return the request.
     */
    public Request getRequest() {
        return this.request;
    }

    /**
     * Returns the response.
     * 
     * @return the response.
     */
    public Response getResponse() {
        return this.response;
    }

    /**
     * Handles a DELETE call. The default behavior, to be overridden by
     * subclasses, is to set the status to {@link Status#SERVER_ERROR_INTERNAL}.
     */
    public void handleDelete() {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Handles a GET call. The default behavior, to be overridden by subclasses,
     * is to set the status to {@link Status#SERVER_ERROR_INTERNAL}.
     */
    public void handleGet() {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Handles a HEAD call. The default behavior is to invoke the handleGet()
     * method. This is the expected behavior of the HTTP 1.1 specification for
     * example. Note that the server connectors will take care of never sending
     * back to the client the response entity bodies.
     */
    public void handleHead() {
        handleGet();
    }

    /**
     * Handles an OPTIONS call introspecting the target resource (as provided by
     * the 'findTarget' method). The default implementation is based on the HTTP
     * specification which says that OPTIONS should return the list of allowed
     * methods in the Response headers.
     */
    public void handleOptions() {
        updateAllowedMethods();
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    /**
     * Handles a POST call. The default behavior, to be overridden by
     * subclasses, is to set the status to {@link Status#SERVER_ERROR_INTERNAL}.
     */
    public void handlePost() {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Handles a PUT call. The default behavior, to be overridden by subclasses,
     * is to set the status to {@link Status#SERVER_ERROR_INTERNAL}.
     */
    public void handlePut() {
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }

    /**
     * Initialize the resource with its context. If you override this method,
     * make sure that you don't forget to call super.init() first, otherwise
     * your Resource won't behave properly.
     * 
     * @param context
     *            The parent context.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     */
    public void init(Context context, Request request, Response response) {
        this.context = context;
        this.request = request;
        this.response = response;
    }

    /**
     * Invokes a method with the given arguments.
     * 
     * @param method
     *            The method to invoke.
     * @param args
     *            The arguments to pass.
     * @return Invocation result.
     */
    private Object invoke(java.lang.reflect.Method method, Object... args) {
        Object result = null;

        if (method != null) {
            try {
                result = method.invoke(this, args);
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
     * Sets the parent context.
     * 
     * @param context
     *            The parent context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the request to handle.
     * 
     * @param request
     *            The request to handle.
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Sets the response to update.
     * 
     * @param response
     *            The response to update.
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * Updates the set of allowed methods on the response.
     */
    public void updateAllowedMethods() {
        updateAllowedMethods(getResponse().getAllowedMethods());
    }

    /**
     * Updates the set of methods with the ones allowed by this resource
     * instance.
     * 
     * @param allowedMethods
     *            The set to update.
     */
    private void updateAllowedMethods(Set<Method> allowedMethods) {
        for (final java.lang.reflect.Method orig_classMethod : getClass()
                .getMethods()) {
            java.lang.reflect.Method classMethod;

            try {
                classMethod = getAncestorMethod(orig_classMethod);

                if (classMethod.getName().startsWith("allow")
                        && (classMethod.getParameterTypes().length == 0)) {
                    if ((Boolean) invoke(classMethod)) {
                        final Method allowedMethod = Method.valueOf(classMethod
                                .getName().substring(5));
                        allowedMethods.add(allowedMethod);
                    }
                }
            } catch (NoSuchMethodException e) {
                getLogger().log(Level.FINE,
                        "Unable to find a public version of this method.", e);
            }
        }
    }

}
