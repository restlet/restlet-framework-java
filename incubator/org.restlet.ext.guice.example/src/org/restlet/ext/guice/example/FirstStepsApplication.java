package org.restlet.ext.guice.example;

import org.restlet.ext.guice.*;
import org.restlet.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import com.google.inject.*;
import static com.google.inject.name.Names.named;

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
            factory = new FinderFactoryModule(this);
            break;

        default:
            throw new IllegalStateException(
                    "No Injector creation mode specified.");
        }

        Router router = new Router(getContext());

        // Route /hello/resource to whatever is bound to ServerResource
        // annotated with @HelloWorld.
        router.attach("/hello/resource", factory.finderOf(Key.get(
                ServerResource.class, HelloWorld.class), getContext()));

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
