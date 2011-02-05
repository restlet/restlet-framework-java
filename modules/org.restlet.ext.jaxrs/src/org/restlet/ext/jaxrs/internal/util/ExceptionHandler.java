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

package org.restlet.ext.jaxrs.internal.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NotAcceptableWebAppException;
import org.restlet.ext.jaxrs.internal.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.internal.exceptions.UnsupportedMediaTypeWebAppException;
import org.restlet.ext.jaxrs.internal.wrappers.AbstractMethodWrapper;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;

/**
 * <p>
 * This class contains the methods to handle exceptions occuring in the
 * {@link org.restlet.ext.jaxrs.JaxRsRestlet}, e.g. while identifying the method
 * that should handle the request.<br>
 * Therefor it contains some Restlets that handles this exceptions.
 * </p>
 * <p>
 * Perhaps this class gets again public. Perhaps the special Restlets for
 * handling will be removed, or stay only for 404.
 * </p>
 * 
 * @author Stephan Koops
 */
public class ExceptionHandler {

    private static final String HEADER_ALLOW = "Allow";

    /**
     * @param supporting
     * @return
     */
    private static Set<Variant> getSupportedVariants(
            Collection<ResourceMethod> supporting) {
        final Set<Variant> supportedVariants = new HashSet<Variant>();
        for (final ResourceMethod resourceMethod : supporting) {
            supportedVariants.addAll(resourceMethod.getSupportedVariants());
        }
        return supportedVariants;
    }

    private final Logger logger;

    private volatile Restlet noResMethodHandler;

    private volatile Restlet noResourceClHandler;

    private volatile Restlet noRootResClHandler;

    /**
     * Creates a new ExceptionHandler.
     * 
     * @param logger
     *            the logger to use
     */
    public ExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    /**
     * handles a {@link ConvertRepresentationException}
     * 
     * @param cre
     * @return (static the thrown exeption for the compiler)
     * @throws WebApplicationException
     */
    public WebApplicationException convertRepresentationExc(
            ConvertRepresentationException cre) throws WebApplicationException {
        final ResponseBuilder rb = Response.status(Status.BAD_REQUEST);
        final StringWriter stw = new StringWriter();
        cre.printStackTrace(new PrintWriter(stw));
        rb.entity(stw.toString());
        throw new WebApplicationException(cre, rb.build());
    }

    /**
     * Returns the Restlet that is called, if no resource method class could be
     * found.
     * 
     * @return the Restlet that is called, if no resource method class could be
     *         found.
     * @see #setNoResMethodHandler(Restlet)
     */
    public Restlet getNoResMethodHandler() {
        return noResMethodHandler;
    }

    /**
     * Returns the Restlet that is called, if no resource class could be found.
     * 
     * @return the Restlet that is called, if no resource class could be found.
     */
    public Restlet getNoResourceClHandler() {
        return noResourceClHandler;
    }

    /**
     * Returns the Restlet that is called, if no root resource class could be
     * found.
     * 
     * @return the Restlet that is called, if no root resource class could be
     *         found.
     * @see #setNoRootResClHandler(Restlet)
     */
    public Restlet getNoRootResClHandler() {
        return noRootResClHandler;
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     * @param callContext
     *            Contains the encoded template Parameters, that are read from
     *            the called URI, the Restlet {@link org.restlet.Request} and
     *            the Restlet {@link org.restlet.Response}.
     * @param methodName
     * @param logMessage
     * @return staticly to throw, if needed by compiler.
     * @throws RequestHandledException
     *             throws this message to exit the method and indicate, that the
     *             request was handled.
     * @throws RequestHandledException
     */
    public RequestHandledException instantiateExecption(
            InstantiateException exception, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(
                org.restlet.data.Status.SERVER_ERROR_INTERNAL);
        this.logger.log(Level.WARNING, logMessage, exception.getCause());
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     * @param callContext
     *            Contains the encoded template Parameters, that are read from
     *            the called URI, the Restlet {@link org.restlet.Request} and
     *            the Restlet {@link org.restlet.Response}.
     * @param methodName
     * @param logMessage
     * @return staticly to throw, if needed by compiler.
     * @throws RequestHandledException
     *             throws this message to exit the method and indicate, that the
     *             request was handled.
     * @throws RequestHandledException
     */
    public RequestHandledException methodInvokeException(
            MethodInvokeException exception, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(
                org.restlet.data.Status.SERVER_ERROR_INTERNAL);
        this.logger.log(Level.WARNING, logMessage, exception.getCause());
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * @param allowedMethods
     * @throws WebApplicationException
     */
    public void methodNotAllowed(Set<Method> allowedMethods)
            throws WebApplicationException {
        final ResponseBuilder rb = Response
                .status(org.restlet.data.Status.CLIENT_ERROR_METHOD_NOT_ALLOWED
                        .getCode());
        rb.header(HEADER_ALLOW, Util.toString(allowedMethods, ", "));
        throw new WebApplicationException(rb.build());
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     * @param callContext
     *            Contains the encoded template Parameters, that are read from
     *            the called URI, the Restlet {@link org.restlet.Request} and
     *            the Restlet {@link org.restlet.Response}.
     * @param methodName
     * @param logMessage
     * @return staticly to throw, if needed by compiler.
     * @throws RequestHandledException
     *             throws this message to exit the method and indicate, that the
     *             request was handled.
     * @throws RequestHandledException
     */
    public RequestHandledException missingAnnotation(
            MissingAnnotationException exception, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(
                org.restlet.data.Status.SERVER_ERROR_INTERNAL);
        if (exception != null) {
            logMessage += ": " + exception.getMessage();
        }
        this.logger.log(Level.WARNING, logMessage);
        throw new RequestHandledException();
    }

    /**
     * @param entityClass
     * @param genericType
     * @param annotations
     * @param respMediaType
     * @param accMediaTypes
     * @return (static the thrown exeption for the compiler)
     * @throws WebApplicationException
     *             the exception to throw according to the JAX-RS specification.
     */
    public WebApplicationException noMessageBodyWriter(
            Class<? extends Object> entityClass, Type genericType,
            Annotation[] annotations, MediaType respMediaType,
            SortedMetadata<MediaType> accMediaTypes)
            throws WebApplicationException {
        String warning = "No message body writer found for class "
                + entityClass + ", genericType " + genericType;
        if (respMediaType != null) {
            warning += "; response media type should be: " + respMediaType;
        }
        if (accMediaTypes != null) {
            warning += "; accepted media types are: " + accMediaTypes;
        }
        this.logger.warning(warning);
        annotations.toString(); // LATER log also annotations
        // NICE get as parameters the accMediaTypes and the entityClass.
        // and return supported MediaTypes as entity
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * see spec, section 3.7.2, item 3(a).4
     * 
     * @param supporting
     *            the methods supporting the requested resource and the given
     *            HTTP method.
     * @throws WebApplicationException
     */
    public void noResourceMethodForAccMediaTypes(
            Collection<ResourceMethod> supporting)
            throws WebApplicationException {
        final Set<Variant> supportedVariants = getSupportedVariants(supporting);
        throw new NotAcceptableWebAppException(supportedVariants);
    }

    /**
     * see spec, section 3.8, item 6
     * 
     * @return staticly to throw, if needed by compiler.
     * @throws WebApplicationException
     */
    public WebApplicationException notAcceptableWhileDetermineMediaType()
            throws WebApplicationException {
        // NICE return supported MediaTypes as entity
        throw new WebApplicationException(Status.NOT_ACCEPTABLE);
    }

    /**
     * Handles the case, if no resource method was found. If a Restlet to handle
     * this case was given (see {@link #setNoResMethodHandler(Restlet)}), it is
     * called. Otherwise a {@link WebApplicationException} with status 404 is
     * thrown (see JAX-RS specification)
     * 
     * @throws WebApplicationException
     * @throws RequestHandledException
     */
    public void resourceMethodNotFound() throws WebApplicationException,
            RequestHandledException {
        if (this.noResMethodHandler != null) {
            this.noResMethodHandler.handle(Request.getCurrent(),
                    org.restlet.Response.getCurrent());
            throw new RequestHandledException();
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    /**
     * Handles the case, if no resource class was found. If a Restlet to handle
     * this case was given (see {@link #setNoResourceClHandler(Restlet)}), it is
     * called. Otherwise a {@link WebApplicationException} with status 404 is
     * thrown (see spec, section 3.7.2, item 2e)
     * 
     * @throws WebApplicationException
     * @throws RequestHandledException
     */
    public void resourceNotFound() throws WebApplicationException,
            RequestHandledException {
        if (this.noResourceClHandler != null) {
            this.noResourceClHandler.handle(Request.getCurrent(),
                    org.restlet.Response.getCurrent());
            throw new RequestHandledException();
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    /**
     * Handles the case, if no root resource class was found. If a Restlet to
     * handle this case was given (see {@link #setNoRootResClHandler(Restlet)}),
     * it is called. Otherwise a {@link WebApplicationException} with status 404
     * is thrown (see JAX-RS specification, section 3.7.2, item 1d)
     * 
     * @throws WebApplicationException
     * @throws RequestHandledException
     */
    public void rootResourceNotFound() throws WebApplicationException,
            RequestHandledException {
        if (this.noRootResClHandler != null) {
            this.noRootResClHandler.handle(Request.getCurrent(),
                    org.restlet.Response.getCurrent());
            throw new RequestHandledException();
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     *            the exception to log
     * @param jaxRsMethod
     *            the called method when the exception occurs. May be null.
     * @param callContext
     *            Contains the encoded template Parameters, that are read from
     *            the called URI, the Restlet {@link org.restlet.Request} and
     *            the Restlet {@link org.restlet.Response}.
     * @param logMessage
     * @param methodName
     * @return staticly to throw, if needed by compiler.
     * @throws RequestHandledException
     *             throws this message to exit the method and indicate, that the
     *             request was handled.
     * @throws RequestHandledException
     */
    public RequestHandledException runtimeExecption(RuntimeException exception,
            AbstractMethodWrapper jaxRsMethod, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(
                org.restlet.data.Status.SERVER_ERROR_INTERNAL);
        if (jaxRsMethod != null) {
            logMessage = jaxRsMethod + ": " + logMessage;
        }
        this.logger.log(Level.WARNING, jaxRsMethod + ": " + logMessage,
                exception);
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * Sets the Restlet that will handle the {@link Request}s, if no resource
     * method could be found.
     * 
     * @param noResMethodHandler
     *            the noResMethodHandler to set
     * @see #getNoResMethodHandler()
     * @see #setNoResourceClHandler(Restlet)
     * @see #setNoRootResClHandler(Restlet)
     */
    public void setNoResMethodHandler(Restlet noResMethodHandler) {
        this.noResMethodHandler = noResMethodHandler;
    }

    /**
     * Sets the Restlet that will handle the {@link Request}s, if no resource
     * class could be found.
     * 
     * @param noResourceClHandler
     *            the noResourceClHandler to set
     * @see #getNoResourceClHandler()
     * @see #setNoResMethodHandler(Restlet)
     * @see #setNoRootResClHandler(Restlet)
     */
    public void setNoResourceClHandler(Restlet noResourceClHandler) {
        this.noResourceClHandler = noResourceClHandler;
    }

    /**
     * Sets the Restlet that is called, if no root resource class could be
     * found.
     * 
     * @param noRootResClHandler
     *            the Restlet to call, if no root resource class could be found.
     * @see #getNoRootResClHandler(Restlet)
     * @see #setNoResourceClHandler()
     * @see #setNoResMethodHandler(Restlet)
     */
    public void setNoRootResClHandler(Restlet noRootResClHandler) {
        this.noRootResClHandler = noRootResClHandler;
    }

    /**
     * see spec, section 3.7.2, item 3 (a) .3
     * 
     * @param accepting
     *            resource methods for the requested resource and the given HTTP
     *            method.
     * @throws WebApplicationException
     */
    public void unsupportedMediaType(Collection<ResourceMethod> accepting)
            throws WebApplicationException {
        final Set<Variant> acceptedVariants = getSupportedVariants(accepting);
        throw new UnsupportedMediaTypeWebAppException(acceptedVariants);
    }
}