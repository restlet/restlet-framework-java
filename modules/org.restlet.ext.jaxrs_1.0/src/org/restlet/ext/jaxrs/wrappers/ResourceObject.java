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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.ext.jaxrs.impl.PathRegExp;
import org.restlet.ext.jaxrs.util.Util;

/**
 * Represents a resource Object
 * 
 * @author Stephan
 * 
 */
public class ResourceObject {
    private ResourceClass resourceClass;

    private Object resourceObject;

    /**
     * Creates a new wrapper for a resource object
     * 
     * @param resourceObject
     *                the resource object
     */
    public ResourceObject(Object resourceObject) {
        this(resourceObject, new ResourceClass(resourceObject.getClass()));
    }

    /**
     * Creates a new wrapper for a resource object
     * 
     * @param resourceObject
     *                the resource object
     * @param resourceClass
     *                the wrapped resource class
     * @param logger
     *                The logger to log unexpected Exceptions.
     */
    public ResourceObject(Object resourceObject, ResourceClass resourceClass) {
        if (resourceObject instanceof ResourceObject)
            throw new IllegalArgumentException(
                    "The given resource class object should not be an instance of the wrapping class ResourceObject");
        this.resourceObject = resourceObject;
        this.resourceClass = resourceClass;
    }

    /**
     * @return Returns the wrapped resource class.
     */
    public ResourceClass getResourceClass() {
        return resourceClass;
    }

    Object getResourceObject() {
        return resourceObject;
    }

    /**
     * Return all resource methods for the given path, ignoring HTTP method,
     * consumed or produced mimes and so on.
     * 
     * @param resourceObject
     *                The resource object
     * @param remainingPath
     *                the path
     * @return The ist of ResourceMethods
     */
    public Collection<ResourceMethod> getMethodsForPath(String remainingPath) {
        // LATER results may be chached, if any method is returned.
        // The 404 case will be called rarely and produce a lot of cached data.
        List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
        Iterable<SubResourceMethod> subResourceMethods = this
                .getResourceClass().getSubResourceMethods();
        for (SubResourceMethod method : subResourceMethods) {
            PathRegExp methodPath = method.getPathRegExp();
            if (Util.isEmptyOrSlash(remainingPath)) {
                if (methodPath.isEmptyOrSlash())
                    resourceMethods.add(method);
            } else {
                if (methodPath.matchesWithEmpty(remainingPath))
                    resourceMethods.add(method);
            }
        }
        return resourceMethods;
    }
}