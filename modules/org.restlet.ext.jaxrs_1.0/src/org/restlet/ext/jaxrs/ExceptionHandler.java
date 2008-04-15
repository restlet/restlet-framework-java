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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.AbstractMethodWrapper;
import org.restlet.resource.StringRepresentation;

/**
 * <p>
 * This class contains the methods to handle exceptions occuring in the
 * {@link JaxRsRouter}, e.g. while identifying the method that should handle
 * the request.<br>
 * Therefor it contains some Restlets that handles this exceptions.
 * </p>
 * <p>
 * Perhaps this class gets again public. Perhaps the special Restlets for
 * handling will be removed, or stay only for 404.
 * </p>
 * 
 * @author Stephan Koops
 */
class ExceptionHandler {

    /**
     * 
     */
    private static final String HEADER_ALLOW = "Allow";

    private final Logger logger;

    ExceptionHandler(Logger logger) {
        this.logger = logger;
    }

    /**
     * @param cpe
     * @throws WebApplicationException
     */
    RequestHandledException convertCookieParamExc(
            ConvertCookieParamException cpe) throws WebApplicationException {
        ResponseBuilder rb = javax.ws.rs.core.Response
                .status(Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        StringWriter stw = new StringWriter();
        cpe.printStackTrace(new PrintWriter(stw));
        rb.entity(stw.toString());
        throw new WebApplicationException(cpe, rb.build());
    }

    /**
     * @param cpe
     * @throws WebApplicationException
     */
    RequestHandledException convertHeaderParamExc(
            ConvertHeaderParamException cpe) throws WebApplicationException {
        ResponseBuilder rb = javax.ws.rs.core.Response
                .status(Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        StringWriter stw = new StringWriter();
        cpe.printStackTrace(new PrintWriter(stw));
        rb.entity(stw.toString());
        throw new WebApplicationException(cpe, rb.build());
    }

    /**
     * @param cpe
     * @throws WebApplicationException
     */
    RequestHandledException convertMatrixParamExc(
            ConvertMatrixParamException cpe) throws WebApplicationException {
        ResponseBuilder rb = javax.ws.rs.core.Response.status(404);
        StringWriter stw = new StringWriter();
        cpe.printStackTrace(new PrintWriter(stw));
        rb.entity(stw.toString());
        throw new WebApplicationException(cpe, rb.build());
    }

    /**
     * @param cpe
     * @throws WebApplicationException
     */
    RequestHandledException convertPathParamExc(ConvertPathParamException cpe)
            throws WebApplicationException {
        ResponseBuilder rb = javax.ws.rs.core.Response.status(404);
        StringWriter stw = new StringWriter();
        cpe.printStackTrace(new PrintWriter(stw));
        rb.entity(stw.toString());
        throw new WebApplicationException(cpe, rb.build());
    }

    /**
     * @param cpe
     * @throws WebApplicationException
     */
    RequestHandledException convertQueryParamExc(ConvertQueryParamException cpe)
            throws WebApplicationException {
        ResponseBuilder rb = javax.ws.rs.core.Response
                .status(Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        StringWriter stw = new StringWriter();
        cpe.printStackTrace(new PrintWriter(stw));
        rb.entity(stw.toString());
        throw new WebApplicationException(cpe, rb.build());
    }

    /**
     * @param cpe
     * @throws WebApplicationException
     */
    RequestHandledException convertRepresentationExc(
            ConvertRepresentationException cre) throws WebApplicationException {
        ResponseBuilder rb = javax.ws.rs.core.Response
                .status(Status.CLIENT_ERROR_BAD_REQUEST.getCode());
        StringWriter stw = new StringWriter();
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
    RequestHandledException instantiateExecption(
            InstantiateException exception, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        logger.log(Level.WARNING, logMessage, exception.getCause());
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
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
    RequestHandledException methodInvokeException(
            MethodInvokeException exception, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        logger.log(Level.WARNING, logMessage, exception.getCause());
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * @param httpMethod
     * @param resourceClass
     * @param u
     */
    void methodNotAllowed(Set<Method> allowedMethods)
            throws WebApplicationException {
        ResponseBuilder rb = javax.ws.rs.core.Response
                .status(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED.getCode());
        rb.header(HEADER_ALLOW, Util.toString(allowedMethods, ", "));
        throw new WebApplicationException(rb.build());
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param exception
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
    RequestHandledException missingAnnotation(
            MissingAnnotationException exception, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        if (exception != null)
            logMessage += ": " + exception.getMessage();
        logger.log(Level.WARNING, logMessage);
        throw new RequestHandledException();
    }

    /**
     * @param response
     * @param mediaType
     * @param paramType
     * @return formally the type of thrown Exception
     * @throws RequestHandledException
     */
    RequestHandledException noMessageBodyReader(CallContext callContext,
            NoMessageBodyReaderException nmbre) throws RequestHandledException {
        Response response = callContext.getResponse();
        MediaType mediaType = nmbre.getMediaType();
        Class<?> paramType = nmbre.getParamType();
        response.setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
        response.setEntity(new StringRepresentation(
                "No MessageBodyReader found to convert from media type "
                        + mediaType + " to " + paramType));
        throw new RequestHandledException();
    }

    RequestHandledException noMessageBodyWriter(Response response,
            SortedMetadata<MediaType> accMediaTypes, Class<?> paramType)
            throws RequestHandledException {
        response.setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
        response.setEntity(new StringRepresentation(
                "No MessageBodyWriter found to convert from java type "
                        + paramType + " to one of the media types "
                        + accMediaTypes));
        throw new RequestHandledException();
    }

    /**
     * see spec, section 3.7.2, item 3(a).4
     */
    void noResourceMethodForAccMediaTypes() throws WebApplicationException {
        // REQUESTED return supported MediaTypes as entity
        throw new WebApplicationException(Status.CLIENT_ERROR_NOT_ACCEPTABLE
                .getCode());
    }

    /**
     * see spec, section 3.8, item 6
     */
    WebApplicationException notAcceptableWhileDetermineMediaType()
            throws WebApplicationException {
        throw new WebApplicationException(Status.CLIENT_ERROR_NOT_ACCEPTABLE
                .getCode());
    }

    /**
     */
    void resourceMethodNotFound() throws WebApplicationException {
        throw new WebApplicationException(Status.CLIENT_ERROR_NOT_FOUND
                .getCode());
    }

    /**
     * see spec, section 3.7.2, item 2 (e)
     */
    void resourceNotFound() throws WebApplicationException {
        throw new WebApplicationException(
                javax.ws.rs.core.Response.Status.NOT_FOUND);
    }

    /**
     * see spec, section 3.7.2, item 1 (d)
     */
    void rootResourceNotFound() throws WebApplicationException {
        throw new WebApplicationException(Status.CLIENT_ERROR_NOT_FOUND
                .getCode());
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
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet {@link Request} and the
     *                Restlet {@link Response}.
     * @param logMessage
     * @param methodName
     * @throws RequestHandledException
     *                 throws this message to exit the method and indicate, that
     *                 the request was handled.
     * @throws RequestHandledException
     */
    RequestHandledException runtimeExecption(RuntimeException exception,
            AbstractMethodWrapper jaxRsMethod, CallContext callContext,
            String logMessage) throws RequestHandledException {
        callContext.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        if (jaxRsMethod != null)
            logMessage = jaxRsMethod + ": " + logMessage;
        logger.log(Level.WARNING, jaxRsMethod + ": " + logMessage, exception);
        exception.printStackTrace();
        throw new RequestHandledException();
    }

    /**
     * see spec, section 3.7.2, item 3 (a) .3
     */
    void unsupportedMediaType() throws WebApplicationException {
        // REQUESTED return allowed Media types
        throw new WebApplicationException(
                Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode());
    }
}