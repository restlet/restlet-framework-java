/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.util.Converter;

/**
 * Contains the entity providers and has some methods to pick the wished out.
 * 
 * @author Stephan Koops
 */
public class JaxRsProviders implements javax.ws.rs.ext.Providers,
        MessageBodyReaderSet {

    private static final Logger localLogger = Logger
            .getLogger("ExceptionsMapper");

    /**
     * Returns the generic class of the given {@link ContextResolver} class.
     */
    private static Class<?> getCtxResGenClass(Class<?> crClaz) {
        Type[] crIfTypes = crClaz.getGenericInterfaces();
        for (Type crIfType : crIfTypes) {
            if (!(crIfType instanceof ParameterizedType))
                continue;
            Type t = ((ParameterizedType) crIfType).getActualTypeArguments()[0];
            if (!(t instanceof Class))
                continue;
            return (Class<?>) t;
        }
        return null;
    }

    private final Set<ProviderWrapper> all;

    /**
     * This {@link Set} contains all available
     * {@link javax.ws.rs.ext.ContextResolver}s.<br>
     * This field is final, because it is shared with other objects.
     */
    private final Collection<ProviderWrapper> contextResolvers;

    private final Map<Class<? extends Throwable>, ProviderWrapper> excMappers;

    private final ExtensionBackwardMapping extensionBackwardMapping;

    private final Logger logger;

    private final List<ProviderWrapper> messageBodyReaderWrappers;

    private final List<ProviderWrapper> messageBodyWriterWrappers;

    private final ObjectFactory objectFactory;

    private final ThreadLocalizedContext tlContext;

    /**
     * Creates a new JaxRsProviders.
     * 
     * @param objectFactory
     * @param tlContext
     * @param extensionBackwardMapping
     * @param logger
     */
    public JaxRsProviders(ObjectFactory objectFactory,
            ThreadLocalizedContext tlContext,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger) {
        this.all = new CopyOnWriteArraySet<ProviderWrapper>();
        this.messageBodyReaderWrappers = new CopyOnWriteArrayList<ProviderWrapper>();
        this.messageBodyWriterWrappers = new CopyOnWriteArrayList<ProviderWrapper>();
        this.contextResolvers = new CopyOnWriteArraySet<ProviderWrapper>();
        this.excMappers = new ConcurrentHashMap<Class<? extends Throwable>, ProviderWrapper>();

        this.objectFactory = objectFactory;
        this.tlContext = tlContext;
        this.extensionBackwardMapping = extensionBackwardMapping;
        this.logger = logger;
    }

    /**
     * Adds the given provider to this JaxRsProviders. If the Provider is not an
     * entity provider, it doesn't matter.
     * 
     * @param provider
     * @param defaultProvider
     */
    private void add(ProviderWrapper provider, boolean defaultProvider) {
        if (provider.isWriter()) {
            if (defaultProvider)
                this.messageBodyWriterWrappers.add(provider);
            else
                this.messageBodyWriterWrappers.add(0, provider);
        }
        if (provider.isReader()) {
            if (defaultProvider)
                this.messageBodyReaderWrappers.add(provider);
            else
                this.messageBodyReaderWrappers.add(0, provider);
        }
        if (provider.isContextResolver())
            this.contextResolvers.add(provider);
        if (provider.isExceptionMapper())
            this.addExcMapper(provider);
        this.all.add(provider);
    }

    /**
     * @param jaxRsProviderClass
     * @return
     */
    public boolean addClass(Class<?> jaxRsProviderClass) {
        ProviderWrapper provider;
        try {
            provider = new PerRequestProviderWrapper(jaxRsProviderClass,
                    objectFactory, tlContext, this, extensionBackwardMapping,
                    this.logger);
        } catch (IllegalParamTypeException e) {
            String msg = "Ignore provider " + jaxRsProviderClass.getName()
                    + ": Could not instantiate class "
                    + jaxRsProviderClass.getName();
            logger.log(Level.WARNING, msg, e);
            return false;
        } catch (InstantiateException e) {
            String msg = "Ignore provider " + jaxRsProviderClass.getName()
                    + ": Could not instantiate class "
                    + jaxRsProviderClass.getName();
            logger.log(Level.WARNING, msg, e);
            return false;
        } catch (MissingAnnotationException e) {
            String msg = "Ignore provider " + jaxRsProviderClass.getName()
                    + ": Could not instantiate class "
                    + jaxRsProviderClass.getName() + ", because "
                    + e.getMessage();
            logger.log(Level.WARNING, msg);
            return false;
        } catch (InvocationTargetException ite) {
            String msg = "Ignore provider " + jaxRsProviderClass.getName()
                    + ", because an exception occured while instantiating";
            logger.log(Level.WARNING, msg, ite);
            return false;
        } catch (IllegalArgumentException iae) {
            String msg = "Ignore provider " + jaxRsProviderClass.getName()
                    + ", because it could not be instantiated";
            logger.log(Level.WARNING, msg, iae);
            return false;
        } catch (MissingConstructorException mce) {
            String msg = "Ignore provider " + jaxRsProviderClass.getName()
                    + ", because no valid constructor was found: "
                    + mce.getMessage();
            logger.warning(msg);
            return false;
        } catch (IllegalConstrParamTypeException e) {
            String msg = "Ignore provider " + jaxRsProviderClass.getName()
                    + ", because no valid constructor was found: "
                    + e.getMessage();
            logger.warning(msg);
            return false;
        }
        this.add(provider, false);
        return true;
    }

    /**
     * Adds the given {@link ExceptionMapper} to this ExceptionMappers.
     * 
     * @param excMapper
     * @throws NullPointerException
     *                 if null is given
     */
    @SuppressWarnings("unchecked")
    private void addExcMapper(ProviderWrapper excMapperWrapper) {
        Class excClass = excMapperWrapper.getExcMapperType();
        excMappers.put(excClass, excMapperWrapper);
    }

    /**
     * @param jaxRsProviderObject
     * @param defaultProvider
     * @return true if the object was added, false if not.
     * @throws WebApplicationException
     */
    public boolean addSingleton(Object jaxRsProviderObject,
            boolean defaultProvider) throws WebApplicationException {
        ProviderWrapper provider;
        try {
            provider = new SingletonProvider(jaxRsProviderObject, this.logger);
        } catch (IllegalArgumentException iae) {
            String msg = "Ignore provider "
                    + jaxRsProviderObject.getClass().getName()
                    + ", because it could not be instantiated";
            logger.log(Level.WARNING, msg, iae);
            return false;
        }
        this.add(provider, defaultProvider);
        return true;
    }

    /**
     * converts the cause of the given InvocationTargetException to a
     * {@link Response}, if an {@link ExceptionMapper} could be found.<br>
     * Otherwise this method returns an Response with an internal server error.
     * 
     * @param cause
     *                the thrown exception (was wrapped by an
     *                {@link InvocationTargetException})
     * @return the created Response
     * @throws NullPointerException
     *                 if <code>null</code> is given
     */
    @SuppressWarnings("unchecked")
    public Response convert(Throwable cause) {
        // TODO update javadoc in this file and upate the code according to it.
        ExceptionMapper mapper = getExceptionMapper(cause.getClass());
        if (mapper == null) {
            if (cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            if (cause instanceof Error)
                throw (Error) cause;
            String entity = "No ExceptionMapper was found, but must be found";
            return Response.serverError().entity(entity).type(
                    javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE).build();
        }
        Response response = mapper.toResponse(cause);
        if (response == null) {
            String message = "The ExceptionMapper returned null";
            localLogger.log(Level.WARNING, message);
            return Response.serverError().entity(message).build();
        }
        return response;
    }

    /**
     * Returns the {@link MessageBodyReader}, that best matches the given
     * criteria.
     * 
     * @param paramType
     * @param genericType
     * @param annotations
     * @param mediaType
     *                The {@link MediaType}, that should be supported.
     * @return the {@link MessageBodyReader}, that best matches the given
     *         criteria, or null if no matching MessageBodyReader could be
     *         found.
     * @see MessageBodyReaderSet#getBestReader(Class, Type, Annotation[],
     *      MediaType)
     */
    public MessageBodyReader getBestReader(Class<?> paramType,
            Type genericType, Annotation[] annotations, MediaType mediaType) {
        // NICE optimization: may be cached for speed.
        for (ProviderWrapper mbrw : this.messageBodyReaderWrappers) {
            if (mbrw.supportsRead(mediaType)) {
                MessageBodyReader mbr = mbrw.getInitializedReader();
                if (mbr.isReadable(paramType, genericType, annotations))
                    return mbr;
            }
        }
        return null;
    }

    /**
     * @see Providers#getContextResolver(Class, Class,
     *      javax.ws.rs.core.MediaType)
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.ContextResolver<T> getContextResolver(
            Class<T> contextType, Class<?> objectType,
            javax.ws.rs.core.MediaType mediaType) {
        // LATER test JaxRsProviders.getContextResolver
        for (ProviderWrapper crWrapper : this.contextResolvers) {
            final javax.ws.rs.ext.ContextResolver<?> cr;
            cr = crWrapper.getInitializedCtxResolver().getContextResolver();
            // TODO do I need the ContextResolverWrapper?
            final Class<?> crClaz = cr.getClass();
            final Class<?> genClass = JaxRsProviders.getCtxResGenClass(crClaz);
            if (genClass == null || !genClass.equals(contextType)) {
                continue;
            }
            if (!crWrapper.supportsWrite(mediaType)) {
                continue;
            }
            try {
                Method getContext = crClaz.getMethod("getContext", Class.class);
                if (getContext.getReturnType().equals(contextType)) {
                    return (javax.ws.rs.ext.ContextResolver<T>) cr;
                }
            } catch (SecurityException e) {
                throw new RuntimeException(
                        "sorry, the method getContext(Class) of ContextResolver "
                                + crClaz + " is not accessible");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        "The ContextResolver "
                                + crClaz
                                + " is not valid, because it has no method getContext(Class)");
            }
        }
        return null;
    }

    /**
     * @param causeClass
     * @return the ExceptionMapper for the given Throwable class, or null, if
     *         none was found.
     */
    @SuppressWarnings("unchecked")
    public <T> ExceptionMapper<T> getExceptionMapper(Class<T> causeClass) {
        if (causeClass == null)
            throw new ImplementationException(
                    "The call of an exception mapper with null is not allowed");
        ProviderWrapper mapperWrapper;
        do {
            mapperWrapper = this.excMappers.get(causeClass);
            Class superclass = causeClass.getSuperclass();
            if (superclass == null || superclass.equals(Object.class))
                return null;
            causeClass = superclass;
        } while (mapperWrapper == null);
        // disabled caching, because adding of new ExceptionMappers could
        // cause trouble.
        // this.excMappers.put(superclass, mapper);
        return (ExceptionMapper<T>)mapperWrapper.getInitializedExcMapper();
    }

    /**
     * @see javax.ws.rs.ext.Providers#getMessageBodyReader(Class, Type,
     *      Annotation[], javax.ws.rs.core.MediaType)
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.MessageBodyReader<T> getMessageBodyReader(
            Class<T> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType) {
        MediaType restletMediaType = Converter.toRestletMediaType(mediaType);
        final MessageBodyReader mbr;
        mbr = getBestReader(type, genericType, annotations, restletMediaType);
        return (javax.ws.rs.ext.MessageBodyReader) mbr.getJaxRsReader();
    }

    /**
     * @see javax.ws.rs.ext.Providers#getMessageBodyWriter(Class, Type,
     *      Annotation[], javax.ws.rs.core.MediaType)
     */
    @SuppressWarnings( { "unchecked", "cast" })
    public <T> javax.ws.rs.ext.MessageBodyWriter<T> getMessageBodyWriter(
            Class<T> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType) {
        MediaType restletMediaType = Converter.toRestletMediaType(mediaType);
        for (ProviderWrapper mbww : this.messageBodyWriterWrappers) {
            if (mbww.supportsWrite(restletMediaType)) {
                MessageBodyWriter mbw = mbww.getInitializedWriter();
                if (mbw.isWriteable(type, genericType, annotations))
                    return (javax.ws.rs.ext.MessageBodyWriter<T>) mbw
                            .getJaxRsWriter();
            }
        }
        return null;
    }

    /**
     * Init all providers. If an error for one provider occurs, this provider is
     * ignored and the next provider initialized.
     * 
     * @param tlContext
     * @param extensionBackwardMapping
     */
    public void initAll(ThreadLocalizedContext tlContext,
            ExtensionBackwardMapping extensionBackwardMapping) {
        for (final ProviderWrapper provider : new ArrayList<ProviderWrapper>(
                this.all)) {
            try {
                provider.initAtAppStartUp(tlContext, this,
                        extensionBackwardMapping);
            } catch (InjectException e) {
                localLogger.log(Level.WARNING, "The provider "
                        + provider.getClassName() + " could not be used", e);
                this.remove(provider);
            } catch (IllegalTypeException e) {
                localLogger.log(Level.WARNING, "The provider "
                        + provider.getClassName() + " could not be used", e);
                this.remove(provider);
            } catch (InvocationTargetException e) {
                localLogger.log(Level.WARNING, "The provider "
                        + provider.getClassName() + " could not be used", e
                        .getCause());
                this.remove(provider);
            }
        }
    }

    /**
     * @param provider
     */
    private void remove(ProviderWrapper provider) {
        this.all.remove(provider);
        this.contextResolvers.remove(provider);
        this.messageBodyReaderWrappers.remove(provider);
        this.messageBodyWriterWrappers.remove(provider);
        Iterator<Map.Entry<Class<? extends Throwable>, ProviderWrapper>> excMapperEntryIter = this.excMappers
                .entrySet().iterator();
        while (excMapperEntryIter.hasNext()) {
            final Map.Entry<Class<? extends Throwable>, ProviderWrapper> excMapperEntry;
            excMapperEntry = excMapperEntryIter.next();
            ProviderWrapper providerWrapper = excMapperEntry.getValue();
            if (providerWrapper.equals(provider))
                excMapperEntryIter.remove();
        }
    }

    /**
     * Returns a Collection of {@link MessageBodyWriter}s, that support the
     * given entityClass.
     * 
     * @param entityClass
     * @param genericType
     *                may be null
     * @param annotations
     *                may be null
     * @return
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class, Type,
     *      Annotation[])
     */
    public MessageBodyWriterSubSet writerSubSet(Class<?> entityClass,
            Type genericType, Annotation[] annotations) {
        // NICE optimization: may be cached for speed.
        final List<MessageBodyWriter> mbws = new ArrayList<MessageBodyWriter>();
        for (ProviderWrapper mbww : this.messageBodyWriterWrappers) {
            MessageBodyWriter mbw = mbww.getInitializedWriter();
            if (mbw.isWriteable(entityClass, genericType, annotations))
                mbws.add(mbw);
        }
        return new MessageBodyWriterSubSet(mbws);
    }
}