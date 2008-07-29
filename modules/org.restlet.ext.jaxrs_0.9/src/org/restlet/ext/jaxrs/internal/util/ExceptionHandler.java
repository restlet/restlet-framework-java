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

    /**
     * Creates a new ExceptionHandler.
     * 
     * @param logger
     *                the logger to use
     */
    public ExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    /**
     * handles a {@link ConvertRepresentationException}
     * 
     * @param cre
     * @return
     * @throws WebApplicationException
     */
    public RequestHandledException convertRepresentationExc(
            ConvertRepresentationException cre) throws WebApplicationException {
        final ResponseBuilder rb = Response.status(Status.BAD_REQUEST);
        final StringWriter stw = new StringWriter();
        cre.printStackTrace(new PrintWriter(stw));
        rb.entity(stw.toString());
        throw new WebApplicationException(cre, rb.build());
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     * @param callContext
     *            Contains the encoded template Parameters, that are read from
     *            the called URI, the Restlet {@link org.restlet.data.Request}
     *            and the Restlet {@link org.restlet.data.Response}.
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
     *            the called URI, the Restlet {@link org.restlet.data.Request}
     *            and the Restlet {@link org.restlet.data.Response}.
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
     *            the called URI, the Restlet {@link org.restlet.data.Request}
     *            and the Restlet {@link org.restlet.data.Response}.
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
     * see spec, section 4.3.2, item 7
     * 
     * @param accMediaTypes
     * @param entityClass
     * @param genericType 
     * @param annotations 
     * 
     * @return staticly to throw, if needed by compiler.
     */
    public WebApplicationException noMessageBodyWriter(Class<?> entityClass,
            Type genericType, Annotation[] annotations) {
        this.logger.warning("No message body writer found for class "
                + entityClass + ", genericType " + genericType);
        annotations.toString(); // LATER log also annotations
        // NICE get as parameters the accMediaTypes and the entityClass.
        // and return supported MediaTypes as entity
        throw new WebApplicationException(Status.NOT_ACCEPTABLE);
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
     * @throws WebApplicationException
     */
    public void resourceMethodNotFound() throws WebApplicationException {
        throw new WebApplicationException(Status.NOT_FOUND);
    }

    /**
     * see spec, section 3.7.2, item 2 (e)
     * 
     * @throws WebApplicationException
     */
    public void resourceNotFound() throws WebApplicationException {
        throw new WebApplicationException(Status.NOT_FOUND);
    }

    /**
     * see spec, section 3.7.2, item 1 (d)
     * 
     * @throws WebApplicationException
     */
    public void rootResourceNotFound() throws WebApplicationException {
        throw new WebApplicationException(Status.NOT_FOUND);
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
     *                the exception to log
     * @param jaxRsMethod
     *                the called method when the exception occurs. May be null.
     * @param callContext
     *            Contains the encoded template Parameters, that are read from
     *            the called URI, the Restlet {@link org.restlet.data.Request}
     *            and the Restlet {@link org.restlet.data.Response}.
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