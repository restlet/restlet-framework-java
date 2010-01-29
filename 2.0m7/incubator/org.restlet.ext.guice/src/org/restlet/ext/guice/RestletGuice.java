package org.restlet.ext.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Stage;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import static java.util.Arrays.asList;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.util.ServerList;


/**
 * Guice dependency injection for Restlet.
 */
public class RestletGuice {

    /**
     * Creates an injector from the given modules with DependencyInjection
     * bound to an implementation that uses the injector's bindings to create
     * Finder instances and handle calls to attach and setNext.
     */
    public static Injector createInjector(com.google.inject.Module... modules) {
        return injectorFor(null, new Module(modules));
    }

    /**
     * Creates an injector in the given Stage from the given modules with DependencyInjection
     * bound to an implementation that uses the injector's bindings to create
     * Finder instances and handle calls to attach and setNext.
     */
    public static Injector createInjector(Stage stage, com.google.inject.Module... modules) {
        return injectorFor(stage, new Module(modules));
    }

    /**
     * Creates an injector from the given modules with DependencyInjection
     * bound to an implementation that uses the injector's bindings to create
     * Finder instances and handle calls to attach and setNext.
     */
    public static Injector createInjector(Iterable<com.google.inject.Module> modules) {
        return injectorFor(null, new Module(modules));
    }

    /**
     * Creates an injector in the given Stage from the given modules with DependencyInjection
     * bound to an implementation that uses the injector's bindings to create
     * Finder instances and handle calls to attach and setNext.
     */
    public static Injector createInjector(Stage stage, Iterable<com.google.inject.Module> modules) {
        return injectorFor(stage, new Module(modules));
    }

    private static Injector injectorFor(Stage stage, Module rootModule) {
        if (stage == null) {
            return Guice.createInjector(rootModule);
        } else {
            return Guice.createInjector(stage, rootModule);
        }
    }

    /**
     * A Guice module implements DependencyInjection.
     * On first use of the methods of this facility, if the module hasn't
     * been used to create an Injector, this module creates its own Injector.
     */
    public static class Module extends AbstractModule implements DependencyInjection {

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


        //
        // DependencyInjection methods
        //

        public Finder inject(Class<? extends ServerResource> cls) {
            return new ServerResourceKeyFinder(Key.get(cls));
        }

        public Finder inject(Class<? extends ServerResource> cls, Class<? extends Annotation> qualifier) {
            return new ServerResourceKeyFinder(Key.get(cls, qualifier));
        }


        @Override protected final void configure() {

            if (injector != null) {
                throw new IllegalStateException("can't reconfigure with existing Injector");
            }

            if (!alreadyBound.get()) {
                alreadyBound.set(true);

                bind(DependencyInjection.class)
                    .toInstance(this);

                bind(Application.class)
                    .toProvider(newApplicationProvider());
                bind(Context.class)
                    .toProvider(newContextProvider());
                bind(Request.class)
                    .toProvider(newRequestProvider());
                bind(Response.class)
                    .toProvider(newResponseProvider());
            }

            for (com.google.inject.Module module : modules) {
                install(module);
            }
        }

        /**
         * Creates a Provider for the Application.
         * Override to use a custom Application provider.
         */
        protected Provider<Application> newApplicationProvider() {
            return new Provider<Application>() {
                public Application get() {
                    return Application.getCurrent();
                }
            };
        }

        /**
         * Creates a Provider for the Context.
         * Override to use a custom Context provider.
         */
        protected Provider<Context> newContextProvider() {
            return new Provider<Context>() {
                public Context get() {
                    return Context.getCurrent();
                }
            };
        }


        /**
         * Creates a Provider for the Request.
         * Override to use a custom Request provider.
         */
        protected Provider<Request> newRequestProvider() {
            return new Provider<Request>() {
                public Request get() {
                    return Request.getCurrent();
                }
            };
        }


        /**
         * Creates a Provider for the Response.
         * Override to use a custom Response provider.
         */
        protected Provider<Response> newResponseProvider() {
            return new Provider<Response>() {
                public Response get() {
                    return Response.getCurrent();
                }
            };
        }


        class KeyFinder extends Finder {
            private final Class<?> targetClass;

            @SuppressWarnings("unchecked")
            KeyFinder(Type type) {
                this.targetClass = (Class<?>) type;
            }

            @Override public final Context getContext() {
                return getInjector().getInstance(Context.class);
            }

            public final Class<?> getTargetClass() {
                return this.targetClass;
            }

            protected final Injector getInjector() {
                Injector inj = injector;
                if (inj == null) {
                    synchronized (RestletGuice.Module.this) {
                        inj = injector;
                        if (inj == null) {
                            System.err.println("Automatically creating injector.");
                            injector = inj = Guice.createInjector(RestletGuice.Module.this);
                        }
                    }
                }
                return inj;
            }
        }

        class ServerResourceKeyFinder extends KeyFinder {
            private final Key<? extends ServerResource> serverResourceKey;

            ServerResourceKeyFinder(Key<? extends ServerResource> serverResourceKey) {
                super(serverResourceKey.getTypeLiteral().getType());
                this.serverResourceKey = serverResourceKey;
            }

            @Override public ServerResource create(Request request, Response response) {
                return getInjector().getInstance(serverResourceKey);
            }
        }


        private final Iterable<? extends com.google.inject.Module> modules;
        @Inject private volatile Injector injector;

        /**
         * If this module is used in more than one injector, we clear the thread-local
         * boolean that prevents binding more than once in the same thread.
         */
        @SuppressWarnings("unused")
        @Inject private void clearAlreadyBound() {
            alreadyBound.set(false);
        }

        private static ThreadLocal<Boolean> alreadyBound = new ThreadLocal<Boolean>() {
            @Override protected Boolean initialValue() {
                return false;
            }
        };
    }
}
