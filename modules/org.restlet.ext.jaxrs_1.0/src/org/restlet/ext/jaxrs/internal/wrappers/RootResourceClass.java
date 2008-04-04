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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.Encoded;
import javax.ws.rs.Path;

import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ContextResolver;
import org.restlet.ext.jaxrs.internal.wrappers.provider.EntityProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader;

/**
 * Instances represents a root resource class, see chapter 3 of JAX-RS
 * specification.
 * 
 * @author Stephan Koops
 */
public class RootResourceClass extends ResourceClass {

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
            String msg = "The "
                    + typeName
                    + " "
                    + jaxRsClass.getName()
                    + " is not annotated with @Path. The class will be ignored.";
            throw new MissingAnnotationException(msg);
        }
    }

    private Constructor<?> constructor;

    /**
     * is true, if the constructor (or the root resource class) is annotated
     * with &#64;Path. Is available after constructor was running.
     */
    private boolean constructorLeaveEncoded;

    private IntoRrcInjector injectHelper;

    /**
     * Creates a wrapper for the given JAX-RS root resource class.
     * 
     * @param jaxRsClass
     *                the root resource class to wrap
     * @param logger
     *                the logger to use.
     * @see WrapperFactory#getRootResourceClass(Class)
     * @throws IllegalArgumentException
     *                 if the class is not a valid root resource class.
     * @throws MissingAnnotationException
     *                 if the class is not annotated with &#64;Path.
     * @throws IllegalPathOnClassException
     * @throws MissingConstructorException
     *                 if no valid constructor could be found
     */
    RootResourceClass(Class<?> jaxRsClass, Logger logger)
            throws IllegalArgumentException, MissingAnnotationException,
            IllegalPathOnClassException, MissingConstructorException {
        super(jaxRsClass, logger, logger);
        Util.checkClassConcrete(getJaxRsClass(), "root resource class");
        checkClassForPathAnnot(jaxRsClass, "root resource class");
        this.injectHelper = new IntoRrcInjector(jaxRsClass, isLeaveEncoded());
        this.constructor = WrapperUtil.findJaxRsConstructor(getJaxRsClass(),
                "root resource class");
        this.constructorLeaveEncoded = this.isLeaveEncoded()
                || constructor.isAnnotationPresent(Encoded.class);
    }

    /**
     * Creates an instance of the root resource class.
     * 
     * @param callContext
     *                Contains the encoded template Parameters, that are read
     *                from the called URI, the Restlet
     *                {@link org.restlet.data.Request} and the Restlet
     *                {@link org.restlet.data.Response}.
     * @param allResolvers
     *                all available wrapped
     *                {@link javax.ws.rs.ext.ContextResolver}s.
     * @param entityProviders
     *                The available {@link MessageBodyReader}s in the
     *                {@link org.restlet.ext.jaxrs.JaxRsRouter}.
     * @param logger
     *                The logger to use
     * @return
     * @throws InvocationTargetException
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws NoMessageBodyReaderException
     * @throws ConvertCookieParamException
     * @throws ConvertQueryParamException
     * @throws ConvertMatrixParamException
     * @throws ConvertPathParamException
     * @throws ConvertHeaderParamException
     * @throws ConvertRepresentationException
     */
    public ResourceObject createInstance(CallContext callContext,
            Collection<ContextResolver<?>> allResolvers,
            EntityProviders entityProviders, Logger logger)
            throws MissingAnnotationException, InstantiateException,
            NoMessageBodyReaderException, InvocationTargetException,
            ConvertRepresentationException, ConvertHeaderParamException,
            ConvertPathParamException, ConvertMatrixParamException,
            ConvertQueryParamException, ConvertCookieParamException {
        Constructor<?> constructor = this.constructor;
        Object instance;
        try {
            instance = WrapperUtil.createInstance(constructor,
                    false, constructorLeaveEncoded, callContext, entityProviders, logger);
        } catch (IllegalAnnotationException iae) {
            // should not be possible here
            throw new InstantiateException(iae);
        }
        ResourceObject rootResourceObject = new ResourceObject(instance, this);
        try {
            this.injectHelper.inject(rootResourceObject, callContext,
                    allResolvers, entityProviders);
        } catch (InjectException e) {
            throw new InstantiateException(e);
        }
        return rootResourceObject;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject)
            return true;
        if (!(anotherObject instanceof RootResourceClass))
            return false;
        RootResourceClass otherRootResourceClass = (RootResourceClass) anotherObject;
        return this.jaxRsClass.equals(otherRootResourceClass.jaxRsClass);
    }

    /**
     * @return Returns the regular expression for the URI template
     */
    @Override
    public PathRegExp getPathRegExp() {
        return super.getPathRegExp();
    }
}