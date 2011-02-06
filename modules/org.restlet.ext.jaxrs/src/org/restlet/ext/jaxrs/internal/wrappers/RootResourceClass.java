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

package org.restlet.ext.jaxrs.internal.wrappers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.ws.rs.Encoded;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.params.IntoRrcInjector;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;

/**
 * Instances represents a root resource class, see chapter 3 of JAX-RS
 * specification.
 * 
 * @author Stephan Koops
 */
public abstract class RootResourceClass extends ResourceClass implements
        RrcOrRml {

    /**
     * Checks, if the class is public and so on.
     * 
     * @param jaxRsClass
     *                JAX-RS root resource class or JAX-RS provider.
     * @param typeName
     *                "root resource class" or "provider"
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     */
    private static void checkClassForPathAnnot(Class<?> jaxRsClass,
            String typeName) throws MissingAnnotationException {
        if (!jaxRsClass.isAnnotationPresent(Path.class)) {
            final String msg = "The "
                    + typeName
                    + " "
                    + jaxRsClass.getName()
                    + " is not annotated with @Path. The class will be ignored.";
            throw new MissingAnnotationException(msg);
        }
    }

    protected final Constructor<?> constructor;

    protected final ParameterList constructorParameters;

    /**
     * Injects the necessary values directly into the root resource class.
     */
    protected final IntoRrcInjector injectHelper;

    private final boolean singelton = false;

    /**
     * Creates a wrapper for the given JAX-RS root resource class.
     * 
     * @param jaxRsClass
     *                the root resource class to wrap
     * @param tlContext
     *                the {@link ThreadLocalizedContext} of the
     *                {@link org.restlet.ext.jaxrs.JaxRsRestlet}.
     * @param jaxRsProviders
     *                all entity providers.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                the logger to use.
     * @see ResourceClasses#getRootResourceClass(Class)
     * @throws IllegalArgumentException
     *                 if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     * @throws IllegalPathOnClassException
     * @throws MissingConstructorException
     *                 if no valid constructor could be found
     * @throws IllegalConstrParamTypeException
     * @throws IllegalBeanSetterTypeException
     * @throws IllegalFieldTypeException
     * @throws IllegalPathParamTypeException
     */
    RootResourceClass(Class<?> jaxRsClass, ThreadLocalizedContext tlContext,
            JaxRsProviders jaxRsProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException,
            IllegalPathOnClassException, MissingConstructorException,
            IllegalConstrParamTypeException, IllegalFieldTypeException,
            IllegalBeanSetterTypeException, IllegalPathParamTypeException {
        super(jaxRsClass, jaxRsProviders, tlContext, extensionBackwardMapping,
                logger);
        Util.checkClassConcrete(getJaxRsClass(), "root resource class");
        checkClassForPathAnnot(jaxRsClass, "root resource class");
        this.injectHelper = new IntoRrcInjector(jaxRsClass, tlContext,
                isLeaveEncoded(), jaxRsProviders, extensionBackwardMapping);
        this.constructor = WrapperUtil.findJaxRsConstructor(getJaxRsClass(),
                "root resource class");
        final boolean constructorLeaveEncoded = isLeaveEncoded()
                || constructor.isAnnotationPresent(Encoded.class);
        try {
            this.constructorParameters = new ParameterList(this.constructor,
                    tlContext, constructorLeaveEncoded, jaxRsProviders,
                    extensionBackwardMapping, true, logger, !this.singelton);
        } catch (IllegalConstrParamTypeException e) {
            throw e;
        } catch (IllegalTypeException e) {
            throw new IllegalConstrParamTypeException(e);
        }
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (!(anotherObject instanceof RootResourceClass)) {
            return false;
        }
        final RootResourceClass otherRootResourceClass = (RootResourceClass) anotherObject;
        return this.jaxRsClass.equals(otherRootResourceClass.jaxRsClass);
    }

    /**
     * Creates an or gets the instance of this root resource class.
     * 
     * @param objectFactory
     *                object responsible for instantiating the root resource
     *                class. Optional, thus can be null.
     * @return a wrapped root resource instance
     * @throws InvocationTargetException
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws WebApplicationException
     */
    public abstract ResourceObject getInstance(ObjectFactory objectFactory)
            throws InstantiateException, InvocationTargetException;

    /**
     * @return Returns the regular expression for the URI template
     */
    @Override
    public PathRegExp getPathRegExp() {
        return super.getPathRegExp();
    }
}