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

/**
 * A method of a resource class that is used to handle requests on a
 * sub-resource of the corresponding resource, see section 2.3.1.
 * 
 * The differentiator is the presence or absence of request method designator:
 * Present Such methods, known as sub-resource methods, are treated like a
 * normal resource method (see section 2.2) except the method is only invoked
 * for request URIs that match a URI template created by concatenating the URI
 * template of the resource class with the URI template of the method.
 * 
 * @author Stephan Koops
 * 
 */
public class SubResourceMethod extends ResourceMethod {

    /**
     * Creates a new Wrapper for a SubResourceMethod
     * 
     * @param javaMethod
     * @param path
     * @param resourceClass
     * @param httpMethod
     */
    public SubResourceMethod(Method javaMethod, Path path,
            ResourceClass resourceClass, org.restlet.data.Method httpMethod) {
        super(javaMethod, path, resourceClass, httpMethod);
    }
}