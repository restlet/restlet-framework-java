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

package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;

/**
 * @author Stephan Koops
 */
public class SingletonRootResourceClass extends RootResourceClass {

    private final ResourceObject rootResObject;

    /**
     * @param rootResourceObject
     * @param tlContext
     * @param jaxRsProviders
     * @param extensionBackwardMapping
     * @param logger
     * @throws IllegalArgumentException
     * @throws MissingAnnotationException
     * @throws IllegalPathOnClassException
     * @throws MissingConstructorException
     * @throws IllegalConstrParamTypeException
     * @throws IllegalFieldTypeException
     * @throws IllegalBeanSetterTypeException
     * @throws IllegalPathParamTypeException
     * @throws InvocationTargetException
     *             if an exception occurs while the injecting of dependencies.
     * @throws InjectException
     */
    SingletonRootResourceClass(Object rootResourceObject,
            ThreadLocalizedContext tlContext, JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException,
            IllegalPathOnClassException, MissingConstructorException,
            IllegalConstrParamTypeException, IllegalFieldTypeException,
            IllegalBeanSetterTypeException, IllegalPathParamTypeException,
            InvocationTargetException, InjectException {
        super(rootResourceObject.getClass(), tlContext, jaxRsProviders,
                extensionBackwardMapping, logger);
        this.injectHelper.injectInto(rootResourceObject, true);
        this.rootResObject = new ResourceObject(rootResourceObject, this);
    }

    /**
     * Creates an or gets the instance of this root resource class.
     * 
     * @param objectFactory
     *            object responsible for instantiating the root resource class.
     *            Optional, thus can be null.
     * @return the wrapped root resource class instance
     * @throws InvocationTargetException
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws WebApplicationException
     */
    @Override
    public ResourceObject getInstance(ObjectFactory objectFactory) {
        return this.rootResObject;
    }
}
