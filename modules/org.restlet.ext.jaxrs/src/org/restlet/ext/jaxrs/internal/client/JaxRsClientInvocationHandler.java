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

package org.restlet.ext.jaxrs.internal.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.ClassUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ClientInfo;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusInfo;
import org.restlet.engine.resource.ClientInvocationHandler;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.engine.resource.ThrowableAnnotationInfo;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.jaxrs.JaxRsClientResource;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalMethodParamTypeException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientProxy;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Result;
import org.restlet.util.Series;

/**
 * Reflection proxy invocation handler created for the {@link JaxRsClientResource#wrap(Class)} and related methods.
 * 
 * @see JaxRsClientResource
 * @see ClientInvocationHandler
 * 
 * @author Shaun Elliott
 * 
 * @param <T>
 *            The annotated resource interface.
 */
public class JaxRsClientInvocationHandler<T> extends ClientInvocationHandler<T> {

    private ClientResource clientResource;

    /**
     * Constructor.
     * 
     * @param clientResource
     *            The client resource.
     * @param resourceInterface
     *            The annotated resource interface.
     */
    public JaxRsClientInvocationHandler(ClientResource clientResource,
            Class<? extends T> resourceInterface) {
        super(clientResource, resourceInterface, JaxRsAnnotationUtils
                .getInstance());

        this.clientResource = clientResource;
    }

    private void addCookieParam(Request request, String representationAsText,
            Annotation annotation) {
        Series<Cookie> cookies = request.getCookies();
        if (cookies == null) {
            cookies = new Series<Cookie>(Cookie.class);
        }

        cookies.add(new Cookie(((CookieParam) annotation).value(),
                representationAsText));

        request.setCookies(cookies);
    }

    private void addFormParam(Form form, String representationAsText,
            Annotation annotation) {
        form.add(new Parameter(((FormParam) annotation).value(),
                representationAsText));
    }

    private void addHeaderParam(Request request, String representationAsText,
            Annotation annotation) {
        Util.getHttpHeaders(request).add(((HeaderParam) annotation).value(),
                representationAsText);
    }

    private void addPathParam(Request request, String representationAsText,
            Annotation annotation) {
        String paramName = ((PathParam) annotation).value();
        String existingPath = Reference.decode(request.getResourceRef()
                .getPath());

        String simplePathParam = String.format("{%s}", paramName);
        if (existingPath.contains(simplePathParam)) {
            existingPath = existingPath.replace(simplePathParam,
                    Reference.encode(representationAsText));
        }

        // TODO - allow regex path params - this code *mostly* works, but not
        // quite
        // String regexPathParam = String.format(".*\\{%s:(.+)\\}.*",
        // paramName);
        // try {
        // if (existingPath.matches(regexPathParam)) {
        // Matcher matcher = Pattern.compile(regexPathParam).matcher(
        // existingPath);
        // String pattern = matcher.group(1);
        //
        // /* I'm not sure how much sense it makes to match on the
        // * textual form of the representation, unless it is a String...
        // */
        // if (representationAsText.matches(pattern)) {
        // existingPath = existingPath.replace(regexPathParam,
        // Reference.encode(representationAsText));
        // }
        // }
        // } catch (PatternSyntaxException pse) {
        // // something is not right in the param definition, skip it
        // pse.printStackTrace();
        // return;
        // }

        request.getResourceRef().setPath(existingPath);
    }

    private void addQueryParam(Request request, String representationAsText,
            Annotation annotation) {
        request.getResourceRef().addQueryParameter(
                new Parameter(((QueryParam) annotation).value(),
                        representationAsText));
    }

    private String getRepresentationAsText(Object value) {
        Class<? extends Object> clazz = value.getClass();
        boolean isPrimitiveOrWrapped = clazz.isPrimitive()
                || ClassUtils.wrapperToPrimitive(clazz) != null;

        if (isPrimitiveOrWrapped || clazz == String.class) {
            return String.valueOf(value);
        }

        String representationAsText = null;

        try {
            Representation representation = clientResource.getApplication()
                    .getConverterService().toRepresentation(value);
            representationAsText = representation.getText();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }

        return representationAsText;
    }

    @Override
    protected Request getRequest(Method javaMethod, Object[] args)
            throws Throwable {
        Request request = super.getRequest(javaMethod, args);

        setRequestPathToAnnotationPath(javaMethod, request);

        return request;
    }

    @SuppressWarnings("rawtypes")
    private void handleJavaMethodParameter(Request request, Object value,
            Type genericParameterType, Annotation[] annotations, Form form) throws IOException {

        if (value == null) {
            // Set the entity
            request.setEntity(null);
        } else if (Result.class.isAssignableFrom(value.getClass())) {
            // Asynchronous mode where a callback object is to
            // be called.

            // Get the kind of result expected.
            final Result rCallback = (Result) value;
            ParameterizedType parameterizedType = (genericParameterType instanceof java.lang.reflect.ParameterizedType) ?
                    (java.lang.reflect.ParameterizedType) genericParameterType
                    : null;
            final Class<?> actualType = (
                    parameterizedType != null
                    && parameterizedType.getActualTypeArguments()[0] instanceof Class<?>) ?
                            (Class<?>) parameterizedType.getActualTypeArguments()[0] :
                            null;

            // Define the callback
            Uniform callback = new Uniform() {
                @SuppressWarnings("unchecked")
                public void handle(Request request, Response response) {
                    if (response.getStatus().isError()) {
                        rCallback.onFailure(new ResourceException(response.getStatus()));
                    } else {
                        if (actualType != null) {
                            Object result = null;
                            try {
                                result = getClientResource().toObject(response.getEntity(), actualType);
                                rCallback.onSuccess(result);
                            } catch (Exception e) {
                                rCallback.onFailure(new ResourceException(e));
                            }
                        } else {
                            rCallback.onSuccess(null);
                        }
                    }
                }
            };

            getClientResource().setOnResponse(callback);
        } else if (annotations != null && annotations.length > 0) {
            String representationAsText = getRepresentationAsText(value);

            if (representationAsText != null) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof HeaderParam) {
                        addHeaderParam(request, representationAsText, annotation);
                    } else if (annotation instanceof QueryParam) {
                        addQueryParam(request, representationAsText, annotation);
                    } else if (annotation instanceof FormParam) {
                        addFormParam(form, representationAsText, annotation);
                    } else if (annotation instanceof CookieParam) {
                        addCookieParam(request, representationAsText, annotation);
                    } else if (annotation instanceof MatrixParam) {
                        // TODO
                    } else if (annotation instanceof PathParam) {
                        addPathParam(request, representationAsText, annotation);
                    }
                }
            }
        } else {
            // Set the entity
            request.setEntity(getClientResource().toRepresentation(value));
        }
    }

    @Override
    public Object invoke(Object proxy, Method javaMethod, Object[] args)
            throws Throwable {
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

            if (annotationInfo == null) {
                return result;
            }

            // Clone the prototype request
            Request request = getRequest(javaMethod, args);

            if ((args != null) && args.length > 0) {
                Form form = new Form();
                Annotation[][] parameterAnnotations = javaMethod.getParameterAnnotations();
                Type[] genericParameterTypes = javaMethod.getGenericParameterTypes();

                // Checks if the user has defined its own callback.
                for (int i = 0; i < args.length; i++) {
                    Object o = args[i];
                    Type genericParameterType = genericParameterTypes[i];

                    handleJavaMethodParameter(request, o, genericParameterType, parameterAnnotations[i], form);
                }
                if (!form.isEmpty()) {
                    request.setEntity(form.getWebRepresentation());
                }
            }

            // The Java method was annotated
            request.setMethod(annotationInfo.getRestletMethod());

            // Add the mandatory query parameters
            String query = annotationInfo.getQuery();

            if (query != null) {
                Form queryParams = new Form(annotationInfo.getQuery());
                request.getResourceRef().addQueryParameters(queryParams);
            }

            // Updates the client preferences if they weren't changed
            if ((request.getClientInfo().getAcceptedCharacterSets().isEmpty())
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

            // Handle the response
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
                                t = (Throwable) throwableClazz.newInstance();
                            } catch (Exception e) {
                                Context.getCurrentLogger()
                                        .log(Level.FINE,
                                                "Unable to instantiate the client-side exception using the default constructor.");
                            }

                            if (response.isEntityAvailable()) {
                                StatusInfo si = getClientResource().toObject(
                                        response.getEntity(), StatusInfo.class);

                                if (si != null) {
                                    response.setStatus(new Status(si.getCode(),
                                            si.getReasonPhrase(), si
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
                    result = getClientResource().toObject(
                            (response == null ? null : response.getEntity()),
                            annotationInfo.getJavaOutputType());
                }
            }
        }

        return result;
    }

    private void setRequestPathToAnnotationPath(Method javaMethod,
            Request request) {
        Path methodPathAnnotation = javaMethod.getAnnotation(Path.class);
        if (methodPathAnnotation != null) {
            String methodPath = methodPathAnnotation.value();
            if (!StringUtils.isNullOrEmpty(methodPath)) {
                String fullUriFromPath = request.getResourceRef().getPath();
                if (fullUriFromPath.endsWith("/")) {
                    if (methodPath.startsWith("/")) {
                        fullUriFromPath += methodPath.substring(1);
                    } else {
                        fullUriFromPath += methodPath;
                    }
                } else {
                    if (methodPath.startsWith("/")) {
                        fullUriFromPath += methodPath;
                    } else {
                        fullUriFromPath += "/" + methodPath;
                    }
                }

                request.getResourceRef().setPath(fullUriFromPath);
            }
        }
    }

}
