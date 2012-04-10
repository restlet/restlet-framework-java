/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.resource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientProxy;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Result;

// [excludes gwt]
/**
 * Reflection proxy invocation handler created for the
 * {@link ClientResource#wrap(Class)} and related methods.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 *            The annotated resource interface.
 */
public class ClientInvocationHandler<T> implements InvocationHandler {

    /** The annotations of the resource interface. */
    private final List<AnnotationInfo> annotations;

    /** The associated client resource. */
    private final ClientResource clientResource;

    /** The associated annotation utils. */
    private AnnotationUtils annotationUtils;

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
        this.annotations = this.annotationUtils
                .getAnnotations(resourceInterface);
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
     * Returns the associated client resource.
     * 
     * @return The associated client resource.
     */
    public ClientResource getClientResource() {
        return clientResource;
    }

    /**
     * Returns a new instance of {@link Request} according to the given Java
     * Method.
     * 
     * @param javaMethod
     *            The Java method.
     * @return A new instance of {@link Request}.
     */
    protected Request getRequest(Method javaMethod) {
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
            AnnotationInfo annotationInfo = annotationUtils.getAnnotation(
                    annotations, javaMethod);

            if (annotationInfo != null) {
                Representation requestEntity = null;
                boolean isSynchronous = true;

                if ((args != null) && args.length > 0) {
                    // Checks if the user has defined its own
                    // callback.
                    for (int i = 0; i < args.length; i++) {
                        Object o = args[i];

                        if (o == null) {
                            requestEntity = null;
                        } else if (Result.class.isAssignableFrom(o.getClass())) {
                            // Asynchronous mode where a callback
                            // object is to be called.
                            isSynchronous = false;

                            // Get the kind of result expected.
                            final Result rCallback = (Result) o;
                            Type[] genericParameterTypes = javaMethod
                                    .getGenericParameterTypes();
                            Type genericParameterType = genericParameterTypes[i];
                            ParameterizedType parameterizedType = (genericParameterType instanceof java.lang.reflect.ParameterizedType) ? (java.lang.reflect.ParameterizedType) genericParameterType
                                    : null;
                            final Class<?> actualType = (parameterizedType
                                    .getActualTypeArguments()[0] instanceof Class<?>) ? (Class<?>) parameterizedType
                                    .getActualTypeArguments()[0] : null;

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
                                    .toRepresentation(args[i], null);
                        }
                    }
                }

                // Clone the prototype request
                Request request = getRequest(javaMethod);

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
                if ((request.getClientInfo().getAcceptedCharacterSets().size() == 0)
                        && (request.getClientInfo().getAcceptedEncodings()
                                .size() == 0)
                        && (request.getClientInfo().getAcceptedLanguages()
                                .size() == 0)
                        && (request.getClientInfo().getAcceptedMediaTypes()
                                .size() == 0)) {
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

                // Handle the response
                if (isSynchronous) {
                    if (response.getStatus().isError()) {
                        getClientResource().doError(response.getStatus());
                    }

                    if (!annotationInfo.getJavaOutputType().equals(void.class)) {
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
