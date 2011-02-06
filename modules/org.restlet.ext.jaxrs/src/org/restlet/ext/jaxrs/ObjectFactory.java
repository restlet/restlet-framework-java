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

package org.restlet.ext.jaxrs;

/**
 * <p>
 * Implement this interface to instantiate JAX-RS root resource classes and
 * providers yourself and register it by
 * {@link JaxRsApplication#setObjectFactory(ObjectFactory)}.
 * </p>
 * <p>
 * When using a ObjectFactory, no JAX-RS constructor dependency injection will
 * be performed, but instance variable and bean setter injection will still be
 * done.
 * </p>
 * 
 * @author Bruno Dumon
 * @see JaxRsApplication#setObjectFactory(ObjectFactory)
 * @see JaxRsRestlet#setObjectFactory(ObjectFactory)
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
    // LATER if a resource class is a singelton, it must be ensured, that it
    // has no @*Param on fields and perhaps bean setters.
}