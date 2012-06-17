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

import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.engine.resource.ClientInvocationHandler;
import org.restlet.ext.jaxrs.JaxRsClientResource;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalMethodParamTypeException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

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

		setRequestParams(javaMethod, args, request);

		setRequestPathToAnnotationPath(javaMethod, request);

		return request;
	}

	private void setRequestParams(Method javaMethod, Object[] args,
			Request request) throws IllegalMethodParamTypeException {

		int argIndex = 0;

		Annotation[][] parameterAnnotations = javaMethod
				.getParameterAnnotations();
		for (Annotation[] annotations : parameterAnnotations) {

			String representationAsText = getRepresentationAsText(argIndex,
					args);

			for (Annotation annotation : annotations) {
				if (annotation instanceof HeaderParam
						&& representationAsText != null) {
					Util.getHttpHeaders(request).add(
							((HeaderParam) annotation).value(),
							representationAsText);
					argIndex++;
				} else if (annotation instanceof QueryParam) {
					request.getResourceRef().addQueryParameter(
							new Parameter(((QueryParam) annotation).value(),
									representationAsText));
					argIndex++;
				} else if (annotation instanceof MatrixParam
						&& representationAsText != null) {
					// TODO
					argIndex++;
				} else if (annotation instanceof CookieParam
						&& representationAsText != null) {
					// TODO
					argIndex++;
				} else if (annotation instanceof FormParam
						&& representationAsText != null) {
					// TODO
					argIndex++;
				} else if (annotation instanceof QueryParam
						&& representationAsText != null) {
					// TODO
					argIndex++;
				} else if (annotation instanceof PathParam
						&& representationAsText != null) {
					// TODO
					argIndex++;
				}
			}
		}

		// TODO - possibly throw an exception if the arg count != processed annotations?
	}

	private String getRepresentationAsText(int argIndex, Object[] args) {
		String representationAsText = null;
		Representation representation = clientResource.getApplication()
				.getConverterService().toRepresentation(args[argIndex]);
		try {
			representationAsText = representation.getText();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
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
