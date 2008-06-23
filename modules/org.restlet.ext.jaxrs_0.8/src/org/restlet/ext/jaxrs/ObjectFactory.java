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
package org.restlet.ext.jaxrs;

import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;

/**
 * <p>
 * Implement this interface to instantiate JAX-RS root resource classes and
 * providers yourself and register it by
 * {@link JaxRsApplication#setObjectFactory(ObjectFactory)}.
 * </p>
 * 
 * <p>
 * When using a ObjectFactory, no JAX-RS constructor dependency injection will
 * be performed, but instance variable and bean setter injection will still be
 * done.
 * </p>
 * 
 * @author Bruno Dumon
 * @see JaxRsApplication#setObjectFactory(ObjectFactory)
 * @see JaxRsRouter#setObjectFactory(ObjectFactory)
 */
public interface ObjectFactory {
    /**
     * Creates an instance of the given class.<br>
     * If the concrete instance could not instantiate the given class, it could
     * return null. Than the constructor specified by the JAX-RS specification
     * (section 4.2) is used.
     * 
     * @param <T>
     * @param jaxRsClass
     *                the root resource class or provider class.
     * @return The created instance.
     * @throws InstantiateException
     */
    public <T> T getInstance(Class<T> jaxRsClass) throws InstantiateException;
    // LATER move InstantiateException from package internal to another package
    // if more exceptions should be used public, perhaps own package for Except.
    
    // LATER if a resource class is a singelton, it must be ensured, that it
    // has no @*Param on fields and perhaps bean setters.
}