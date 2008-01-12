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

package org.restlet.ext.jaxrs.wrappers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.Request;
import org.restlet.ext.jaxrs.MatchingResult;

abstract class AbstractMethodWrapper extends AbstractJaxRsWrapper {

	ResourceClass resourceClass;
	Method javaMethod;

	AbstractMethodWrapper(Method javaMethod, Path path, ResourceClass resourceClass) {
		super(path);
		this.javaMethod = javaMethod;
		this.resourceClass = resourceClass;
	}

	/**
	 * Führt die Methode aus und gibt das erzeugte Objekt aus. Es ist die Antwort-Repräsentation, die zum Client geschickt wird.
	 * 
	 * @param resourceObject
	 * @param matchingResult the matching result containing the variabel alues of the path
	 * @param allTemplParamsEnc Contains all Parameters, that are read from the called URI.
	 * @param restletRequest
	 * @return
	 * @throws Exception of the native invoke of the Java method
	 */
	public Object invoke(ResourceObject resourceObject, MatchingResult matchingResult, MultivaluedMap<String, String> allTemplParamsEnc, Request restletRequest) throws Exception {
		Annotation[][] parameterAnnotationss = javaMethod.getParameterAnnotations();
		Class<?>[] parameterTypes = javaMethod.getParameterTypes();
		Object[] args = getParameterValues(parameterAnnotationss, parameterTypes, matchingResult, restletRequest, allTemplParamsEnc);
		return this.javaMethod.invoke(resourceObject.getResourceObject(), args);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + this.javaMethod.getDeclaringClass().getSimpleName() + "." + this.javaMethod.getName() + "(__)]";
	}
}