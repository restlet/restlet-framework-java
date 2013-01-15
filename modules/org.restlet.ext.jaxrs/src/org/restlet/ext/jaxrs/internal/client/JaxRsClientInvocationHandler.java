/**
 * Copyright 2005-2013 Restlet S.A.S.
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

package org.restlet.ext.jaxrs.internal.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.ClassUtils;
import org.restlet.Request;
import org.restlet.data.Cookie;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.resource.ClientInvocationHandler;
import org.restlet.ext.jaxrs.JaxRsClientResource;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalMethodParamTypeException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Reflection proxy invocation handler created for the
 * {@link JaxRsClientResource#wrap(Class)} and related methods.
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

    @Override
    protected Request getRequest(Method javaMethod, Object[] args)
            throws Throwable {
        Request request = super.getRequest(javaMethod, args);

        setRequestPathToAnnotationPath(javaMethod, request);

        setRequestParams(javaMethod, args, request);

        return request;
    }

    private void setRequestParams(Method javaMethod, Object[] args,
            Request request) throws IllegalMethodParamTypeException {

        int argIndex = 0;

        Annotation[][] parameterAnnotations = javaMethod
                .getParameterAnnotations();
        for (Annotation[] annotations : parameterAnnotations) {

            String representationAsText = getRepresentationAsText(args[argIndex]);

            if (representationAsText != null) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof HeaderParam) {
                        addHeaderParam(request, representationAsText,
                                annotation);
                        argIndex++;
                    } else if (annotation instanceof QueryParam) {
                        addQueryParam(request, representationAsText, annotation);
                        argIndex++;
                    } else if (annotation instanceof FormParam) {

                        // TODO
                        argIndex++;
                    } else if (annotation instanceof CookieParam) {
                        addCookieParam(request, representationAsText,
                                annotation);
                        argIndex++;
                    } else if (annotation instanceof MatrixParam) {

                        // TODO
                        argIndex++;
                    } else if (annotation instanceof PathParam) {
                        addPathParam(request, representationAsText, annotation);
                        argIndex++;
                    }
                }
            }
        }

        // TODO - possibly throw an exception if the arg count != processed
        // annotations?
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

    private void addQueryParam(Request request, String representationAsText,
            Annotation annotation) {
        request.getResourceRef().addQueryParameter(
                new Parameter(((QueryParam) annotation).value(),
                        representationAsText));
    }

    private void addHeaderParam(Request request, String representationAsText,
            Annotation annotation) {
        Util.getHttpHeaders(request).add(((HeaderParam) annotation).value(),
                representationAsText);
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

    private void setRequestPathToAnnotationPath(Method javaMethod,
            Request request) {
        Path methodPathAnnotation = javaMethod.getAnnotation(Path.class);
        if (methodPathAnnotation != null) {
            String methodPath = methodPathAnnotation.value();
            if (methodPath != null && methodPath.length() > 0) {
                request.getResourceRef().setPath(
                        request.getResourceRef().getPath() + "/" + methodPath);
            }
        }
    }

}
