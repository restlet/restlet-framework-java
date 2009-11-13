package org.restlet.ext.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;

import java.lang.reflect.Type;
import static java.util.Arrays.asList;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.Handler;
import org.restlet.resource.ServerResource;

/**
 * A Guice module that doubles as a factory for creating Finders that look up
 * Handler or Resource instances from a given class or Guice Key. On first use
 * of these Finders, if the module hasn't been used to create an Injector, this
 * module creates its own Injector.
 */
public class FinderFactoryModule extends AbstractModule implements
        FinderFactory {

    /**
     * Creates a FinderFactoryModule that will install the given modules.
     */
    public FinderFactoryModule(Module... modules) {
        this.modules = asList(modules);
    }

    /**
     * Creates a FinderFactoryModule that will install the given modules.
     */
    public FinderFactoryModule(Iterable<? extends Module> modules) {
        this.modules = modules;
    }

    //
    // FinderFactory methods
    //

    public Finder finderOf(Key<? extends ServerResource> key, Context context) {
        return new ServerResourceKeyFinder(key, context);
    }

    public Finder finderOf(Class<? extends ServerResource> cls, Context context) {
        return new ServerResourceKeyFinder(Key.get(cls), context);
    }

    public Finder finderFor(Key<? extends Handler> key) {
        return new HandlerKeyFinder(key);
    }

    public Finder finderFor(Class<? extends Handler> cls) {
        return new HandlerKeyFinder(Key.get(cls));
    }

    @Override
    protected final void configure() {

        if (injector != null) {
            throw new IllegalStateException(
                    "can't reconfigure with existing Injector");
        }

        if (!alreadyBound.get()) {
            alreadyBound.set(true);

            bind(Application.class).toProvider(newApplicationProvider());
            bind(Context.class).toProvider(newContextProvider());
            bind(Request.class).toProvider(newRequestProvider());
            bind(Response.class).toProvider(newResponseProvider());
            bind(FinderFactory.class).toInstance(this);
        }

        for (Module module : modules) {
            install(module);
        }
    }

    /**
     * Creates a Provider for the Application. Override to use a custom
     * Application provider.
     */
    protected Provider<Application> newApplicationProvider() {
        return new Provider<Application>() {
            public Application get() {
                return Application.getCurrent();
            }
        };
    }

    /**
     * Creates a Provider for the Context. Override to use a custom Context
     * provider.
     */
    protected Provider<Context> newContextProvider() {
        return new Provider<Context>() {
            public Context get() {
                return Context.getCurrent();
            }
        };
    }

    /**
     * Creates a Provider for the Request. Override to use a custom Request
     * provider.
     */
    protected Provider<Request> newRequestProvider() {
        return new Provider<Request>() {
            public Request get() {
                return Request.getCurrent();
            }
        };
    }

    /**
     * Creates a Provider for the Response. Override to use a custom Response
     * provider.
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

        KeyFinder(Type type, Context context) {
            super(context);
            this.targetClass = (Class<?>) type;
        }

        KeyFinder(Type type) {
            this(type, null);
        }

        public final Class<?> getTargetClass() {
            return this.targetClass;
        }

        protected final Injector getInjector() {
            Injector inj = injector;
            if (inj == null) {
                synchronized (FinderFactoryModule.this) {
                    inj = injector;
                    if (inj == null) {
                        inj = Guice.createInjector(FinderFactoryModule.this);
                    }
                }
            }
            return inj;
        }
    }

    class HandlerKeyFinder extends KeyFinder {
        private final Key<? extends Handler> handlerKey;

        HandlerKeyFinder(Key<? extends Handler> handlerKey) {
            super(handlerKey.getTypeLiteral().getType());
            this.handlerKey = handlerKey;
        }

        @Override
        protected Handler createTarget(Request request, Response response) {
            return getInjector().getInstance(handlerKey);
        }
    }

    class ServerResourceKeyFinder extends KeyFinder {
        private final Key<? extends ServerResource> serverResourceKey;

        ServerResourceKeyFinder(
                Key<? extends ServerResource> serverResourceKey, Context context) {
            super(serverResourceKey.getTypeLiteral().getType(), context);
            this.serverResourceKey = serverResourceKey;
        }

        @Override
        public ServerResource create(Request request, Response response) {
            return getInjector().getInstance(serverResourceKey);
        }
    }

    private final Iterable<? extends Module> modules;

    @Inject
    private volatile Injector injector;

    /**
     * If this module is used in more than one injector, we clear the
     * thread-local boolean that prevents binding more than once in the same
     * thread.
     */
    @SuppressWarnings("unused")
    @Inject
    private void clearAlreadyBound() {
        alreadyBound.set(false);
    }

    private static ThreadLocal<Boolean> alreadyBound = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
}
