/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.resource;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientProxy;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Result;

/**
 * Reflection proxy invocation handler created for the {@link ClientResource#wrap(Class)} and
 * related methods.
 *
 * @author Jerome Louvel
 * @param <T> The annotated resource interface.
 */
public class ClientInvocationHandler<T> implements InvocationHandler {

    /** The annotations of the resource interface. */
    private final List<AnnotationInfo> annotations;

    /** The associated annotation utils. */
    private final AnnotationUtils annotationUtils;

    /** The associated client resource. */
    private final ClientResource clientResource;

    /**
     * Constructor.
     *
     * @param clientResource The associated client resource.
     * @param resourceInterface The annotated resource interface.
     */
    public ClientInvocationHandler(
            ClientResource clientResource, Class<? extends T> resourceInterface) {
        this(clientResource, resourceInterface, AnnotationUtils.getInstance());
    }

    /**
     * Constructor.
     *
     * @param clientResource The associated client resource.
     * @param resourceInterface The annotated resource interface.
     * @param annotationUtils The annotationUtils class.
     */
    public ClientInvocationHandler(
            ClientResource clientResource,
            Class<? extends T> resourceInterface,
            AnnotationUtils annotationUtils) {
        this.clientResource = clientResource;
        this.annotationUtils = annotationUtils;

        // Introspect the interface for Restlet annotations
        this.annotations = getAnnotationUtils().getAnnotations(resourceInterface);
    }

    /**
     * Returns the annotations of the resource interface.
     *
     * @return The annotations of the resource interface.
     */
    public List<AnnotationInfo> getAnnotations() {
        return annotations;
    }

    /**
     * Returns the associated annotation utils.
     *
     * @return The associated annotation utils.
     */
    public AnnotationUtils getAnnotationUtils() {
        return annotationUtils;
    }

    /**
     * Returns the associated client resource.
     *
     * @return The associated client resource.
     */
    public ClientResource getClientResource() {
        return clientResource;
    }

    /** Allows for child classes to modify the request. */
    protected Request getRequest(Method javaMethod, Object[] args) throws Throwable {
        return getClientResource().createRequest();
    }

    /** Effectively invokes a Java method on the given proxy object. */
    public Object invoke(Object proxy, java.lang.reflect.Method javaMethod, Object[] args)
            throws Throwable {
        if (javaMethod.equals(Object.class.getMethod("toString"))) {
            return "ClientProxy for resource: " + clientResource;
        }
        if (javaMethod.equals(ClientProxy.class.getMethod("getClientResource"))) {
            return clientResource;
        }

        MethodAnnotationInfo annotationInfo =
                getAnnotationUtils().getMethodAnnotation(getAnnotations(), javaMethod);

        if (annotationInfo == null) {
            return null;
        }

        Representation requestEntity = processArgs(javaMethod, args);
        Response response = buildAndSendRequest(javaMethod, annotationInfo, requestEntity, args);
        return handleSyncResponse(javaMethod, annotationInfo, response);
    }

    /** Iterates the args, sets up the async callback, or extracts the request entity. */
    @SuppressWarnings("rawtypes")
    private Representation processArgs(java.lang.reflect.Method javaMethod, Object[] args)
            throws IOException {
        if (args == null || args.length == 0) {
            return null;
        }

        Representation requestEntity = null;
        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            if (o == null) {
                requestEntity = null;
            } else if (Result.class.isAssignableFrom(o.getClass())) {
                Result rCallback = (Result) o;
                Class<?> actualType = resolveActualType(javaMethod, i);
                getClientResource().setOnResponse(createAsyncCallback(rCallback, actualType));
            } else {
                requestEntity = getClientResource().toRepresentation(o);
            }
        }
        return requestEntity;
    }

    private Class<?> resolveActualType(java.lang.reflect.Method javaMethod, int argIndex) {
        Type genericParameterType = javaMethod.getGenericParameterTypes()[argIndex];
        if (genericParameterType instanceof ParameterizedType parameterizedType) {
            Type firstArg = parameterizedType.getActualTypeArguments()[0];
            if (firstArg instanceof Class<?>) {
                return (Class<?>) firstArg;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Uniform createAsyncCallback(Result rCallback, Class<?> actualType) {
        return (request, response) -> {
            if (response.getStatus().isError()) {
                rCallback.onFailure(new ResourceException(response.getStatus()));
            } else if (actualType == null) {
                rCallback.onSuccess(null);
            } else {
                try {
                    Object callbackResult =
                            getClientResource().toObject(response.getEntity(), actualType);
                    rCallback.onSuccess(callbackResult);
                } catch (Exception e) {
                    rCallback.onFailure(new ResourceException(e));
                }
            }
        };
    }

    private Response buildAndSendRequest(
            java.lang.reflect.Method javaMethod,
            MethodAnnotationInfo annotationInfo,
            Representation requestEntity,
            Object[] args)
            throws Throwable {
        Request request = getRequest(javaMethod, args);
        request.setMethod(annotationInfo.getRestletMethod());

        String query = annotationInfo.getQuery();
        if (query != null) {
            request.getResourceRef().addQueryParameters(new Form(query));
        }

        request.setEntity(requestEntity);

        ClientInfo clientInfo = request.getClientInfo();
        if (clientInfo.getAcceptedCharacterSets().isEmpty()
                && clientInfo.getAcceptedEncodings().isEmpty()
                && clientInfo.getAcceptedLanguages().isEmpty()
                && clientInfo.getAcceptedMediaTypes().isEmpty()) {
            List<Variant> responseVariants =
                    annotationInfo.getResponseVariants(
                            getClientResource().getMetadataService(),
                            getClientResource().getConverterService());
            if (responseVariants != null) {
                request.setClientInfo(new ClientInfo(responseVariants));
            }
        }

        return getClientResource().handleOutbound(request);
    }

    /**
     * handles the error response or convert the response's representation to the return type of the
     * java method.
     */
    private Object handleSyncResponse(
            java.lang.reflect.Method javaMethod,
            MethodAnnotationInfo annotationInfo,
            Response response)
            throws Throwable {
        if (getClientResource().getOnResponse() != null) {
            // the callback will handle the async response
            return null;
        }

        final Object result;
        if (response != null && response.getStatus().isError()) {
            handleErrorResponse(javaMethod, response);
            result = null;
        } else if (annotationInfo.getJavaOutputType().equals(void.class)) {
            result = null;
        } else {
            result =
                    getClientResource()
                            .toObject(
                                    response == null ? null : response.getEntity(),
                                    annotationInfo.getJavaOutputType());
        }
        return result;
    }

    /**
     * Handles the error response or throw an exception if the java method has a
     * ThrowableAnnotationInfo annotation.
     */
    private void handleErrorResponse(java.lang.reflect.Method javaMethod, Response response)
            throws Throwable {
        ThrowableAnnotationInfo tai =
                getAnnotationUtils()
                        .getThrowableAnnotationInfo(javaMethod, response.getStatus().getCode());

        if (tai != null) {
            Throwable t = resolveThrowable(tai, response);
            if (t != null) {
                throw t;
            }
            // TODO cf issues 1004 and 1018.
            // this code has been commented as the automatic
            // deserialization is problematic. We may rethink a
            // way to recover the status info.
            // } else if (response.isEntityAvailable()) {
            // StatusInfo si = getClientResource().toObject(
            // response.getEntity(), StatusInfo.class);
            //
            // if (si != null) {
            // response.setStatus(new Status(si.getCode(), si
            // .getReasonPhrase(), si.getDescription()));
            // }
        }

        getClientResource().doError(response.getStatus());
    }

    private Throwable resolveThrowable(ThrowableAnnotationInfo tai, Response response) {
        Class<?> throwableClazz = tai.getJavaClass();
        if (tai.isSerializable() && response.isEntityAvailable()) {
            return (Throwable) getClientResource().toObject(response.getEntity(), throwableClazz);
        }
        Throwable t = instantiateThrowable(throwableClazz);
        if (t != null && response.isEntityAvailable()) {
            StatusInfo si = getClientResource().toObject(response.getEntity(), StatusInfo.class);
            if (si != null) {
                response.setStatus(
                        new Status(si.getCode(), si.getReasonPhrase(), si.getDescription()));
            }
        }
        return t;
    }

    private Throwable instantiateThrowable(Class<?> throwableClazz) {
        try {
            return (Throwable) throwableClazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Context.getCurrentLogger()
                    .log(
                            Level.FINE,
                            "Unable to instantiate the client-side exception using the default constructor.");
            return null;
        }
    }
}
