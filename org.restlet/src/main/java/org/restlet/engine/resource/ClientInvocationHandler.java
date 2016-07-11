/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.resource;

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

// [excludes gwt]
/**
 * Reflection proxy invocation handler created for the {@link ClientResource#wrap(Class)} and related methods.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 *            The annotated resource interface.
 */
public class ClientInvocationHandler<T> implements InvocationHandler {

    /** The annotations of the resource interface. */
    private final List<AnnotationInfo> annotations;

    /** The associated annotation utils. */
    private AnnotationUtils annotationUtils;

    /** The associated client resource. */
    private final ClientResource clientResource;

    /**
     * Constructor.
     * 
     * @param clientResource
     *            The associated client resource.
     * @param resourceInterface
     *            The annotated resource interface.
     */
    public ClientInvocationHandler(ClientResource clientResource,
            Class<? extends T> resourceInterface) {
        this(clientResource, resourceInterface, AnnotationUtils.getInstance());
    }

    /**
     * Constructor.
     * 
     * @param clientResource
     *            The associated client resource.
     * @param resourceInterface
     *            The annotated resource interface.
     * @param annotationUtils
     *            The annotationUtils class.
     */
    public ClientInvocationHandler(ClientResource clientResource,
            Class<? extends T> resourceInterface,
            AnnotationUtils annotationUtils) {
        this.clientResource = clientResource;
        this.annotationUtils = annotationUtils;

        // Introspect the interface for Restlet annotations
        this.annotations = getAnnotationUtils().getAnnotations(
                resourceInterface);
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

    /**
     * Allows for child classes to modify the request.
     */
    protected Request getRequest(Method javaMethod, Object[] args)
            throws Throwable {
        return getClientResource().createRequest();
    }

    /**
     * Effectively invokes a Java method on the given proxy object.
     */
    @SuppressWarnings("rawtypes")
    public Object invoke(Object proxy, java.lang.reflect.Method javaMethod,
            Object[] args) throws Throwable {
        Object result = null;

        if (javaMethod.equals(Object.class.getMethod("toString"))) {
            // Help debug
            result = "ClientProxy for resource: " + clientResource;
        } else if (javaMethod.equals(ClientProxy.class
                .getMethod("getClientResource"))) {
            result = clientResource;
        } else {
            MethodAnnotationInfo annotationInfo = getAnnotationUtils()
                    .getMethodAnnotation(getAnnotations(), javaMethod);

            if (annotationInfo != null) {
                Representation requestEntity = null;

                if ((args != null) && args.length > 0) {
                    // Checks if the user has defined its own callback.
                    for (int i = 0; i < args.length; i++) {
                        Object o = args[i];

                        if (o == null) {
                            requestEntity = null;
                        } else if (Result.class.isAssignableFrom(o.getClass())) {
                            // Asynchronous mode where a callback object is to
                            // be called.

                            // Get the kind of result expected.
                            final Result rCallback = (Result) o;
                            Type[] genericParameterTypes = javaMethod
                                    .getGenericParameterTypes();
                            Type genericParameterType = genericParameterTypes[i];
                            ParameterizedType parameterizedType = (genericParameterType instanceof java.lang.reflect.ParameterizedType) ? (java.lang.reflect.ParameterizedType) genericParameterType
                                    : null;
                            final Class<?> actualType = (parameterizedType != null
                                    && parameterizedType.getActualTypeArguments()[0] instanceof Class<?>) ?
                                    (Class<?>) parameterizedType.getActualTypeArguments()[0] :
                                    null;

                            // Define the callback
                            Uniform callback = new Uniform() {
                                @SuppressWarnings("unchecked")
                                public void handle(Request request,
                                        Response response) {
                                    if (response.getStatus().isError()) {
                                        rCallback
                                                .onFailure(new ResourceException(
                                                        response.getStatus()));
                                    } else {
                                        if (actualType != null) {
                                            Object result = null;
                                            boolean serializationError = false;

                                            try {
                                                result = getClientResource()
                                                        .toObject(
                                                                response.getEntity(),
                                                                actualType);
                                            } catch (Exception e) {
                                                serializationError = true;
                                                rCallback
                                                        .onFailure(new ResourceException(
                                                                e));
                                            }

                                            if (!serializationError) {
                                                rCallback.onSuccess(result);
                                            }
                                        } else {
                                            rCallback.onSuccess(null);
                                        }
                                    }
                                }
                            };

                            getClientResource().setOnResponse(callback);
                        } else {
                            requestEntity = getClientResource()
                                    .toRepresentation(o);
                        }
                    }
                }

                // Clone the prototype request
                Request request = getRequest(javaMethod, args);

                // The Java method was annotated
                request.setMethod(annotationInfo.getRestletMethod());

                // Add the mandatory query parameters
                String query = annotationInfo.getQuery();

                if (query != null) {
                    Form queryParams = new Form(annotationInfo.getQuery());
                    request.getResourceRef().addQueryParameters(queryParams);
                }

                // Set the entity
                request.setEntity(requestEntity);

                // Updates the client preferences if they weren't changed
                if ((request.getClientInfo().getAcceptedCharacterSets()
                        .isEmpty())
                        && (request.getClientInfo().getAcceptedEncodings()
                                .isEmpty())
                        && (request.getClientInfo().getAcceptedLanguages()
                                .isEmpty())
                        && (request.getClientInfo().getAcceptedMediaTypes()
                                .isEmpty())) {
                    List<Variant> responseVariants = annotationInfo
                            .getResponseVariants(getClientResource()
                                    .getMetadataService(), getClientResource()
                                    .getConverterService());

                    if (responseVariants != null) {
                        request.setClientInfo(new ClientInfo(responseVariants));
                    }
                }

                // Effectively handle the call
                Response response = getClientResource().handleOutbound(request);

                // Handle the response, synchronous call
                if (getClientResource().getOnResponse() == null) {
                    if ((response != null) && response.getStatus().isError()) {
                        ThrowableAnnotationInfo tai = getAnnotationUtils()
                                .getThrowableAnnotationInfo(javaMethod,
                                        response.getStatus().getCode());

                        if (tai != null) {
                            Class<?> throwableClazz = tai.getJavaClass();
                            Throwable t = null;

                            if (tai.isSerializable()
                                    && response.isEntityAvailable()) {
                                t = (Throwable) getClientResource().toObject(
                                        response.getEntity(), throwableClazz);
                            } else {
                                try {
                                    t = (Throwable) throwableClazz
                                            .newInstance();
                                } catch (Exception e) {
                                    Context.getCurrentLogger()
                                            .log(Level.FINE,
                                                    "Unable to instantiate the client-side exception using the default constructor.");
                                }

                                if (response.isEntityAvailable()) {
                                    StatusInfo si = getClientResource()
                                            .toObject(response.getEntity(),
                                                    StatusInfo.class);

                                    if (si != null) {
                                        response.setStatus(new Status(si
                                                .getCode(), si
                                                .getReasonPhrase(), si
                                                .getDescription()));
                                    }
                                }
                            }

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
                    } else if (!annotationInfo.getJavaOutputType().equals(
                            void.class)) {
                        result = getClientResource()
                                .toObject(
                                        (response == null ? null
                                                : response.getEntity()),
                                        annotationInfo.getJavaOutputType());
                    }
                }
            }
        }

        return result;
    }
}
