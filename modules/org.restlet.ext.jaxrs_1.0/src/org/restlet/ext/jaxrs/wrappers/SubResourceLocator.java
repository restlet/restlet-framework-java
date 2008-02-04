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

import java.lang.reflect.Method;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.jaxrs.Authenticator;
import org.restlet.ext.jaxrs.impl.MatchingResult;

/**
 * A method of a resource class that is used to locate sub-resources of the
 * corresponding resource, see section 2.3.1.
 * 
 * @author Stephan Koops
 * 
 */
public class SubResourceLocator extends AbstractMethodWrapper implements
        SubResourceMethodOrLocator {
    /**
     * Creates a new wrapper for the given sub resource locator.
     * 
     * @param javaMethod
     *                The Java method wich creats the sub resource
     * @param path
     *                the path on the method
     * @param resourceClass
     *                the wrapped resource class.
     */
    public SubResourceLocator(Method javaMethod, Path path,
            ResourceClass resourceClass) {
        super(javaMethod, path, resourceClass);
    }

    /**
     * Creates a sub resource
     * 
     * @param resourceObject
     *                the wrapped resource object.
     * @param matchingResult
     *                the matching result, containing the path variables
     * @param allTemplParamsEnc
     *                Contains all Parameters, that are read from the called
     *                URI.
     * @param restletRequest the Restlet request
     * @param restletResponse the Restlet response
     * @param authenticator The logged in User
     * @return Returns the wrapped sub resource object.
     * @throws Exception
     */
    public ResourceObject createSubResource(ResourceObject resourceObject,
            MatchingResult matchingResult,
            MultivaluedMap<String, String> allTemplParamsEnc,
            Request restletRequest, Response restletResponse, Authenticator authenticator) throws Exception {
        Object[] args;
        Class<?>[] parameterTypes = this.javaMethod.getParameterTypes();
        if (parameterTypes.length == 0)
            args = new Object[0];
        else
            args = getParameterValues(javaMethod.getParameterAnnotations(),
                    parameterTypes, matchingResult, restletRequest,
                    restletResponse, allTemplParamsEnc, authenticator);
        Object subResourceObject = javaMethod.invoke(resourceObject
                .getResourceObject(), args);
        return new ResourceObject(subResourceObject);
    }
}