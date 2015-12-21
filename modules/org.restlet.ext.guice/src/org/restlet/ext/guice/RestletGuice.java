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

package org.restlet.ext.guice;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;

/**
 * Guice dependency injection for Restlet.
 * 
 * @author Tim Peierls
 */
public class RestletGuice {

    /**
     * A Guice module that implements {@link FinderFactory}. On first use of the
     * methods of this facility, if the module hasn't been used to create an
     * {@link Injector}, this module creates its own Injector.
     */
    public static class Module extends AbstractModule implements FinderFactory {

        class KeyFinder extends Finder {
            private final Class<?> targetClass;

            KeyFinder(Type type) {
                this.targetClass = (Class<?>) type;
            }

            @Override
            public final Context getContext() {
                return getInjector().getInstance(Context.class);
            }

            protected final Injector getInjector() {
                Injector inj = injector;
                if (inj == null) {
                    synchronized (RestletGuice.Module.this) {
                        inj = injector;
                        if (inj == null) {
                            injector = inj = Guice
                                    .createInjector(RestletGuice.Module.this);
                        }
                    }
                }
                return inj;
            }

            public final Class<? extends ServerResource> getTargetClass() {

                // If the key type is a subtype of ServerResource, return it.
                Class<ServerResource> src = ServerResource.class;
                if (src != null && targetClass != null
                        && src.isAssignableFrom(targetClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends ServerResource> result = (Class<? extends ServerResource>) targetClass;
                    return result;
                }

                // Otherwise, we can't in general determine the true target
                // type, so we revert to the superclass implementation.
                // Since we used the no-arg Finder constructor, it will return
                // null unless someone has explicitly set a target class. This
                // is only relevant to the use of the Router.detach(Class<?>
                // targetClass) method; it implies that we cannot detach routes
                // that target dependency-injected resources attached as
                // non-ServerResource types without explicitly setting a target
                // class type. This seems like a *very* minor restriction.
                return super.getTargetClass();
            }
        }

        class ServerResourceKeyFinder extends KeyFinder {
            private final Key<?> serverResourceKey;

            ServerResourceKeyFinder(Key<?> serverResourceKey) {
                super(serverResourceKey.getTypeLiteral().getType());
                this.serverResourceKey = serverResourceKey;
            }

            @Override
            public ServerResource create(Request request, Response response) {
                try {
                    return ServerResource.class.cast(getInjector().getInstance(
                            serverResourceKey));
                } catch (ClassCastException ex) {
                    String msg = String.format(
                            "Must bind %s to ServerResource (or subclass)",
                            serverResourceKey);
                    throw new ProvisionException(msg, ex);
                }
            }
        }

        //
        // FinderFactory methods
        //

        private static ThreadLocal<Boolean> alreadyBound = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return false;
            }
        };

        @Inject
        private volatile Injector injector;

        private final Iterable<? extends com.google.inject.Module> modules;

        /**
         * Creates a RestletGuice.Module that will install the given modules.
         */
        public Module(com.google.inject.Module... modules) {
            this.modules = asList(modules);
        }

        /**
         * Creates a RestletGuice.Module that will install the given modules.
         */
        public Module(Iterable<? extends com.google.inject.Module> modules) {
            this.modules = modules;
        }

        /**
         * If this module is used in more than one injector, we clear the
         * thread-local boolean that prevents binding more than once in the same
         * thread.
         */
        @Inject
        private void clearAlreadyBound() {
            alreadyBound.set(false);
        }

        @Override
        protected final void configure() {

            if (injector != null) {
                throw new IllegalStateException(
                        "can't reconfigure with existing Injector");
            }

            if (!alreadyBound.get()) {
                alreadyBound.set(true);

                bind(FinderFactory.class).toInstance(this);

                bind(Application.class).toProvider(newApplicationProvider());
                bind(Context.class).toProvider(newContextProvider());
                bind(Request.class).toProvider(newRequestProvider());
                bind(Response.class).toProvider(newResponseProvider());
            }

            for (com.google.inject.Module module : modules) {
                install(module);
            }
        }

        public Finder finder(Class<?> cls) {
            return new ServerResourceKeyFinder(Key.get(cls));
        }

        public Finder finder(Class<?> cls, Class<? extends Annotation> qualifier) {
            return new ServerResourceKeyFinder(Key.get(cls, qualifier));
        }

        /**
         * Creates a {@link Provider}r for the current {@link Application}.
         * Override to use a custom Application provider.
         * 
         * @return A {@link Provider} for the current {@link Application}.
         */
        protected Provider<Application> newApplicationProvider() {
            return new Provider<Application>() {
                public Application get() {
                    return Application.getCurrent();
                }
            };
        }

        /**
         * Creates a {@link Provider} for the current {@link Context}. Override
         * to use a custom Context provider.
         * 
         * @return A {@link Provider} for the current {@link Context}.
         */
        protected Provider<Context> newContextProvider() {
            return new Provider<Context>() {
                public Context get() {
                    return Context.getCurrent();
                }
            };
        }

        /**
         * Creates a {@link Provider} for the current {@link Request}. Override
         * to use a custom Request provider.
         * 
         * @return A {@link Provider} for the current {@link Request}.
         */
        protected Provider<Request> newRequestProvider() {
            return new Provider<Request>() {
                public Request get() {
                    return Request.getCurrent();
                }
            };
        }

        /**
         * Creates a {@link Provider} for the current {@link Response}. Override
         * to use a custom Response provider.
         * 
         * @return A {@link Provider} for the current {@link Response}.
         */
        protected Provider<Response> newResponseProvider() {
            return new Provider<Response>() {
                public Response get() {
                    return Response.getCurrent();
                }
            };
        }
    }

    /**
     * Creates an instance of {@link Injector} from the given modules with
     * {@link FinderFactory} bound to an implementation that uses the injector's
     * bindings to create Finder instances.
     * 
     * @param modules
     *            The list of modules.
     * @return The injector for the list of modules.
     */
    public static Injector createInjector(com.google.inject.Module... modules) {
        return injectorFor(null, new Module(modules));
    }

    /**
     * Creates an instance of {@link Injector} from the given modules with
     * {@link FinderFactory} bound to an implementation that uses the injector's
     * bindings to create Finder instances.
     * 
     * @param modules
     *            The collection of modules.
     * @return The injector for the list of modules.
     */
    public static Injector createInjector(
            Iterable<com.google.inject.Module> modules) {
        return injectorFor(null, new Module(modules));
    }

    /**
     * Creates an instance of {@link Injector} in the given {@link Stage} from
     * the given modules with {@link FinderFactory} bound to an implementation
     * that uses the injector's bindings to create {@link Finder} instances.
     * 
     * @param stage
     *            The {@link Stage}.
     * @param modules
     *            The list of modules.
     * @return The injector for the list of modules in the given stage.
     */
    public static Injector createInjector(Stage stage,
            com.google.inject.Module... modules) {
        return injectorFor(stage, new Module(modules));
    }

    /**
     * Creates an instance of {@link Injector} in the given {@link Stage} from
     * the given modules with {@link FinderFactory} bound to an implementation
     * that uses the injector's bindings to create {@link Finder} instances.
     * 
     * @param stage
     *            The {@link Stage}.
     * @param modules
     *            The list of modules.
     * @return The injector for the list of modules in the given stage.
     */

    public static Injector createInjector(Stage stage,
            Iterable<com.google.inject.Module> modules) {
        return injectorFor(stage, new Module(modules));
    }

    private static Injector injectorFor(Stage stage, Module rootModule) {
        if (stage == null) {
            return Guice.createInjector(rootModule);
        } else {
            return Guice.createInjector(stage, rootModule);
        }
    }
}
