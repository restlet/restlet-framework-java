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

package org.restlet.engine.resource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ClientInfo;
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
 * @param <T> The annotated resource interface.
 */
public class ClientInvocationHandler<T> implements InvocationHandler {

    /** The annotations of the resource interface. */
    private final List<AnnotationInfo> annotations;

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
        this.clientResource = clientResource;

        // Introspect the interface for Restlet annotations
        this.annotations = AnnotationUtils.getAnnotations(resourceInterface);
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
            AnnotationInfo annotation = AnnotationUtils.getAnnotation(
                    annotations, javaMethod);

            if (annotation != null) {
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
                                            rCallback
                                                    .onSuccess(getClientResource()
                                                            .toObject(
                                                                    response.getEntity(),
                                                                    actualType
                                                                            .getClass()));
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
                Request request = getClientResource().createRequest(
                        getClientResource().getRequest());

                // The Java method was annotated
                request.setMethod(annotation.getRestletMethod());

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
                    List<Variant> responseVariants = annotation
                            .getResponseVariants(getClientResource()
                                    .getMetadataService(), getClientResource()
                                    .getConverterService());

                    if (responseVariants != null) {
                        request.setClientInfo(new ClientInfo(responseVariants));
                    }
                }

                // Effectively handle the call
                Response response = getClientResource().handle(request);

                // Handle the response
                if (isSynchronous) {
                    if (response.getStatus().isError()) {
                        getClientResource().doError(response.getStatus());
                    }

                    if (!annotation.getJavaOutputType().equals(void.class)) {
                        result = getClientResource()
                                .toObject(
                                        (response == null ? null
                                                : response.getEntity()),
                                        annotation.getJavaOutputType());
                    }
                }
            }
        }

        return result;
    }

}
