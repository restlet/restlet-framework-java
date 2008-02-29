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
package org.restlet.ext.jaxrs;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import javax.ws.rs.WebApplicationException;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.core.CallContext;
import org.restlet.ext.jaxrs.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.util.RemainingPath;
import org.restlet.ext.jaxrs.wrappers.AbstractMethodWrapper;
import org.restlet.ext.jaxrs.wrappers.ResourceClass;
import org.restlet.ext.jaxrs.wrappers.ResourceMethodOrLocator;
import org.restlet.ext.jaxrs.wrappers.ResourceObject;
import org.restlet.ext.jaxrs.wrappers.RootResourceClass;
import org.restlet.resource.StringRepresentation;

/**
 * This class contains only the methods to handle exceptions while identifying
 * the method taht should handle the request and in other situations. Therefor
 * it contains some Restlets that handles this exceptions. The
 * {@link JaxRsRouter} is a subclass of this class. By moving all this methods
 * and so on to this super class, the class {@link JaxRsRouter} contains only
 * the real logic code and is more well arranged.
 * 
 * @author Stephan Koops
 */
abstract class JaxRsRouterHelpMethods extends Restlet {

    /**
     * This exception is thrown, when the algorithm "Matching Requests to
     * Resource Methods" in Section 2.5 of JSR-311-Spec could not find a method.
     * 
     * @author Stephan Koops
     */
    class CouldNotFindMethodException extends Exception {
        private static final long serialVersionUID = -8436314060905405146L;

        Restlet errorRestlet;

        CouldNotFindMethodException(Restlet errorRestlet, String message) {
            super(message);
            this.errorRestlet = errorRestlet;
        }
    }

    /**
     * Instances of this class have a given status they return, when
     * {@link Restlet#handle(Request, Response)} is called.
     * 
     * @author Stephan Koops
     */
    private static class ReturnStatusRestlet extends Restlet {
        private Status status;

        ReturnStatusRestlet(Status status) {
            this.status = status;
        }

        @Override
        public void handle(Request request, Response response) {
            super.handle(request, response);
            response.setStatus(status);
        }
    }

    /**
     * The default Restlet used when the method is not allwed on the resource.
     * 
     * @see #errorRestletMethodNotAllowed
     */
    private static final ReturnStatusRestlet DEFAULT_METHOD_NOT_ALLOWED_RESTLET = new ReturnStatusRestlet(
            Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

    /**
     * The default Restlet used when multiple possible resource methods was
     * found.
     * 
     * @see #errorRestletMultipleResourceMethods
     */
    private static final ReturnStatusRestlet DEFAULT_MULTIPLE_RESOURCE_METHODS = new ReturnStatusRestlet(
            new Status(Status.SERVER_ERROR_INTERNAL,
                    "Multiple possible resource methods found"));

    /**
     * The default Restlet used when multiple root resource were found.
     * 
     * @see #errorRestletMultipleRootResourceClasses
     */
    private static final ReturnStatusRestlet DEFAULT_MULTIPLE_ROOT_RESOURCE_CLASSES = new ReturnStatusRestlet(
            new Status(Status.SERVER_ERROR_INTERNAL,
                    "Multiple possible root resource classes found"));

    /**
     * The default Restlet used when the request is not acceptable.
     * 
     * @see #errorRestletRootResourceNotFound
     */
    private static final ReturnStatusRestlet DEFAULT_NOT_ACCEPTABLE_RESTLET = new ReturnStatusRestlet(
            Status.CLIENT_ERROR_NOT_ACCEPTABLE);

    /**
     * The default Restlet used when a (sub) resource method can not be found.
     * 
     * @see #errorRestletResourceMethodNotFound
     */
    private static final ReturnStatusRestlet DEFAULT_RESOURCE_METHOD_NOT_FOUND_RESTLET = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Resource method not found or it is not public"));

    /**
     * The default Restlet used when a (sub) resource can not be found.
     * 
     * @see #errorRestletResourceNotFound
     */
    private static final ReturnStatusRestlet DEFAULT_RESOURCE_NOT_FOUND_RESTLET = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Resource class not found"));

    /**
     * The default Restlet used when a root resource can not be found.
     * 
     * @see #errorRestletRootResourceNotFound
     */
    private static final ReturnStatusRestlet DEFAULT_ROOT_RESOURCE_NOT_FOUND_RESTLET = new ReturnStatusRestlet(
            new Status(Status.CLIENT_ERROR_NOT_FOUND,
                    "Root resource class not found"));

    /**
     * The default Restlet used when the media type is not supported
     * 
     * @see #errorRestletUnsupportedMediaType
     */
    private static final ReturnStatusRestlet DEFAULT_UNSUPPORTED_MEDIA_TYPE_RESTLET = new ReturnStatusRestlet(
            Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);

    private Restlet errorRestletMethodNotAllowed = DEFAULT_METHOD_NOT_ALLOWED_RESTLET;

    private Restlet errorRestletMultipleResourceMethods = DEFAULT_MULTIPLE_RESOURCE_METHODS;

    private Restlet errorRestletMultipleRootResourceClasses = DEFAULT_MULTIPLE_ROOT_RESOURCE_CLASSES;

    private Restlet errorRestletNoResourceMethodForAcceptedMediaType = DEFAULT_NOT_ACCEPTABLE_RESTLET;

    private Restlet errorRestletNotAcceptableWhileDetermineMediaType = DEFAULT_NOT_ACCEPTABLE_RESTLET;

    /**
     * When no Method for the give path is found
     */
    private Restlet errorRestletResourceMethodNotFound = DEFAULT_RESOURCE_METHOD_NOT_FOUND_RESTLET;

    private Restlet errorRestletResourceNotFound = DEFAULT_RESOURCE_NOT_FOUND_RESTLET;

    /**
     * This Restlet will be used to handle the request if no root resource class
     * can be found.
     */
    private Restlet errorRestletRootResourceNotFound = DEFAULT_ROOT_RESOURCE_NOT_FOUND_RESTLET;

    private Restlet errorRestletUnsupportedMediaType = DEFAULT_UNSUPPORTED_MEDIA_TYPE_RESTLET;

    JaxRsRouterHelpMethods(Context context) {
        super(context);
    }

    /**
     * @return Returns the Restlet, that is actually if no resource method could
     *         be found.
     */
    public Restlet getErrorRestletMethodNotAllowed() {
        return errorRestletMethodNotAllowed;
    }

    /**
     * @return Returns the Restlet that handles the request if multiple resource
     *         methods for a request were found.
     */
    public Restlet getErrorRestletMultipleResourceMethods() {
        return errorRestletMultipleResourceMethods;
    }

    /**
     * @return Returns the request if multiple root resource classes were found.
     */
    public Restlet getErrorRestletMultipleRootResourceClasses() {
        return errorRestletMultipleRootResourceClasses;
    }

    /**
     * @return Returns the Restlet that hanndles the request if the accepted
     *         media type is not supported.
     */
    public Restlet getErrorRestletNoResourceMethodForAcceptedMediaType() {
        return errorRestletNoResourceMethodForAcceptedMediaType;
    }

    /**
     * @return Returns the Restlet that hanndles the request if the media type
     *         of a method result could not be determined.
     */
    public Restlet getErrorRestletNotAcceptableWhileDetermineMediaType() {
        return errorRestletNotAcceptableWhileDetermineMediaType;
    }

    /**
     * @return Returns the Restlet, is used on HTTP-Error 404, when no Resource
     *         class could be found.
     */
    public Restlet getErrorRestletResourceMethodNotFound() {
        return this.errorRestletResourceMethodNotFound;
    }

    /**
     * @return Returns the Restlet, that is actually if no resource class could
     *         be found.
     */
    public Restlet getErrorRestletResourceNotFound() {
        return this.errorRestletResourceNotFound;
    }

    /**
     * @return Returns the Restlet, that is actually if no Root Resource class
     *         could be found.
     */
    public Restlet getErrorRestletRootResourceNotFound() {
        return this.errorRestletRootResourceNotFound;
    }

    /**
     * @return Returns the Restlet that handles the request, if the method is
     *         not allowed on the resource.
     */
    public Restlet getErrorRestletUnsupportedMediaType() {
        return errorRestletUnsupportedMediaType;
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     * @param resourceMethod
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param methodName
     * @param logMessage
     * @throws RequestHandledException
     *                 throws this message to exit the method and indicate, that
     *                 the request was handled.
     * @throws RequestHandledException
     */
    RequestHandledException handleExecption(Throwable exception,
            AbstractMethodWrapper resourceMethod, CallContext callContext,
            String logMessage) throws RequestHandledException {
        if (exception instanceof InvocationTargetException)
            exception = exception.getCause();
        if (exception instanceof WebApplicationException) {
            WebApplicationException webAppExc = (WebApplicationException) exception;
            throw handleWebAppExc(webAppExc, callContext, resourceMethod);
        }
        callContext.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        getLogger().log(Level.WARNING, logMessage, exception.getCause());
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * Handles the given {@link WebApplicationException}.
     * 
     * @param webAppExc
     *                The {@link WebApplicationException} to handle
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @throws RequestHandledException
     *                 throws this message to exit the method and indicate, that
     *                 the request was handled.
     */
    RequestHandledException handleWebAppExc(WebApplicationException webAppExc,
            CallContext callContext, AbstractMethodWrapper resourceMethod)
            throws RequestHandledException {
        // the message of the Exception is not used in the
        // WebApplicationException
        jaxRsRespToRestletResp(webAppExc.getResponse(), callContext,
                resourceMethod);
        // LATER MediaType rausfinden
        throw new RequestHandledException();
    }

    /**
     * Converts the given JAX-RS {@link javax.ws.rs.core.Response} to a Restlet
     * {@link Response}.
     */
    abstract void jaxRsRespToRestletResp(
            javax.ws.rs.core.Response jaxRsResponse, CallContext callContext,
            AbstractMethodWrapper resourceMethod)
            throws RequestHandledException;

    /**
     * Set the Restlet to handle the request if the method is not allowed for
     * the resource. It must return status 405. Set it to null to use default.
     * 
     * @param errorRestletMethodNotAllowed
     *                The Restlet to use. Set to null to use default.
     */
    public void setErrorRestletMethodNotAllowed(
            Restlet errorRestletMethodNotAllowed) {
        if (errorRestletMethodNotAllowed == null)
            this.errorRestletMethodNotAllowed = DEFAULT_METHOD_NOT_ALLOWED_RESTLET;
        else
            this.errorRestletMethodNotAllowed = errorRestletMethodNotAllowed;
    }

    /**
     * Set the Restlet to handle the request if multiple resource methods for a
     * request were found. Set to null to use default.
     * 
     * @param errorRestletMultipleResourceMethods
     *                The Restlet to use. Set to null to use default.
     */
    public void setErrorRestletMultipleResourceMethods(
            Restlet errorRestletMultipleResourceMethods) {
        if (errorRestletMultipleResourceMethods == null)
            this.errorRestletMultipleResourceMethods = DEFAULT_MULTIPLE_RESOURCE_METHODS;
        else
            this.errorRestletMultipleResourceMethods = errorRestletMultipleResourceMethods;
    }

    /**
     * Set the Restlet to handle the request if multiple root resource classes
     * for a request were found. Set to null to use default.
     * 
     * @param errorRestletMultipleRootResourceClasses
     *                The Restlet to use. Set to null to use default.
     */
    public void setErrorRestletMultipleRootResourceClasses(
            Restlet errorRestletMultipleRootResourceClasses) {
        if (errorRestletMultipleRootResourceClasses == null)
            this.errorRestletMultipleRootResourceClasses = DEFAULT_MULTIPLE_ROOT_RESOURCE_CLASSES;
        else
            this.errorRestletMultipleRootResourceClasses = errorRestletMultipleRootResourceClasses;
    }

    /**
     * Sets the Restlet that should handle the request, if the accpeted media
     * type is not supported. Must return status 406. Set to null to use
     * default.
     * 
     * @param errorRestletNoResourceMethodForAcceptedMediaType
     *                The Restlet to use. Set to null to use default.
     */
    public void setErrorRestletNoResourceMethodForAcceptedMediaType(
            Restlet errorRestletNoResourceMethodForAcceptedMediaType) {
        if (errorRestletNoResourceMethodForAcceptedMediaType == null)
            this.errorRestletNoResourceMethodForAcceptedMediaType = DEFAULT_NOT_ACCEPTABLE_RESTLET;
        else
            this.errorRestletNoResourceMethodForAcceptedMediaType = errorRestletNoResourceMethodForAcceptedMediaType;
    }

    /**
     * Sets the Restlet that handles the request if the media type of the
     * generated response could not be determined. Set to null to use default.
     * 
     * @param errorRestletNotAcceptableWhileDetermineMediaType
     *                The Restlet to use. Set to null to use default.
     */
    public void setErrorRestletNotAcceptableWhileDetermineMediaType(
            Restlet errorRestletNotAcceptableWhileDetermineMediaType) {
        if (errorRestletNotAcceptableWhileDetermineMediaType == null)
            this.errorRestletNotAcceptableWhileDetermineMediaType = DEFAULT_UNSUPPORTED_MEDIA_TYPE_RESTLET;
        else
            this.errorRestletNotAcceptableWhileDetermineMediaType = errorRestletNotAcceptableWhileDetermineMediaType;
    }

    /**
     * Sets the Restlet that handles request, when no Resource class could be
     * found. It must return status 404, if it can not otherwise handle the
     * request. Set to null to use default.
     * 
     * @param notFoundRestlet
     *                The Restlet to use when the resource was not found on the
     *                server. This Restlet must return status 404. Set to null
     *                to use default.
     */
    public void setErrorRestletNotFound(Restlet notFoundRestlet) {
        this.setErrorRestletResourceMethodNotFound(notFoundRestlet);
        this.setErrorRestletResourceNotFound(notFoundRestlet);
        this.setErrorRestletRootResourceNotFound(notFoundRestlet);
    }

    /**
     * Sets the Restlet that handles the request if no Resource class could be
     * found. Set to null to use default.
     * 
     * @param resourceMethodNotFoundRestlet
     *                The Restlet to use when no resource class could be found.
     *                This Restlet must return status 404. Set to null to use
     *                default.
     * @see #setErrorRestletNotFound(Restlet)
     */
    public void setErrorRestletResourceMethodNotFound(
            Restlet resourceMethodNotFoundRestlet) {
        if (resourceMethodNotFoundRestlet == null)
            this.errorRestletResourceMethodNotFound = DEFAULT_RESOURCE_METHOD_NOT_FOUND_RESTLET;
        else
            this.errorRestletResourceMethodNotFound = resourceMethodNotFoundRestlet;
    }

    /**
     * Sets the Restlet that handles request, when no Resource class could be
     * found. It must return status 404, if it can not otherwise handle the
     * request. Set to null to use default.
     * 
     * @param resourceNotFoundRestlet
     *                The Restlet to use when no resource class could be found.
     *                This Restlet must return status 404. Set to null to use
     *                default.
     * @see #setErrorRestletNotFound(Restlet)
     */
    public void setErrorRestletResourceNotFound(Restlet resourceNotFoundRestlet) {
        if (resourceNotFoundRestlet == null)
            this.errorRestletResourceNotFound = DEFAULT_RESOURCE_NOT_FOUND_RESTLET;
        else
            this.errorRestletResourceNotFound = resourceNotFoundRestlet;
    }

    /**
     * Restlet, is used on HTTP-Error 404, when no Root Resource class could be
     * found. Set to null to use default.
     * 
     * @param rootResourceNotFoundRestlet
     *                The Restlet to use when no root resource class could be
     *                found. This Restlet must return status 404.
     * @see #setErrorRestletNotFound(Restlet)
     */
    public void setErrorRestletRootResourceNotFound(
            Restlet rootResourceNotFoundRestlet) {
        if (rootResourceNotFoundRestlet == null)
            this.errorRestletRootResourceNotFound = DEFAULT_ROOT_RESOURCE_NOT_FOUND_RESTLET;
        else
            this.errorRestletRootResourceNotFound = rootResourceNotFoundRestlet;
    }

    /**
     * Sets the Restlet that handles the request if the given media type is not
     * supported. Set to null to use default.
     * 
     * @param errorRestletUnsupportedMediaType
     *                The Restlet to use. Set to null to use default.
     */
    public void setErrorRestletUnsupportedMediaType(
            Restlet errorRestletUnsupportedMediaType) {
        if (errorRestletUnsupportedMediaType == null)
            this.errorRestletUnsupportedMediaType = DEFAULT_UNSUPPORTED_MEDIA_TYPE_RESTLET;
        else
            this.errorRestletUnsupportedMediaType = errorRestletUnsupportedMediaType;
    }

    /**
     * @param httpMethod
     * @param resourceClass
     * @param u
     * @throws CouldNotFindMethodException
     */
    void throwMethodNotAllowed(org.restlet.data.Method httpMethod,
            ResourceClass resourceClass, RemainingPath u)
            throws CouldNotFindMethodException {
        throw new CouldNotFindMethodException(errorRestletMethodNotAllowed,
                "there is no method supporting the http method " + httpMethod
                        + " on class " + resourceClass.getName()
                        + " and remaining path " + u);
    }

    /**
     * @param bestMethod
     * @param currentMethod
     * @throws CouldNotFindMethodException
     *                 you can throw the result, if the compiler want to get
     *                 sure, that you leave the calling method.
     */
    void throwMultipleResourceMethods(ResourceMethodOrLocator bestMethod,
            ResourceMethodOrLocator currentMethod)
            throws CouldNotFindMethodException {
        String message = "Multiple java methods found on "
                + currentMethod.getResourceClass().getName() + ": "
                + bestMethod.getName() + " and " + currentMethod.getName();
        getLogger().warning(message);
        throw new CouldNotFindMethodException(
                this.errorRestletMultipleResourceMethods, message);
    }

    /**
     * @param bestRrc
     * @param currentRrc
     * @throws CouldNotFindMethodException
     */
    void throwMultipleRootResourceClasses(RootResourceClass bestRrc,
            RootResourceClass currentRrc) throws CouldNotFindMethodException {
        String message = "there are multiple ressources for the same path: "
                + bestRrc.getPathRegExp() + " and "
                + currentRrc.getPathRegExp() + " (and perhaps more)";
        getLogger().warning(message);
        throw new CouldNotFindMethodException(
                this.errorRestletMultipleRootResourceClasses, message);
    }

    /**
     * @param httpMethod
     * @param resourceClass
     * @param u
     * @throws CouldNotFindMethodException
     */
    void throwNoResourceMethodForAccMediaTypes(
            org.restlet.data.Method httpMethod, ResourceClass resourceClass,
            RemainingPath u) throws CouldNotFindMethodException {
        // LATER return MediaTypes are supported.
        throw new CouldNotFindMethodException(
                errorRestletNoResourceMethodForAcceptedMediaType,
                "there is no java method on class " + resourceClass.getName()
                        + " supporting the http method " + httpMethod
                        + " and remaining path " + u
                        + " and the given and accepted media types");
    }

    RequestHandledException throwNotAcceptableWhileDetermineMediaType(
            Request request, Response response) throws RequestHandledException {
        errorRestletNoResourceMethodForAcceptedMediaType.handle(request,
                response);
        response.setEntity(new StringRepresentation(
                "Could not determinde the media type of the created response",
                MediaType.TEXT_PLAIN, Language.ENGLISH));
        throw new RequestHandledException();
    }

    /**
     * @param resourceClass
     * @param u
     * @throws CouldNotFindMethodException
     */
    void throwResourceMethodNotFound(ResourceClass resourceClass,
            RemainingPath u) throws CouldNotFindMethodException {
        throw new CouldNotFindMethodException(
                errorRestletResourceMethodNotFound,
                "there is no method on class " + resourceClass.getName()
                        + " for remaining path " + u);
    }

    /**
     * @param o
     * @param u
     * @throws CouldNotFindMethodException
     */
    void throwResourceNotFound(ResourceObject o, RemainingPath u)
            throws CouldNotFindMethodException {
        throw new CouldNotFindMethodException(
                errorRestletResourceNotFound,
                "The resource class "
                        + o.getResourceClass().getName()
                        + " could not find a resource object to handle the request for remaining path "
                        + u);
    }

    /**
     * @param u
     * @throws CouldNotFindMethodException
     */
    void throwRootResourceNotFound(RemainingPath u)
            throws CouldNotFindMethodException {
        throw new CouldNotFindMethodException(errorRestletRootResourceNotFound,
                "No root resource class found for realtiv path " + u);
    }

    /**
     * @param httpMethod
     * @param resourceClass
     * @param u
     * @param givenMediaType
     * @throws CouldNotFindMethodException
     */
    void throwUnsupportedMediaType(org.restlet.data.Method httpMethod,
            ResourceClass resourceClass, RemainingPath u,
            MediaType givenMediaType) throws CouldNotFindMethodException {
        throw new CouldNotFindMethodException(errorRestletUnsupportedMediaType,
                "there is no java method on class "
                        + resourceClass.getName()
                        + " supporting the http method "
                        + httpMethod
                        + " on an "
                        + (u.isEmptyOrSlash() ? "empty remaining path"
                                : (" remaining path " + u))
                        + " and the given media type " + givenMediaType);
    }
}