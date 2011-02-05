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

package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
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
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.exceptions.ProviderNotInitializableException;
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
    private Object createInstance() throws IllegalArgumentException,
            InvocationTargetException, InstantiateException,
            MissingAnnotationException, WebApplicationException,
            IllegalConstrParamTypeException, IllegalPathParamTypeException,
            MissingConstructorException {
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
        } catch (NoMessageBodyReaderException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertRepresentationException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertHeaderParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertPathParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertMatrixParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertQueryParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertCookieParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        }
    }

    private final Class<?> jaxRsProviderClass;

    private final ObjectFactory objectFactory;

    private final ThreadLocalizedContext tlContext;

    private final JaxRsProviders allProviders;

    private final ExtensionBackwardMapping extensionBackwardMapping;

    private final Logger logger;

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
        this.objectFactory = objectFactory;
        this.tlContext = tlContext;
        this.allProviders = allProviders;
        this.extensionBackwardMapping = extensionBackwardMapping;
        this.logger = logger;
        createInstance(); // test, if it works.
        // If not, the provider class is not useable.
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
        return Util.getGenericClass(this.jaxRsProviderClass,
                ExceptionMapper.class);
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedCtxResolver()
     */
    public ContextResolver getInitializedCtxResolver()
            throws ProviderNotInitializableException {
        return new SingletonProvider(instantiateAndInitialize(), logger);
    }

    /**
     * Instantiates the provider class, initializes the instance and returns it
     * unwrapped.
     * 
     * @throws ProviderNotInitializableException
     * @throws WebApplicationException
     */
    private Object instantiateAndInitialize()
            throws ProviderNotInitializableException {
        Object jaxRsProvider;
        try {
            jaxRsProvider = createInstance();
        } catch (IllegalConstrParamTypeException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        } catch (IllegalPathParamTypeException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        } catch (IllegalArgumentException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        } catch (WebApplicationException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        } catch (MissingAnnotationException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        } catch (MissingConstructorException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        } catch (InvocationTargetException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        } catch (InstantiateException e) {
            throw new ImplementationException(
                    "The provider could not be instantiated, but this could not be here",
                    e);
        }
        try {
            initProvider(jaxRsProvider, tlContext, allProviders,
                    extensionBackwardMapping);
        } catch (IllegalFieldTypeException e) {
            logger.log(Level.WARNING, "The provider " + this.getClassName()
                    + " could not be initialized and so it could not be used",
                    e);
            throw new ProviderNotInitializableException();
        } catch (IllegalBeanSetterTypeException e) {
            logger.log(Level.WARNING, "The provider " + this.getClassName()
                    + " could not be initialized and so it could not be used",
                    e);
            throw new ProviderNotInitializableException();
        } catch (InjectException e) {
            logger.log(Level.WARNING, "The provider " + this.getClassName()
                    + " could not be initialized and so it could not be used",
                    e);
            throw new ProviderNotInitializableException();
        } catch (InvocationTargetException e) {
            logger.log(Level.WARNING, "The provider " + this.getClassName()
                    + " could not be initialized and so it could not be used",
                    e);
            throw new ProviderNotInitializableException();
        }
        return jaxRsProvider;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedExcMapper()
     */
    @SuppressWarnings("unchecked")
    public ExceptionMapper<? extends Throwable> getInitializedExcMapper()
            throws ProviderNotInitializableException {
        return (ExceptionMapper<? extends Throwable>) instantiateAndInitialize();
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedReader()
     */
    public org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader getInitializedReader()
            throws ProviderNotInitializableException {
        return new SingletonProvider(instantiateAndInitialize(), logger);
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedWriter()
     */
    public org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter getInitializedWriter()
            throws ProviderNotInitializableException {
        return new SingletonProvider(instantiateAndInitialize(), logger);
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#hashCode()
     */
    @Override
    public int hashCode() {
        return this.jaxRsProviderClass.hashCode();
    }

    /**
     * This method does nothing in this class.
     * 
     * @see ProviderWrapper#initAtAppStartUp(ThreadLocalizedContext, Providers,
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
        return Util.doesImplement(jaxRsProviderClass,
                javax.ws.rs.ext.ContextResolver.class);
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#isExceptionMapper()
     */
    @Override
    public boolean isExceptionMapper() {
        return Util.doesImplement(jaxRsProviderClass,
                javax.ws.rs.ext.ExceptionMapper.class);
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#isReader()
     */
    @Override
    public boolean isReader() {
        return Util.doesImplement(jaxRsProviderClass,
                javax.ws.rs.ext.MessageBodyReader.class);
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#isWriter()
     */
    @Override
    public boolean isWriter() {
        return Util.doesImplement(jaxRsProviderClass,
                javax.ws.rs.ext.MessageBodyWriter.class);
    }
}