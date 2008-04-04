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
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * The ExceptionMappers handles the exceptions thrown by JAX-RS classes.
 * 
 * @author Stephan Koops
 */
public class ExceptionMappers {

    private static final ServerErrorExcMapper SERVER_ERROR_EXC_MAPPER = new ServerErrorExcMapper();

    static class ServerErrorExcMapper implements ExceptionMapper<Throwable> {

        private static final Logger logger = Logger
                .getLogger("DefaultExceptionMapper");

        /**
         * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
         */
        public Response toResponse(Throwable exception) {
            // TODO this is a hack
            if (exception instanceof WebApplicationException)
                return ((WebApplicationException) exception).getResponse();
            String msg = "A JAX-RS class throws an unhandled "
                    + exception.getClass().getName();
            logger.log(Level.WARNING, msg, exception);
            return Response.serverError().build();
        }
    }

    static class WebAppExcMapper implements
            ExceptionMapper<WebApplicationException> {
        /**
         * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
         */
        public Response toResponse(WebApplicationException wae) {
            return wae.getResponse();
        }
    }

    private static final Logger logger = Logger.getLogger("ExceptionsMapper");

    private final Map<Class<? extends Throwable>, ExceptionMapper<? extends Throwable>> excMappers = new HashMap<Class<? extends Throwable>, ExceptionMapper<? extends Throwable>>();

    /**
     * Creates a new ExceptionMapper
     */
    public ExceptionMappers() {
        init();
    }

    /**
     * Adds the given {@link ExceptionMapper} to this ExceptionMappers.
     * 
     * @param excMapper
     * @throws NullPointerException
     *                 if null is given
     */
    public void add(ExceptionMapper<? extends Throwable> excMapper) {
        // REQUESTED how to get class of ExceptionMapper?
        Type[] gis = excMapper.getClass().getGenericInterfaces();
        for (Type gi : gis) {
            if (gi instanceof ParameterizedType) {
                ParameterizedType ifpt = (ParameterizedType) gi;
                if (ifpt.getRawType().equals(ExceptionMapper.class)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Throwable> excClass = (Class) ifpt
                            .getActualTypeArguments()[0];
                    excMappers.put(excClass, excMapper);
                }
            }
        }
    }

    /**
     * Adds the given {@link Provider} to this ExceptionMappers.
     * 
     * @param exceptionMapper
     * @throws NullPointerException
     *                 if <code>null</code> is given
     */
    public void add(Provider<?> exceptionMapper) {
        add(exceptionMapper.getExcMapper());
    }

    /**
     * converts the cause of the given InvocationTargetException to a
     * {@link Response}, if an {@link ExceptionMapper} could be found.<br>
     * Otherwise this method returns an Response with an internal server error.
     * 
     * @param ite
     *                the {@link InvocationTargetException} wrapping the thrown
     *                exception
     * @return the created Response
     * @throws NullPointerException
     *                 if <code>null</code> is given
     */
    public Response convert(InvocationTargetException ite) {
        Throwable cause = ite.getCause();
        ExceptionMapper<Throwable> mapper = getMapper(cause.getClass());
        if (mapper == null) {
            String entity = "No ExceptionMapper was found, but must be found";
            return Response.serverError().entity(entity).build();
        }
        Response response;
        try {
            response = mapper.toResponse(cause);
            // REQUESTED add to javadoc ExceptionMapper.toResponse, that this
            // method should not throw an WebAppExc.
        } catch (RuntimeException e) {
            String message = "The ExceptionMapper throws an Exception";
            logger.log(Level.WARNING, message, e);
            return Response.serverError().entity(message).build();
        }
        if (response == null) {
            String entity = "The ExceptionMapper returned null";
            return Response.serverError().entity(entity).build();
        }
        return response;
    }

    /**
     * @param causeClass
     * @return the ExceptionMapper for the given Throwable class. Never returns
     *         null.
     */
    @SuppressWarnings("unchecked")
    private ExceptionMapper<Throwable> getMapper(
            Class<? extends Throwable> causeClass) {
        if (causeClass == null)
            return SERVER_ERROR_EXC_MAPPER;
        ExceptionMapper<Throwable> mapper = (ExceptionMapper) this.excMappers
                .get(causeClass);
        if (mapper == null) {
            Class superclass = causeClass.getSuperclass();
            mapper = getMapper(superclass);
            // this.excMappers.put(superclass, mapper);
        }
        return mapper;
    }

    void init() {
        this.add(SERVER_ERROR_EXC_MAPPER);
    }

    void reset() {
        this.excMappers.clear();
        init();
    }
}