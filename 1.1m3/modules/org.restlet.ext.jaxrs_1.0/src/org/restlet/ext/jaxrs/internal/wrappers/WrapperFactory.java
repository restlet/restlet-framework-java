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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;

/**
 * A WrapperFactory creates and caches some of the wrapper objects.
 * 
 * @author Stephan Koops
 */
public class WrapperFactory {

    private Logger logger;

    private final Map<Class<?>, ResourceClass> resourceClasses = new HashMap<Class<?>, ResourceClass>();

    /**
     * @param logger
     *                the to log warnings and so on
     */
    public WrapperFactory(Logger logger) {
        this.logger = logger;
    }

    /**
     * Creates a new JAX-RS resource class wrapper.
     * 
     * @param jaxRsResourceClass
     * @return
     */
    ResourceClass getResourceClass(Class<?> jaxRsResourceClass) {
        ResourceClass rc = resourceClasses.get(jaxRsResourceClass);
        if (rc == null) {
            rc = new ResourceClass(jaxRsResourceClass, logger);
            resourceClasses.put(jaxRsResourceClass, rc);
        }
        return rc;
    }

    /**
     * Creates a new JAX-RS root resource class wrapper.
     * 
     * @param jaxRsRootResourceClass
     * @return the wrapped root resource class.
     * @throws IllegalArgumentException
     *                 if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     * @throws IllegalPathOnClassException
     */
    public RootResourceClass getRootResourceClass(
            Class<?> jaxRsRootResourceClass) throws IllegalArgumentException,
            MissingAnnotationException, IllegalPathOnClassException {
        return new RootResourceClass(jaxRsRootResourceClass, logger);
    }
}