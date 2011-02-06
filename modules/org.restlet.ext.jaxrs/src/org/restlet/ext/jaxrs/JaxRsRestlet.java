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

import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.addPathVarsToMap;
import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.getBestMethod;
import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.getFirstByNoOfLiteralCharsNoOfCapturingGroups;
import static org.restlet.ext.jaxrs.internal.util.AlgorithmUtil.removeNotSupportedHttpMethod;
import static org.restlet.ext.jaxrs.internal.util.Util.copyResponseHeaders;
import static org.restlet.ext.jaxrs.internal.util.Util.getMediaType;
import static org.restlet.ext.jaxrs.internal.util.Util.getSupportedCharSet;
import static org.restlet.ext.jaxrs.internal.util.Util.sortByConcreteness;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.internal.provider.BufferedReaderProvider;
import org.restlet.ext.jaxrs.internal.provider.ByteArrayProvider;
import org.restlet.ext.jaxrs.internal.provider.ConverterProvider;
import org.restlet.ext.jaxrs.internal.provider.FileProvider;
import org.restlet.ext.jaxrs.internal.provider.InputStreamProvider;
import org.restlet.ext.jaxrs.internal.provider.ReaderProvider;
import org.restlet.ext.jaxrs.internal.provider.SourceProvider;
import org.restlet.ext.jaxrs.internal.provider.StreamingOutputProvider;
import org.restlet.ext.jaxrs.internal.provider.StringProvider;
import org.restlet.ext.jaxrs.internal.provider.WebAppExcMapper;
import org.restlet.ext.jaxrs.internal.provider.WwwFormFormProvider;
import org.restlet.ext.jaxrs.internal.provider.WwwFormMmapProvider;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.ExceptionHandler;
import org.restlet.ext.jaxrs.internal.util.JaxRsOutputRepresentation;
import org.restlet.ext.jaxrs.internal.util.MatchingResult;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.util.WrappedRequestForHttpHeaders;
import org.restlet.ext.jaxrs.internal.wrappers.AbstractMethodWrapper;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClasses;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethodOrLocator;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceObject;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.SubResourceLocator;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExtensionBackwardMapping;
import org.restlet.ext.jaxrs.internal.wrappers.provider.JaxRsProviders;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriterSubSet;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;
import org.restlet.service.MetadataService;

/**
 * <p>
 * This class choose the JAX-RS resource class and method to use for a request
 * and handles the result from he resource method. Typically you should
 * instantiate a {@link JaxRsApplication} to run JAX-RS resource classes.
 * </p>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Stephan Koops
 */
public class JaxRsRestlet extends Restlet {
    /**
     * Structure to return the obtained {@link ResourceObject} and the
     * identified {@link ResourceMethod}.
     * 
     * @author Stephan Koops
     */
    class ResObjAndMeth {

        private ResourceObject resourceObject;

        private ResourceMethod resourceMethod;

        ResObjAndMeth(ResourceObject resourceObject,
                ResourceMethod resourceMethod) {
            this.resourceObject = resourceObject;
            this.resourceMethod = resourceMethod;
        }
    }

    /**
     * Structure to return the obtained {@link ResourceObject} and the remaining
     * path after identifying the object.
     * 
     * @author Stephan Koops
     */
    class ResObjAndRemPath {

        private ResourceObject resourceObject;

        private RemainingPath u;

        ResObjAndRemPath(ResourceObject resourceObject, RemainingPath u) {
            this.resourceObject = resourceObject;
            this.u = u;
        }
    }

    /**
     * Structure to return an instance of the identified
     * {@link RootResourceClass}, the matched URI path and the remaining path
     * after identifying the root resource class.
     * 
     * @author Stephan Koops
     */
    class RroRemPathAndMatchedPath {

        private ResourceObject rootResObj;

        private RemainingPath u;

        private String matchedUriPath;

        RroRemPathAndMatchedPath(ResourceObject rootResObj, RemainingPath u,
                String matchedUriPath) {
            this.rootResObj = rootResObj;
            this.u = u;
            this.matchedUriPath = matchedUriPath;
        }
    }

    static {
        javax.ws.rs.ext.RuntimeDelegate
                .setInstance(new org.restlet.ext.jaxrs.internal.spi.RuntimeDelegateImpl());
    }

    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    @SuppressWarnings("deprecation")
    private volatile RoleChecker roleChecker;

    private final JaxRsProviders providers;

    private final ResourceClasses resourceClasses;

    /**
     * Contains and handles the exceptions occurring while in resource objects
     * and providers, and also for the other cases where the runtime environment
     * should throw {@link WebApplicationException}.
     */
    private final ExceptionHandler excHandler;

    /**
     * Contains the thread localized {@link CallContext}s.
     */
    private final ThreadLocalizedContext tlContext = new ThreadLocalizedContext();

    private volatile ObjectFactory objectFactory;

    /**
     * Creates a new JaxRsRestlet with the given Context. Only the default
     * providers are loaded.
     * 
     * @param context
     *            the context from the parent, see
     *            {@link Restlet#Restlet(Context)}.
     * @param metadataService
     *            the metadata service of the {@link JaxRsApplication}.
     * @see #JaxRsRestlet(Context, RoleChecker, MetadataService)
     */
    public JaxRsRestlet(Context context, MetadataService metadataService) {
        this(context, null, metadataService);
    }

    /**
     * Creates a new JaxRsRestlet with the given Context. Only the default
     * providers are loaded.
     * 
     * @param context
     *            the context from the parent, see
     *            {@link Restlet#Restlet(Context)}.
     * @param roleChecker
     *            The RoleChecker to use. If null, the normal Restlet security
     *            API wil be used.
     * @param metadataService
     *            the metadata service of the {@link JaxRsApplication}.
     * @see #JaxRsRestlet(Context, MetadataService)
     */
    @SuppressWarnings("deprecation")
    public JaxRsRestlet(Context context, RoleChecker roleChecker,
            MetadataService metadataService) {
        super(context);
        final ExtensionBackwardMapping extensionBackwardMapping = new ExtensionBackwardMapping(
                metadataService);
        this.excHandler = new ExceptionHandler(getLogger());
        this.providers = new JaxRsProviders(this.objectFactory, this.tlContext,
                extensionBackwardMapping, getLogger());
        this.resourceClasses = new ResourceClasses(this.tlContext,
                this.providers, extensionBackwardMapping, getLogger());
        this.loadDefaultProviders();
        this.setRoleChecker(roleChecker);
    }

    /**
     * Will use the given JAX-RS root resource class.<br>
     * If the given class is not a valid root resource class, a warning is
     * logged and false is returned.
     * 
     * @param jaxRsClass
     *            A JAX-RS root resource class or provider class to add. If the
     *            root resource class is already available in this JaxRsRestlet,
     *            it is ignored for later calls of this method.
     * @return true if the class is added or was already included, or false if
     *         the given class is not a valid class (a warning was logged).
     * @throws IllegalArgumentException
     *             if the root resource class is null.
     */
    public boolean addClass(Class<?> jaxRsClass)
            throws IllegalArgumentException {
        if (jaxRsClass == null)
            throw new IllegalArgumentException(
                    "The JAX-RS class to add must not be null");
        boolean used = false;
        if (Util.isRootResourceClass(jaxRsClass)) {
            used = resourceClasses.addRootClass(jaxRsClass);
        }
        if (Util.isProvider(jaxRsClass)) {
            if (providers.addClass(jaxRsClass))
                used = true;
        }
        // @see Application#getClasses()
        // @see Application#getSingletons()
        if (!used) {
            final String warning = ("The class " + jaxRsClass + " is neither a povider nor a root resource class");
            getLogger().warning(warning);
        }
        return used;
    }

    /**
     * Adds the provider object as default provider to this JaxRsRestlet.
     * 
     * @param jaxRsProvider
     *            The provider object or class name.
     * @return true, if the provider is ok and added, otherwise false.
     * @throws IllegalArgumentException
     *             if null was given
     * @see {@link javax.ws.rs.ext.Provider}
     */
    private boolean addDefaultProvider(Object jaxRsProvider) {
        try {
            return addSingleton(jaxRsProvider, true);
        } catch (GenericSignatureFormatError e) {
            getLogger().warning(
                    "Unable to add default provider class : " + jaxRsProvider);
        }
        return false;
    }

    /**
     * Adds the provider object to this JaxRsRestlet.
     * 
     * @param jaxRsProviderOrRootresourceObject
     *            the JAX-RS provider or root resource object
     * @return true, if the provider is ok and added, otherwise false.
     * @throws IllegalArgumentException
     *             if null was given
     * @see javax.ws.rs.ext.Provider
     */
    public boolean addSingleton(Object jaxRsProviderOrRootresourceObject)
            throws IllegalArgumentException {
        return addSingleton(jaxRsProviderOrRootresourceObject, false);
    }

    /**
     * Adds the provider object to this JaxRsRestlet.
     * 
     * @param jaxRsProvider
     *            The provider object or class name.
     * @param defaultProvider
     * @return true, if the provider is ok and added, otherwise false.
     * @throws IllegalArgumentException
     *             if null was given
     * @see {@link javax.ws.rs.ext.Provider}
     */
    private boolean addSingleton(Object jaxRsProvider, boolean defaultProvider)
            throws IllegalArgumentException {
        if (jaxRsProvider instanceof String) {
            try {
                jaxRsProvider = Engine.loadClass((String) jaxRsProvider)
                        .newInstance();
            } catch (ClassNotFoundException e) {
                getLogger().fine(
                        "Unable to load provider class : " + jaxRsProvider);
                jaxRsProvider = null;
            } catch (InstantiationException e) {
                getLogger().fine(
                        "Unable to instantiate provider : " + jaxRsProvider);
                jaxRsProvider = null;
            } catch (IllegalAccessException e) {
                getLogger().fine(
                        "Unable to access to provider : " + jaxRsProvider);
                jaxRsProvider = null;
            } catch (NoClassDefFoundError e) {
                getLogger().fine(
                        "Unable to load provider class : " + jaxRsProvider);
                jaxRsProvider = null;
            }
        } else if (jaxRsProvider == null)
            throw new IllegalArgumentException(
                    "The JAX-RS object to add must not be null");
        else if (jaxRsProvider instanceof Class<?>)
            throw new IllegalArgumentException(
                    "The JAX-RS object to add must not be a java.lang.Class");

        boolean used = false;
        if (jaxRsProvider != null) {
            if (defaultProvider || Util.isProvider(jaxRsProvider.getClass())) {
                used = providers.addSingleton(jaxRsProvider, defaultProvider);
            }
            if (Util.isRootResourceClass(jaxRsProvider.getClass())) {
                throw new NotYetImplementedException(
                        "only providers are allowed as singletons for now");
                // used = ...
            }
            if (!used) {
                final String warning = ("The class " + jaxRsProvider.getClass() + " is neither a provider nor a root resource class");
                getLogger().warning(warning);
            }
        }

        return used;
    }

    // now methods for the daily work

    /**
     * Sets the Restlet that is called, if no (root) resource class or method
     * could be found.
     * 
     * @param notMatchedRestlet
     *            the Restlet to call, if no (root) resource class or method
     *            could be found.
     * @see #setNoRootResClHandler(Restlet)
     * @see #setNoResourceClHandler(Restlet)
     * @see #setNoResMethodHandler(Restlet)
     * @see Router#attachDefault(Restlet)
     */
    public void attachDefault(Restlet notMatchedRestlet) {
        this.setNoRootResClHandler(notMatchedRestlet);
        this.setNoResourceClHandler(notMatchedRestlet);
        this.setNoResMethodHandler(notMatchedRestlet);
    }

    /**
     * Converts the given entity - returned by the resource method - to a
     * Restlet {@link Representation}.
     * 
     * @param entity
     *            the entity to convert.
     * @param resourceMethod
     *            The resource method created the entity. Could be null, if an
     *            exception is handled, e.g. a {@link WebApplicationException}.
     * @param jaxRsResponseMediaType
     *            The MediaType of the JAX-RS {@link javax.ws.rs.core.Response}.
     *            May be null.
     * @param jaxRsRespHeaders
     *            The headers added to the {@link javax.ws.rs.core.Response} by
     *            the {@link ResponseBuilder}.
     * @param accMediaTypes
     *            the accepted media type from the current call context, or the
     *            returned of the JAX-RS {@link javax.ws.rs.core.Response}.
     * @return the corresponding Restlet Representation. Returns
     *         <code>null</code> only if null was given.
     * @throws WebApplicationException
     * @see AbstractMethodWrapper.EntityGetter
     */
    private Representation convertToRepresentation(Object entity,
            ResourceMethod resourceMethod, MediaType jaxRsResponseMediaType,
            MultivaluedMap<String, Object> jaxRsRespHeaders,
            SortedMetadata<MediaType> accMediaTypes)
            throws ImplementationException {
        if (entity instanceof Representation) {
            Representation repr = (Representation) entity;
            // ensures that a supported character set is set
            repr.setCharacterSet(getSupportedCharSet(repr.getCharacterSet()));
            if (jaxRsResponseMediaType != null) {
                repr.setMediaType(Converter
                        .getMediaTypeWithoutParams(jaxRsResponseMediaType));
            }
            return repr;
        }
        Type genericReturnType;
        Class<? extends Object> entityClass;
        Annotation[] methodAnnotations;
        if (resourceMethod != null) // is default
            methodAnnotations = resourceMethod.getAnnotations();
        else
            methodAnnotations = EMPTY_ANNOTATION_ARRAY;

        if (entity instanceof GenericEntity) {
            GenericEntity<?> genericEntity = (GenericEntity<?>) entity;
            genericReturnType = genericEntity.getType();
            entityClass = genericEntity.getRawType();
            entity = genericEntity.getEntity();
        } else {
            entityClass = (entity != null) ? entity.getClass() : null;
            if (resourceMethod != null) // is default
                genericReturnType = resourceMethod.getGenericReturnType();
            else
                genericReturnType = null;
            if (genericReturnType instanceof Class
                    && ((Class<?>) genericReturnType)
                            .isAssignableFrom(javax.ws.rs.core.Response.class)) {
                genericReturnType = entityClass;
            }
        }

        final MultivaluedMap<String, Object> httpResponseHeaders = new WrappedRequestForHttpHeaders(
                tlContext.get().getResponse(), jaxRsRespHeaders);
        final Representation repr;

        if (entity != null) {
            final MediaType respMediaType = determineMediaType(
                    jaxRsResponseMediaType, resourceMethod, entityClass,
                    genericReturnType);

            final MessageBodyWriterSubSet mbws;
            mbws = providers.writerSubSet(entityClass, genericReturnType);
            if (mbws.isEmpty())
                throw excHandler.noMessageBodyWriter(entityClass,
                        genericReturnType, methodAnnotations, null, null);

            final MessageBodyWriter mbw = mbws.getBestWriter(respMediaType,
                    methodAnnotations, accMediaTypes);
            if (mbw == null)
                throw excHandler.noMessageBodyWriter(entityClass,
                        genericReturnType, methodAnnotations, respMediaType,
                        accMediaTypes);
            repr = new JaxRsOutputRepresentation<Object>(entity,
                    genericReturnType, respMediaType, methodAnnotations, mbw,
                    httpResponseHeaders);
        } else { // entity == null
            repr = new EmptyRepresentation();
            repr.setMediaType(determineMediaType(jaxRsResponseMediaType,
                    resourceMethod, entityClass, genericReturnType));
        }
        repr.setCharacterSet(getSupportedCharSet(httpResponseHeaders));
        return repr;
    }

    /**
     * @param o
     * @param subResourceLocator
     * @param callContext
     * @return
     * @throws WebApplicationException
     * @throws RequestHandledException
     */
    private ResourceObject createSubResource(ResourceObject o,
            SubResourceLocator subResourceLocator, CallContext callContext)
            throws WebApplicationException, RequestHandledException {
        try {
            o = subResourceLocator.createSubResource(o, resourceClasses,
                    getLogger());
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw excHandler.runtimeExecption(e, subResourceLocator,
                    callContext,
                    "Could not create new instance of resource class");
        } catch (MissingAnnotationException e) {
            throw excHandler.missingAnnotation(e, callContext,
                    "Could not create new instance of resource class");
        } catch (InstantiateException e) {
            throw excHandler.instantiateExecption(e, callContext,
                    "Could not create new instance of resource class");
        } catch (InvocationTargetException e) {
            throw handleInvocationTargetExc(e);
        } catch (ConvertRepresentationException e) {
            throw excHandler.convertRepresentationExc(e);
        }
        return o;
    }

    /**
     * Determines the MediaType for a response, see JAX-RS-Spec (2008-08-27),
     * section 3.8 "Determining the MediaType of Responses"
     * 
     * @param jaxRsResponseMediaType
     * @param resourceMethod
     *            The ResourceMethod that created the entity.
     * @param entityClass
     *            needed, if neither the resource method nor the resource class
     *            is annotated with &#64;{@link Produces}.
     * @param genericReturnType
     *            needed, if neither the resource method nor the resource class
     *            is annotated with &#64;{@link Produces}.
     * @param methodAnnotation
     *            needed, if neither the resource method nor the resource class
     *            is annotated with &#64;{@link Produces}.
     * @param mbws
     *            The {@link MessageBodyWriter}s, that support the class of the
     *            returned entity object as generic type of the
     *            {@link MessageBodyWriter}.
     * @return the determined {@link MediaType}. If no method is given,
     *         "text/plain" is returned.
     * @throws WebApplicationException
     */
    private MediaType determineMediaType(MediaType jaxRsResponseMediaType,
            ResourceMethod resourceMethod, Class<?> entityClass,
            Type genericReturnType) throws WebApplicationException {
        // 1. if the Response contains a MediaType, use it.
        if (jaxRsResponseMediaType != null)
            return jaxRsResponseMediaType;
        if (resourceMethod == null)
            return MediaType.TEXT_PLAIN;
        CallContext callContext = tlContext.get();
        // 2. Gather the set of producible media types P:
        // (a) + (b)
        Collection<MediaType> p = resourceMethod.getProducedMimes();
        // 2. (c)
        if (p.isEmpty()) {
            p = providers.writerSubSet(entityClass, genericReturnType)
                    .getAllProducibleMediaTypes();
            // 3.
            if (p.isEmpty())
                // '*/*', in conjunction with 8.:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
        // 4. Obtain the acceptable media types A.
        SortedMetadata<MediaType> a = callContext.getAccMediaTypes();
        // 4. If A = {}, set A = {'*/*'}
        if (a.isEmpty())
            a = SortedMetadata.getMediaTypeAll();
        // 5. Sort P and A (A is already sorted)
        List<MediaType> pSorted = sortByConcreteness(p);
        // 6.
        List<MediaType> m = new ArrayList<MediaType>();
        for (MediaType acc : a)
            for (MediaType prod : pSorted)
                if (prod.isCompatible(acc))
                    m.add(MediaType.getMostSpecific(prod, acc));
        // 7.
        if (m.isEmpty())
            excHandler.notAcceptableWhileDetermineMediaType();
        // 8.
        for (MediaType mediaType : m)
            if (mediaType.isConcrete())
                return mediaType;
        // 9.
        if (m.contains(MediaType.ALL) || m.contains(MediaType.APPLICATION_ALL))
            return MediaType.APPLICATION_OCTET_STREAM;
        // 10.
        throw excHandler.notAcceptableWhileDetermineMediaType();
    }

    /**
     * Returns the Restlet that is called, if no resource method class could be
     * found.
     * 
     * @return the Restlet that is called, if no resource method class could be
     *         found.
     * @see #setNoResMethodHandler(Restlet)
     */
    public Restlet getNoResMethodHandler() {
        return excHandler.getNoResMethodHandler();
    }

    /**
     * Returns the Restlet that is called, if no resource class could be found.
     * 
     * @return the Restlet that is called, if no resource class could be found.
     */
    public Restlet getNoResourceClHandler() {
        return excHandler.getNoResourceClHandler();
    }

    /**
     * Returns the Restlet that is called, if no root resource class could be
     * found. You could remove a given Restlet by set null here.<br>
     * If no Restlet is given here, status 404 will be returned.
     * 
     * @return the Restlet that is called, if no root resource class could be
     *         found.
     * @see #setNoRootResClHandler(Restlet)
     */
    public Restlet getNoRootResClHandler() {
        return excHandler.getNoRootResClHandler();
    }

    /**
     * Returns the ObjectFactory for root resource class and provider
     * instantiation, if given.
     * 
     * @return the ObjectFactory for root resource class and provider
     *         instantiation, if given.
     */
    public ObjectFactory getObjectFactory() {
        return this.objectFactory;
    }

    /**
     * Gets the currently used {@link RoleChecker}.
     * 
     * @return the currently used RoleChecker.
     * @see #setRoleChecker(RoleChecker)
     * @deprecated Use {@link ClientInfo#getRoles()} instead
     */
    @Deprecated
    public RoleChecker getRoleChecker() {
        return roleChecker;
    }

    /**
     * Returns an unmodifiable set with the attached root resource classes.
     * 
     * @return an unmodifiable set with the attached root resource classes.
     */
    public Set<Class<?>> getRootResourceClasses() {
        Set<Class<?>> rrcs = new HashSet<Class<?>>();
        for (RootResourceClass rootResourceClass : this.resourceClasses.roots())
            rrcs.add(rootResourceClass.getJaxRsClass());
        return Collections.unmodifiableSet(rrcs);
    }

    /**
     * Returns a Collection with all root uris attached to this JaxRsRestlet.
     * 
     * @return a Collection with all root uris attached to this JaxRsRestlet.
     */
    public Collection<String> getRootUris() {
        List<String> uris = new ArrayList<String>();
        for (RootResourceClass rrc : this.resourceClasses.roots())
            uris.add(rrc.getPathRegExp().getPathTemplateEnc());
        return Collections.unmodifiableCollection(uris);
    }

    /**
     * Handles a call by looking for the resource metod to call, call it and
     * return the result.
     * 
     * @param request
     *            The {@link Request} to handle.
     * @param response
     *            The {@link Response} to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        ResourceObject resourceObject = null;
        final Reference baseRef = request.getResourceRef().getBaseRef();
        request.setRootRef(new Reference(baseRef.toString()));
        // NICE Normally, the "rootRef" property is set by the VirtualHost, each
        // time a request is handled by one of its routes.
        // Email from Jerome, 2008-09-22
        try {
            CallContext callContext;
            callContext = new CallContext(request, response, this.roleChecker);
            tlContext.set(callContext);
            try {
                ResObjAndMeth resObjAndMeth;
                resObjAndMeth = requestMatching();
                callContext.setReadOnly();
                ResourceMethod resourceMethod = resObjAndMeth.resourceMethod;
                resourceObject = resObjAndMeth.resourceObject;
                Object result = invokeMethod(resourceMethod, resourceObject);
                handleResult(result, resourceMethod);
            } catch (WebApplicationException e) {
                // the message of the Exception is not used in the
                // WebApplicationException
                jaxRsRespToRestletResp(this.providers.convert(e), null);
                return;
            }
        } catch (RequestHandledException e) {
            // Exception was handled and data were set into the Response.
        } finally {
            Representation entity = request.getEntity();
            if (entity != null)
                entity.release();
        }
    }

    /**
     * Handles the given Exception, catched by an invoke of a resource method or
     * a creation if a sub resource object.
     * 
     * @param ite
     * @param methodName
     * @throws RequestHandledException
     *             throws this message to exit the method and indicate, that the
     *             request was handled.
     * @throws RequestHandledException
     */
    private RequestHandledException handleInvocationTargetExc(
            InvocationTargetException ite) throws RequestHandledException {
        Throwable cause = ite.getCause();
        if (cause instanceof ResourceException) {
            // avoid mapping to a JAX-RS Response and back to a Restlet Response
            Status status = ((ResourceException) cause).getStatus();
            Response restletResponse = tlContext.get().getResponse();
            restletResponse.setStatus(status);
        } else {
            javax.ws.rs.core.Response jaxRsResp = this.providers.convert(cause);
            jaxRsRespToRestletResp(jaxRsResp, null);
        }
        throw new RequestHandledException();
    }

    /**
     * Sets the result of the resource method invocation into the response. Do
     * necessary converting.
     * 
     * @param result
     *            the object returned by the resource method
     * @param resourceMethod
     *            the resource method; it is needed for the conversion. Could be
     *            null, if an exception is handled, e.g. a
     *            {@link WebApplicationException}.
     */
    private void handleResult(Object result, ResourceMethod resourceMethod) {
        Response restletResponse = tlContext.get().getResponse();
        if (result instanceof javax.ws.rs.core.Response) {
            jaxRsRespToRestletResp((javax.ws.rs.core.Response) result,
                    resourceMethod);
        } else if (result instanceof ResponseBuilder) {
            String warning = "the method " + resourceMethod
                    + " returnef a ResponseBuilder. You should "
                    + "call responseBuilder.build() in the resource method";
            getLogger().warning(warning);
            jaxRsRespToRestletResp(((ResponseBuilder) result).build(),
                    resourceMethod);
        } else {
            if (result == null) // no representation
                restletResponse.setStatus(Status.SUCCESS_NO_CONTENT);
            else
                restletResponse.setStatus(Status.SUCCESS_OK);
            SortedMetadata<MediaType> accMediaTypes;
            accMediaTypes = tlContext.get().getAccMediaTypes();
            restletResponse.setEntity(convertToRepresentation(result,
                    resourceMethod, null, null, accMediaTypes));
        }
    }

    /**
     * Identifies the method that will handle the request, see JAX-RS-Spec
     * (2008-04-16), section 3.7.2 "Request Matching", Part 3: Identify the
     * method that will handle the request:"
     * 
     * @return Resource Object and Method, that handle the request.
     * @throws RequestHandledException
     *             for example if the method was OPTIONS, but no special
     *             Resource Method for OPTIONS is available.
     * @throws ResourceMethodNotFoundException
     */
    private ResObjAndMeth identifyMethod(ResObjAndRemPath resObjAndRemPath,
            MediaType givenMediaType) throws RequestHandledException {
        CallContext callContext = tlContext.get();
        org.restlet.data.Method httpMethod = callContext.getRequest()
                .getMethod();
        // 3. Identify the method that will handle the request:
        // (a)
        ResourceObject resObj = resObjAndRemPath.resourceObject;
        RemainingPath u = resObjAndRemPath.u;
        // (a) 1
        ResourceClass resourceClass = resObj.getResourceClass();
        Collection<ResourceMethod> resourceMethods = resourceClass
                .getMethodsForPath(u);
        if (resourceMethods.isEmpty())
            excHandler.resourceMethodNotFound();// NICE (resourceClass, u);
        // (a) 2: remove methods not support the given method
        boolean alsoGet = httpMethod.equals(Method.HEAD);
        removeNotSupportedHttpMethod(resourceMethods, httpMethod, alsoGet);
        if (resourceMethods.isEmpty()) {
            Set<Method> allowedMethods = resourceClass.getAllowedMethods(u);
            if (httpMethod.equals(Method.OPTIONS)) {
                callContext.getResponse().getAllowedMethods()
                        .addAll(allowedMethods);
                throw new RequestHandledException();
            }
            excHandler.methodNotAllowed(allowedMethods);
        }
        // (a) 3
        if (givenMediaType != null) {
            Collection<ResourceMethod> supporting = resourceMethods;
            resourceMethods = new ArrayList<ResourceMethod>();
            for (ResourceMethod resourceMethod : supporting) {
                if (resourceMethod.isGivenMediaTypeSupported(givenMediaType))
                    resourceMethods.add(resourceMethod);
            }
            if (resourceMethods.isEmpty())
                excHandler.unsupportedMediaType(supporting);
        }
        // (a) 4
        SortedMetadata<MediaType> accMediaTypes = callContext
                .getAccMediaTypes();
        Collection<ResourceMethod> supporting = resourceMethods;
        resourceMethods = new ArrayList<ResourceMethod>();
        for (ResourceMethod resourceMethod : supporting) {
            if (resourceMethod.isAcceptedMediaTypeSupported(accMediaTypes))
                resourceMethods.add(resourceMethod);
        }
        if (resourceMethods.isEmpty()) {
            excHandler.noResourceMethodForAccMediaTypes(supporting);
        }
        // (b) and (c)
        ResourceMethod bestResourceMethod = getBestMethod(resourceMethods,
                givenMediaType, accMediaTypes, httpMethod);
        MatchingResult mr = bestResourceMethod.getPathRegExp().match(u);
        addPathVarsToMap(mr, callContext);
        String matchedUriPart = mr.getMatched();
        if (matchedUriPart.length() > 0) {
            Object jaxRsResObj = resObj.getJaxRsResourceObject();
            callContext.addForMatched(jaxRsResObj, matchedUriPart);
        }
        return new ResObjAndMeth(resObj, bestResourceMethod);
    }

    /**
     * Identifies the root resource class, see JAX-RS-Spec (2008-04-16), section
     * 3.7.2 "Request Matching", Part 1: "Identify the root resource class"
     * 
     * @param u
     *            the remaining path after the base ref
     * @return The identified root resource object, the remaining path after
     *         identifying and the matched template parameters; see
     *         {@link RroRemPathAndMatchedPath}.
     * @throws WebApplicationException
     * @throws RequestHandledException
     */
    private RroRemPathAndMatchedPath identifyRootResource(RemainingPath u)
            throws WebApplicationException, RequestHandledException {
        // 1. Identify the root resource class:
        // (a)
        // c: Set<Class>: root resource classes
        // e: Set<RegExp>
        // Map<UriTemplateRegExp, Class> eAndCs = new HashMap();
        Collection<RootResourceClass> eAndCs = new ArrayList<RootResourceClass>();
        // (a) and (b) and (c) Filter E
        for (RootResourceClass rootResourceClass : this.resourceClasses.roots()) {
            // Map.Entry<UriTemplateRegExp, Class> eAndC = eAndCIter.next();
            // UriTemplateRegExp regExp = eAndC.getKey();
            // Class clazz = eAndC.getValue();
            PathRegExp rrcPathRegExp = rootResourceClass.getPathRegExp();
            MatchingResult matchingResult = rrcPathRegExp.match(u);
            if (matchingResult == null)
                continue; // doesn't match
            if (matchingResult.getFinalCapturingGroup().isEmptyOrSlash())
                eAndCs.add(rootResourceClass);
            else if (rootResourceClass.hasSubResourceMethodsOrLocators())
                eAndCs.add(rootResourceClass);
        }
        // (d)
        if (eAndCs.isEmpty())
            excHandler.rootResourceNotFound();
        // (e) and (f)
        RootResourceClass tClass = getFirstByNoOfLiteralCharsNoOfCapturingGroups(eAndCs);
        // (f)
        PathRegExp rMatch = tClass.getPathRegExp();
        MatchingResult matchResult = rMatch.match(u);
        u = matchResult.getFinalCapturingGroup();
        addPathVarsToMap(matchResult, tlContext.get());
        ResourceObject o = instantiateRrc(tClass);
        return new RroRemPathAndMatchedPath(o, u, matchResult.getMatched());
    }

    /**
     * Instantiates the root resource class and handles thrown exceptions.
     * 
     * @param rrc
     *            the root resource class to instantiate
     * @return the instance of the root resource
     * @throws WebApplicationException
     *             if a WebApplicationException was thrown while creating the
     *             instance.
     * @throws RequestHandledException
     *             If an Exception was thrown and the request is already
     *             handeled.
     */
    private ResourceObject instantiateRrc(RootResourceClass rrc)
            throws WebApplicationException, RequestHandledException {
        ResourceObject o;
        try {
            o = rrc.getInstance(this.objectFactory);
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw excHandler.runtimeExecption(e, null, tlContext.get(),
                    "Could not create new instance of root resource class");
        } catch (InstantiateException e) {
            throw excHandler.instantiateExecption(e, tlContext.get(),
                    "Could not create new instance of root resource class");
        } catch (InvocationTargetException e) {
            throw handleInvocationTargetExc(e);
        }
        return o;
    }

    /**
     * Invokes the (sub) resource method. Handles / converts also occuring
     * exceptions.
     * 
     * @param resourceMethod
     *            the (sub) resource method to invoke
     * @param resourceObject
     *            the resource object to invoke the method on.
     * @return the object returned by the (sub) resource method.
     * @throws RequestHandledException
     *             if the request is already handled
     * @throws WebApplicationException
     *             if a JAX-RS class throws an WebApplicationException
     */
    private Object invokeMethod(ResourceMethod resourceMethod,
            ResourceObject resourceObject) throws WebApplicationException,
            RequestHandledException {
        Object result;
        try {
            result = resourceMethod.invoke(resourceObject);
        } catch (WebApplicationException e) {
            throw e;
        } catch (InvocationTargetException ite) {
            throw handleInvocationTargetExc(ite);
        } catch (RuntimeException e) {
            throw excHandler.runtimeExecption(e, resourceMethod,
                    tlContext.get(), "Can not invoke the resource method");
        } catch (MethodInvokeException e) {
            throw excHandler.methodInvokeException(e, tlContext.get(),
                    "Can not invoke the resource method");
        } catch (ConvertRepresentationException e) {
            throw excHandler.convertRepresentationExc(e);
        }
        return result;
    }

    /**
     * Converts the given JAX-RS {@link javax.ws.rs.core.Response} to a Restlet
     * {@link Response}.
     * 
     * @param jaxRsResponse
     *            The response returned by the resource method, perhaps as
     *            attribute of a {@link WebApplicationException}.
     * @param resourceMethod
     *            The resource method creating the response. Could be null, if
     *            an exception is handled, e.g. a
     *            {@link WebApplicationException}.
     */
    private void jaxRsRespToRestletResp(
            javax.ws.rs.core.Response jaxRsResponse,
            ResourceMethod resourceMethod) {
        Response restletResponse = tlContext.get().getResponse();
        restletResponse.setStatus(Status.valueOf(jaxRsResponse.getStatus()));
        MultivaluedMap<String, Object> httpHeaders = jaxRsResponse
                .getMetadata();
        MediaType respMediaType = getMediaType(httpHeaders);
        Object jaxRsEntity = jaxRsResponse.getEntity();
        SortedMetadata<MediaType> accMediaType;
        if (respMediaType != null)
            accMediaType = SortedMetadata.get(respMediaType);
        else
            accMediaType = tlContext.get().getAccMediaTypes();
        restletResponse.setEntity(convertToRepresentation(jaxRsEntity,
                resourceMethod, respMediaType, httpHeaders, accMediaType));
        copyResponseHeaders(httpHeaders, restletResponse);
    }

    private void loadDefaultProviders() {
        addDefaultProvider(new BufferedReaderProvider());
        addDefaultProvider(new ByteArrayProvider());
        addDefaultProvider("org.restlet.ext.jaxrs.internal.provider.DataSourceProvider");

        // not yet tested
        addDefaultProvider("org.restlet.ext.jaxrs.internal.provider.FileUploadProvider");

        addDefaultProvider(new FileProvider());
        addDefaultProvider(new InputStreamProvider());
        addDefaultProvider("org.restlet.ext.jaxrs.internal.provider.JaxbElementProvider");
        addDefaultProvider("org.restlet.ext.jaxrs.internal.provider.JaxbProvider");

        // not yet tested
        addDefaultProvider("org.restlet.ext.jaxrs.internal.provider.MultipartProvider");

        addDefaultProvider(new ReaderProvider());
        addDefaultProvider(new StreamingOutputProvider());
        addDefaultProvider(new StringProvider());
        addDefaultProvider(new WwwFormFormProvider());
        addDefaultProvider(new WwwFormMmapProvider());
        addDefaultProvider(new SourceProvider());
        addDefaultProvider(new WebAppExcMapper());

        // Fall-back on the Restlet converter service
        addDefaultProvider(new ConverterProvider());
        addDefaultProvider("org.restlet.ext.jaxrs.internal.provider.JsonProvider");
    }

    /**
     * Obtains the object that will handle the request, see JAX-RS-Spec
     * (2008-04-16), section 3.7.2 "Request Matching", Part 2: "btain the object
     * that will handle the request"
     * 
     * @param rroRemPathAndMatchedPath
     * @throws WebApplicationException
     * @throws RequestHandledException
     * @throws RuntimeException
     */
    private ResObjAndRemPath obtainObject(
            RroRemPathAndMatchedPath rroRemPathAndMatchedPath)
            throws WebApplicationException, RequestHandledException {
        ResourceObject o = rroRemPathAndMatchedPath.rootResObj;
        RemainingPath u = rroRemPathAndMatchedPath.u;
        ResourceClass resClass = o.getResourceClass();
        CallContext callContext = tlContext.get();
        callContext.addForMatched(o.getJaxRsResourceObject(),
                rroRemPathAndMatchedPath.matchedUriPath);
        // Part 2
        for (;;) // (j)
        {
            // (a) If U is null or '/' go to step 3
            if (u.isEmptyOrSlash()) {
                return new ResObjAndRemPath(o, u);
            }
            // (b) Set C = class ofO,E = {}
            Collection<ResourceMethodOrLocator> eWithMethod = new ArrayList<ResourceMethodOrLocator>();
            // (c) and (d) Filter E: remove members do not match U or final
            // match not empty
            for (ResourceMethodOrLocator methodOrLocator : resClass
                    .getResourceMethodsAndLocators()) {
                PathRegExp pathRegExp = methodOrLocator.getPathRegExp();
                MatchingResult matchingResult = pathRegExp.match(u);
                if (matchingResult == null)
                    continue;
                if (matchingResult.getFinalCapturingGroup().isEmptyOrSlash())
                    eWithMethod.add(methodOrLocator);
                // the following is added by Stephan (is not in spec 2008-03-06)
                else if (methodOrLocator instanceof SubResourceLocator)
                    eWithMethod.add(methodOrLocator);
            }
            // (e) If E is empty -> HTTP 404
            if (eWithMethod.isEmpty())
                excHandler.resourceNotFound();// NICE (o.getClass(), u);
            // (f) and (g) sort E, use first member of E
            ResourceMethodOrLocator firstMeth = getFirstByNoOfLiteralCharsNoOfCapturingGroups(eWithMethod);

            PathRegExp rMatch = firstMeth.getPathRegExp();
            MatchingResult matchingResult = rMatch.match(u);

            addPathVarsToMap(matchingResult, callContext);

            // (h) When Method is resource method
            if (firstMeth instanceof ResourceMethod)
                return new ResObjAndRemPath(o, u);
            String matchedUriPart = matchingResult.getMatched();
            Object jaxRsResObj = o.getJaxRsResourceObject();
            callContext.addForMatched(jaxRsResObj, matchedUriPart);

            // (g) and (i)
            u = matchingResult.getFinalCapturingGroup();
            SubResourceLocator subResourceLocator = (SubResourceLocator) firstMeth;
            o = createSubResource(o, subResourceLocator, callContext);
            resClass = o.getResourceClass();
            // (j) Go to step 2a (repeat for)
        }
    }

    /**
     * Implementation of algorithm in JAX-RS-Spec (2008-04-16), Section 3.7.2
     * "Request Matching"
     * 
     * @return (Sub)Resource Method
     * @throws RequestHandledException
     * @throws WebApplicationException
     */
    private ResObjAndMeth requestMatching() throws RequestHandledException,
            WebApplicationException {
        Request restletRequest = tlContext.get().getRequest();
        // Part 1
        RemainingPath u = new RemainingPath(restletRequest.getResourceRef()
                .getRemainingPart());
        RroRemPathAndMatchedPath rrm = identifyRootResource(u);
        // Part 2
        ResObjAndRemPath resourceObjectAndPath = obtainObject(rrm);
        Representation entity = restletRequest.getEntity();
        // Part 3
        MediaType givenMediaType;
        if (entity != null)
            givenMediaType = entity.getMediaType();
        else
            givenMediaType = null;
        ResObjAndMeth method = identifyMethod(resourceObjectAndPath,
                givenMediaType);
        return method;
    }

    /**
     * Sets the Restlet that will handle the {@link Request}s, if no resource
     * method could be found.
     * 
     * @param noResMethodHandler
     *            the noResMethodHandler to set
     * @see #getNoResMethodHandler()
     * @see #setNoResourceClHandler(Restlet)
     * @see #setNoRootResClHandler(Restlet)
     * @see #attachDefault(Restlet)
     */
    public void setNoResMethodHandler(Restlet noResMethodHandler) {
        excHandler.setNoResMethodHandler(noResMethodHandler);
    }

    /**
     * Sets the Restlet that will handle the {@link Request}s, if no resource
     * class could be found. You could remove a given Restlet by set null here.<br>
     * If no Restlet is given here, status 404 will be returned.
     * 
     * @param noResourceClHandler
     *            the noResourceClHandler to set
     * @see #getNoResourceClHandler()
     * @see #setNoResMethodHandler(Restlet)
     * @see #setNoRootResClHandler(Restlet)
     * @see #attachDefault(Restlet)
     */
    public void setNoResourceClHandler(Restlet noResourceClHandler) {
        excHandler.setNoResourceClHandler(noResourceClHandler);
    }

    /**
     * Sets the Restlet that is called, if no root resource class could be
     * found. You could remove a given Restlet by set null here.<br>
     * If no Restlet is given here, status 404 will be returned.
     * 
     * @param noRootResClHandler
     *            the Restlet to call, if no root resource class could be found.
     * @see #getNoRootResClHandler()
     * @see #setNoResourceClHandler(Restlet)
     * @see #setNoResMethodHandler(Restlet)
     * @see #attachDefault(Restlet)
     */
    public void setNoRootResClHandler(Restlet noRootResClHandler) {
        excHandler.setNoRootResClHandler(noRootResClHandler);
    }

    /**
     * Sets the ObjectFactory for root resource class and provider
     * instantiation.
     * 
     * @param objectFactory
     *            the ObjectFactory for root resource class and provider
     *            instantiation.
     */
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        this.providers.setObjectFactory(objectFactory);
    }

    /**
     * Sets the {@link RoleChecker} to use.
     * 
     * @param roleChecker
     *            the roleChecker to set. Can be null, in which case the normal
     *            Restlet security API will be used.
     * @see RoleChecker
     * @see #getRoleChecker()
     * @deprecated Use {@link ClientInfo#getRoles()} instead
     */
    @Deprecated
    public void setRoleChecker(RoleChecker roleChecker) {
        this.roleChecker = roleChecker;
    }

    @Override
    public void start() throws Exception {
        providers.initAll();
        super.start();
    }
}