/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaxrs;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.security.Authenticator;

/**
 * <p>
 * This is the main class to be used for the instantiation of a JAX-RS runtime
 * environment.
 * </p>
 * <p>
 * To set up a JAX-RS runtime environment you should instantiate a
 * {@link JaxRsApplication#JaxRsApplication(Context)}.
 * <ul>
 * <li>Add your {@link Application}(s) by calling {@link #add(Application)}.</li>
 * <li>If you need authentication, set a {@link Authenticator} see
 * {@link #setGuard(Authenticator)}.</li>
 * </ul>
 * At least add the JaxRsApplication to a {@link Component}.
 * </p>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Stephan Koops
 */
public class JaxRsApplication extends org.restlet.Application {

    /**
     * The {@link org.restlet.security.Authenticator}) to use. May be null.
     */
    private volatile Authenticator authenticator;

    /** The {@link JaxRsRestlet} to use. */
    private volatile JaxRsRestlet jaxRsRestlet;

    /**
     * Creates an new JaxRsApplication.
     * 
     * @see #JaxRsApplication(Context)
     */
    public JaxRsApplication() {
        this((Context) null);
    }

    /**
     * Creates an new JaxRsApplication. Attach JAX-RS-{@link Application}s by
     * using {@link #add(Application)}.
     * 
     * @param context
     *            The application's dedicated context based on the protected
     *            parent component's context.
     */
    public JaxRsApplication(Context context) {
        super(context);
        this.jaxRsRestlet = new JaxRsRestlet(context, getMetadataService());
    }

    /**
     * 
     * @param appConfig
     * @throws IllegalArgumentException
     */
    public JaxRsApplication(javax.ws.rs.core.Application appConfig)
            throws IllegalArgumentException {
        add(appConfig);
    }

    /**
     * <p>
     * Attaches a JAX-RS {@link Application} to this JaxRsApplication.<br>
     * The providers are available for all root resource classes provided to
     * this JaxRsApplication. If you won't mix them, instantiate another
     * JaxRsApplication.
     * </p>
     * 
     * @param appConfig
     *            Contains the classes to load as root resource classes and as
     *            providers. Invalid root resource classes and provider classes
     *            are ignored, according to JAX-RS specification.
     * @return true, if all resource classes and providers could be added, or
     *         false at least one could not be added. Exceptions were logged.
     * @throws IllegalArgumentException
     *             if the given appConfig is null.
     */
    public boolean add(Application appConfig) throws IllegalArgumentException {
        if (appConfig == null) {
            throw new IllegalArgumentException(
                    "The ApplicationConfig must not be null");
        }

        final JaxRsRestlet jaxRsRestlet = this.jaxRsRestlet;
        final Set<Class<?>> classes = appConfig.getClasses();
        final Set<Object> singletons = appConfig.getSingletons();
        boolean everythingFine = true;

        if (singletons != null) {
            for (final Object singleton : singletons) {
                // LATER test: check, if a singelton is also available in the
                // classes -> ignore or whatever
                if (singleton != null
                        && !classes.contains(singleton.getClass())) {
                    everythingFine &= jaxRsRestlet.addSingleton(singleton);
                }
            }
        }

        if (classes != null) {
            for (final Class<?> clazz : classes) {
                everythingFine &= jaxRsRestlet.addClass(clazz);
            }
        }

        return everythingFine;
    }

    @Override
    public Restlet createInboundRoot() {
        Restlet restlet = this.jaxRsRestlet;

        if (this.authenticator != null) {
            this.authenticator.setNext(restlet);
            restlet = this.authenticator;
        }

        return restlet;
    }

    /**
     * Returns the {@link Authenticator}.
     * 
     * @return the {@link Authenticator}.
     */
    public Authenticator getAuthenticator() {
        return this.authenticator;
    }

    /**
     * Returns the {@link Authenticator}.
     * 
     * @return the {@link Authenticator}.
     * @deprecated Use {@link #getAuthenticator()} instead.
     */
    @Deprecated
    public Authenticator getGuard() {
        return this.authenticator;
    }

    /**
     * Returns the used {@link JaxRsRestlet}.
     * 
     * @return the used {@link JaxRsRestlet}.
     */
    public JaxRsRestlet getJaxRsRestlet() {
        return this.jaxRsRestlet;
    }

    /**
     * Returns the ObjectFactory for root resource class and provider
     * instantiation, if given.
     * 
     * @return the ObjectFactory for root resource class and provider
     *         instantiation, if given.
     */
    public ObjectFactory getObjectFactory() {
        return this.jaxRsRestlet.getObjectFactory();
    }

    /**
     * Returns an unmodifiable set with the attached root resource classes.
     * 
     * @return an unmodifiable set with the attached root resource classes.
     */
    public Collection<Class<?>> getRootResources() {
        return this.jaxRsRestlet.getRootResourceClasses();
    }

    /**
     * Returns an unmodifiable set of supported URIs (relative to this
     * Application).
     * 
     * @return an unmodifiable set of supported URIs (relative).
     */
    public Collection<String> getRootUris() {
        return this.jaxRsRestlet.getRootUris();
    }

    /**
     * Adds the given applications to the available applications.
     * 
     * @param apps
     */
    public void setApplications(Collection<javax.ws.rs.core.Application> apps) {
        for (Application app : apps) {
            add(app);
        }
    }

    /**
     * Sets the {@link Authenticator} to use. This should be called before the
     * root Restlet is created.
     * <p>
     * This replaced the guard set via {@link #setGuard(Authenticator)}.
     * 
     * @param authenticator
     *            The {@link Authenticator} to use.
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.jaxRsRestlet.setContext(context);
    }

    /**
     * Sets the {@link Authenticator} to use. This should be called before the
     * root Restlet is created.
     * <p>
     * This replaced the guard set via {@link #setGuard(Authenticator)}.
     * 
     * @param authenticator
     *            The {@link Authenticator} to use.
     * @deprecated Use {@link #setAuthenticator(Authenticator)} instead.
     */
    @Deprecated
    public void setGuard(Authenticator authenticator) {
        this.authenticator = authenticator;
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
        this.jaxRsRestlet.setObjectFactory(objectFactory);
    }

}
