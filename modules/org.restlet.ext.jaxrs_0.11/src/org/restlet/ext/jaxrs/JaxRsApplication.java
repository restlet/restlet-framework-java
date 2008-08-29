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
package org.restlet.ext.jaxrs;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Finder;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.service.TunnelService;

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
 * <li>If you need authentication, set a {@link Guard} and perhaps an
 * {@link RoleChecker}, see {@link #setGuard(Guard)} or
 * {@link #setAuthentication(Guard, RoleChecker)}.</li>
 * </ul>
 * At least add the JaxRsApplication to a {@link Component}.
 * </p>
 * <p>
 * <i>The JAX-RS extension as well as the JAX-RS specification are currently
 * under development. You should use this extension only for experimental
 * purpose.</i> <br>
 * For further information see <a href="https://jsr311.dev.java.net/">Java
 * Service Request 311</a>.
 * </p>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Stephan Koops
 */
public class JaxRsApplication extends org.restlet.Application {

    /** Indicates if any {@link ApplicationConfig} is attached yet */
    private volatile boolean appConfigAttached = false;

    /** The {@link Guard} to use. May be null. */
    private volatile Guard guard;

    /** The {@link JaxRsRestlet} to use. */
    private volatile JaxRsRestlet jaxRsRestlet;

    /**
     * Creates an new JaxRsApplication.
     * 
     * @see #JaxRsApplication(Context)
     */
    public JaxRsApplication() {
        this(null);
    }

    /**
     * Creates an new JaxRsApplication, without any access control. Attach
     * JAX-RS-{@link Application}s by using {@link #add(Application)}.<br>
     * If a method calls {@link SecurityContext#isUserInRole(String)}, status
     * 500 is returned to the client, see {@link RoleChecker#REJECT_WITH_ERROR}.
     * Use {@link #setGuard(Guard)} and {@link #setRoleChecker(RoleChecker)} or
     * {@link #setAuthentication(Guard, RoleChecker)}, to set access control.<br>
     * 
     * @param context
     *                The application's dedicated context based on the protected
     *                parent component's context.
     */
    public JaxRsApplication(Context context) {
        super(context);
        getTunnelService().setExtensionsTunnel(false);
        this.jaxRsRestlet = new JaxRsRestlet(context, getMetadataService());
    }

    /**
     * <p>
     * Attaches a JAX-RS {@link Application} to this JaxRsApplication.<br>
     * The providers are available for all root resource classes provided to
     * this JaxRsApplication. If you won't mix them, instantiate another
     * JaxRsApplication.
     * </p>
     * <p>
     * If the given JAX-RS Application is the first attached Application, the
     * default extension mappings are remove and replaced by the given, see
     * {@link TunnelService}.
     * </p>
     * 
     * @param appConfig
     *                Contains the classes to load as root resource classes and
     *                as providers. Invalid root resource classes and provider
     *                classes are ignored, according to JAX-RS specification.<br>
     *                The JAX-RS class {@link javax.ws.rs.ApplicationConfig}
     *                will be renamed to {@link javax.ws.rs.Application} in the
     *                next JAX-RS release.
     * @return true, if all resource classes and providers could be added, or
     *         false if not.
     * @throws IllegalArgumentException
     *                 if the appConfig is null.
     * @see #add(Application, boolean)
     */
    public boolean add(Application appConfig) throws IllegalArgumentException {
        return add(appConfig, true);
    }

    /**
     * Attaches a JAX-RS {@link Application} to this Application.<br>
     * The providers are available for all root resource classes provided to
     * this JaxRsApplication. If you won't mix them, instantiate another
     * JaxRsApplication.
     * 
     * @param appConfig
     *                Contains the classes to load as root resource classes and
     *                as providers. Invalid root resource classes and provider
     *                classes are ignored, according to JAX-RS specification.<br>
     *                The JAX-RS class {@link javax.ws.rs.ApplicationConfig}
     *                will be renamed to {@link javax.ws.rs.Application} in the
     *                next JAX-RS release.
     * @param clearMetadataIfFirst
     *                If this flag is true and the given ApplicationConfig is
     *                the first attached ApplicationConfig, the default
     *                extension mappings are remove an replaced by the given,
     *                see {@link TunnelService}
     * @return true, if all resource classes and providers could be added, or
     *         false if not.
     * @throws IllegalArgumentException
     *                 if the appConfig is null.
     * @see #add(Application)
     */
    public boolean add(Application appConfig, boolean clearMetadataIfFirst)
            throws IllegalArgumentException {
        if (appConfig == null) {
            throw new IllegalArgumentException(
                    "The ApplicationConfig must not be null");
        }
        if (clearMetadataIfFirst && !this.appConfigAttached) {
            getMetadataService().clearExtensions();
        }
        final JaxRsRestlet jaxRsRestlet = this.jaxRsRestlet;
        final Set<Class<?>> classes = appConfig.getClasses();
        final Set<Object> singletons = appConfig.getSingletons();
        boolean everythingFine = true;
        if (singletons != null) {
            for (final Object singleton : singletons) {
                // LATER test: check, if a singelton also available in the
                // classes is ignored
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
        this.appConfigAttached = true;
        return everythingFine;
    }

    @Override
    public Restlet createRoot() {

        Restlet restlet = this.jaxRsRestlet;

        if (this.guard != null) {
            this.guard.setNext(restlet);
            restlet = this.guard;
        }

        return restlet;
    }

    /**
     * Returns the Guard
     * 
     * @return the Guard
     */
    public Guard getGuard() {
        return this.guard;
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
     * Returns the current RoleChecker
     * 
     * @return the current RoleChecker
     */
    public RoleChecker getRoleChecker() {
        return this.jaxRsRestlet.getRoleChecker();
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
     * <i>This method is planned!</i><br>
     * It should return {@link Route}s to attach them to a {@link Router}.<br>
     * The {@link JaxRsRestlet} does not allow other Restlets directly beside
     * it. Example: {@link JaxRsRestlet} handles http://host/path1. So you can't
     * directly add another Restlet handling http://host/path2. When addings
     * this {@link Route}s to the main {@link Router} for "host" you can add
     * another {@link Restlet} (e.g. a {@link Directory} or {@link Finder}) for
     * other pathes.
     * 
     * @return an unmodifiable {@link List} of {@link Route}s.
     * @deprecated planned, but not yet implemented
     */
    @Deprecated
    @SuppressWarnings("unused")
    private List<Route> getRoutes() {
        throw new NotYetImplementedException();
        // NICE JaxRsApplication.getRoutes() : List<Route>
    }

    /**
     * Sets the objects to check the authentication. The {@link Guard} checks
     * the username and password (e.g.), the {@link RoleChecker} manages the
     * role management for the JAX-RS extension.
     * 
     * @param guard
     *                the Guard to use.
     * @param roleChecker
     *                the RoleChecker to use
     * @see #setGuard(Guard)
     * @see #setRoleChecker(RoleChecker)
     */
    public void setAuthentication(Guard guard, RoleChecker roleChecker) {
        setGuard(guard);
        setRoleChecker(roleChecker);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.jaxRsRestlet.setContext(context);
    }

    /**
     * Sets the {@link Guard} to use. It should typically use the
     * {@link Context} of this application.<br>
     * The new one is ignored, after the root Restlet is created (see
     * {@link #createRoot()}.
     * 
     * @param guard
     *                the Guard to use.
     * @see #setAuthentication(Guard, RoleChecker)
     */
    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    /**
     * Sets the ObjectFactory for root resource class and provider
     * instantiation.
     * 
     * @param objectFactory
     *                the ObjectFactory for root resource class and provider
     *                instantiation.
     */
    public void setObjectFactory(ObjectFactory objectFactory) {
        this.jaxRsRestlet.setObjectFactory(objectFactory);
    }

    /**
     * Sets the {@link RoleChecker} to use.<br>
     * If you give an RoleChecker, you should also give a Guard.
     * 
     * @param roleChecker
     * @see #setAuthentication(Guard, RoleChecker)
     * @see #setGuard(Guard)
     */
    public void setRoleChecker(RoleChecker roleChecker) {
        this.jaxRsRestlet.setRoleChecker(roleChecker);
    }
}