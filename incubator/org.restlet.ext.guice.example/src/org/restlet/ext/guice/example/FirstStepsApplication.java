package org.restlet.ext.guice.example;

import static com.google.inject.name.Names.named;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.ext.guice.FinderFactory;
import org.restlet.ext.guice.RestletGuice;
import org.restlet.ext.guice.RestletGuiceModule;
import org.restlet.resource.Handler;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;

public class FirstStepsApplication extends Application implements Module {

    enum Mode {
        EXPLICIT_INJECTOR, AUTO_INJECTOR
    }

    private final Mode mode = Mode.EXPLICIT_INJECTOR;

    @Override
    public synchronized Restlet createRoot() {

        FinderFactory factory;
        switch (mode) {

        case EXPLICIT_INJECTOR: // (1) Use explicit Injector creation.

            factory = RestletGuice.createInjector(this).getInstance(
                    FinderFactory.class);
            break;

        case AUTO_INJECTOR: // (2) Use a special module that is also a
            // FinderFactory and
            // that automatically creates the Injector when needed.
            factory = new RestletGuiceModule(this);
            break;

        default:
            throw new IllegalStateException(
                    "No Injector creation mode specified.");
        }

        Router router = new Router(getContext());

        // Route /hello/resource to whatever is bound to ServerResource
        // annotated with @HelloWorld.
        router.attach("/hello/resource", factory.finderOf(Key.get(
                ServerResource.class, HelloWorld.class)));

        // Route /hello/handler to whatever is bound to Handler annotated with
        // @HelloWorld.
        router.attach("/hello/handler", factory.finderFor(Key.get(
                Handler.class, HelloWorld.class)));

        // Everything else goes here.
        router.attachDefault(DefaultResource.class);

        return router;
    }

    public void configure(Binder binder) {

        // These bindings are right next to the point where they are used, in
        // factory.finderOf(...),
        // but in practice the module configuration need not be in the same file
        // or even package
        // as the calls to finderOf.

        binder.bind(Handler.class).annotatedWith(HelloWorld.class).to(
                HelloWorldResource.class);

        binder.bind(ServerResource.class).annotatedWith(HelloWorld.class).to(
                HelloServerResource.class);

        binder.bindConstant()
                .annotatedWith(named(HelloWorldResource.HELLO_MSG)).to(
                        "Hello, Restlet 1.1 - Guice 1!");

        binder.bindConstant().annotatedWith(
                named(HelloServerResource.HELLO_MSG)).to(
                "Hello, Restlet 2.0 - Guice 1!");
    }
}
