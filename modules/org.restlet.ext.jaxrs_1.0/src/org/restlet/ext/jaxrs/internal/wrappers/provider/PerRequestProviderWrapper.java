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
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList;

/**
 * @author Stephan Koops
 */
class PerRequestProviderWrapper extends AbstractProviderWrapper {

    /**
     * @param providerConstructor
     *                the constructor to use.
     * @param jaxRsProviderClass
     *                class for exception message.
     * @param tlContext
     *                The tread local wrapped call context
     * @param allProviders
     *                all entity providers. <<<<<<< .mine =======
     * @param allResolvers
     *                all available {@link ContextResolver}s. >>>>>>> .r3440
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                the logger to use
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     *                 if the constructor throws an Throwable
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws WebApplicationException
     * @throws IllegalConstrParamTypeException
     *                 if one of the fields or bean setters annotated with &#64;
     *                 {@link Context} has a type that must not be annotated
     *                 with &#64;{@link Context}.
     * @throws IllegalPathParamTypeException
     * @throws MissingConstructorException
     */
    private static Object createInstance(Class<?> jaxRsProviderClass,
            ObjectFactory objectFactory, ThreadLocalizedContext tlContext,
            JaxRsProviders allProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, InvocationTargetException,
            InstantiateException, MissingAnnotationException,
            WebApplicationException, IllegalConstrParamTypeException,
            IllegalPathParamTypeException, MissingConstructorException {
        Util.checkClassConcrete(jaxRsProviderClass, "provider");
        if (objectFactory != null) {
            Object jaxRsProvider;
            jaxRsProvider = objectFactory.getInstance(jaxRsProviderClass);
            if (jaxRsProvider != null) {
                return jaxRsProvider;
            }
        }
        final Constructor<?> providerConstructor = WrapperUtil
                .findJaxRsConstructor(jaxRsProviderClass, "provider");

        ParameterList parameters;
        try {
            parameters = new ParameterList(providerConstructor, tlContext,
                    false, allProviders, extensionBackwardMapping, false,
                    logger, true);
        } catch (IllegalTypeException ite) {
            throw new IllegalConstrParamTypeException(ite);
        }
        try {
            final Object[] args = parameters.get();
            return WrapperUtil.createInstance(providerConstructor, args);
        } catch (final NoMessageBodyReaderException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertRepresentationException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertHeaderParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertPathParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertMatrixParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertQueryParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertCookieParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        }
    }

    private final Class<?> jaxRsProviderClass;

    /**
     * Creates a new wrapper for a Provider and initializes the provider. If the
     * given class is not a provider, an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param jaxRsProviderClass
     *                the JAX-RS provider class.
     * @param objectFactory
     *                The object factory is responsible for the provider
     *                instantiation, if given.
     * @param tlContext
     * @param allProviders
     * @param extensionBackwardMapping
     * @param logger
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws MissingConstructorException
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws WebApplicationException
     * @throws IllegalConstrParamTypeException
     * @throws IllegalPathParamTypeException
     */
    public PerRequestProviderWrapper(final Class<?> jaxRsProviderClass,
            final ObjectFactory objectFactory,
            final ThreadLocalizedContext tlContext,
            final JaxRsProviders allProviders,
            final ExtensionBackwardMapping extensionBackwardMapping,
            final Logger logger) throws IllegalArgumentException,
            InvocationTargetException, MissingConstructorException,
            InstantiateException, MissingAnnotationException,
            WebApplicationException, IllegalConstrParamTypeException,
            IllegalPathParamTypeException {
        super(jaxRsProviderClass);
        this.jaxRsProviderClass = jaxRsProviderClass;
        // LATER I think this could be removed, but check before
        createInstance(jaxRsProviderClass, objectFactory, tlContext,
                allProviders, extensionBackwardMapping, logger);
    }

    @Override
    public final boolean equals(Object otherProvider) {
        if (this == otherProvider) {
            return true;
        }
        if (!(otherProvider instanceof PerRequestProviderWrapper)) {
            return false;
        }
        return this.jaxRsProviderClass
                .equals(((PerRequestProviderWrapper) otherProvider).jaxRsProviderClass);
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getClassName()
     */
    @Override
    public String getClassName() {
        return this.jaxRsProviderClass.getName();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getExcMapperType()
     */
    public Class<?> getExcMapperType() {
        // LATER Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedCtxResolver()
     */
    public ContextResolver getInitializedCtxResolver() {
        // LATER Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedExcMapper()
     */
    public ExceptionMapper<? extends Throwable> getInitializedExcMapper() {
        // LATER Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedReader()
     */
    @Override
    public org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader getInitializedReader() {
        // LATER Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedWriter()
     */
    @Override
    public org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter getInitializedWriter() {
        // LATER Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#hashCode()
     */
    @Override
    public int hashCode() {
        return this.jaxRsProviderClass.hashCode();
    }

    /**
     * @see EntityProvider#initAtAppStartUp(ThreadLocalizedContext, Providers,
     *      ExtensionBackwardMapping)
     */
    public void initAtAppStartUp(ThreadLocalizedContext tlContext,
            Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws InjectException, InvocationTargetException,
            IllegalTypeException {
        // nothing to do here
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#isContextResolver()
     */
    @Override
    public boolean isContextResolver() {
        return Util.doesImplements(jaxRsProviderClass,
                javax.ws.rs.ext.ContextResolver.class); // NICE cache for speed
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#isExceptionMapper()
     */
    @Override
    public boolean isExceptionMapper() {
        return Util.doesImplements(jaxRsProviderClass,
                javax.ws.rs.ext.ExceptionMapper.class); // NICE cache for speed
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#isReader()
     */
    @Override
    public boolean isReader() {
        return Util.doesImplements(jaxRsProviderClass,
                javax.ws.rs.ext.MessageBodyReader.class); // NICE cache for speed
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#isWriter()
     */
    @Override
    public boolean isWriter() {
        return Util.doesImplements(jaxRsProviderClass,
                javax.ws.rs.ext.MessageBodyWriter.class); // NICE cache for speed
    }
}