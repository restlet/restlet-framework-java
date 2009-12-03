package org.restlet.ext.guice.example;

import static com.google.inject.name.Names.named;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import org.restlet.ext.guice.DependencyInjection;
import org.restlet.ext.guice.RestletGuice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;

public class FirstStepsApplication extends Application implements Module {

    enum Mode {
        EXPLICIT_INJECTOR, AUTO_INJECTOR
    }

    private final Mode mode = Mode.EXPLICIT_INJECTOR;

    @Override
    public Restlet createInboundRoot() {

        DependencyInjection dependencyInjection;
        switch (mode) {

        case EXPLICIT_INJECTOR: // (1) Use explicit Injector creation.

            dependencyInjection = RestletGuice.createInjector(this).getInstance(DependencyInjection.class);
            break;

        case AUTO_INJECTOR: // (2) Use a special module that is also a DependencyInjection
                            //     and that automatically creates the Injector when needed.
            dependencyInjection = new RestletGuice.Module(this);
            break;

        default:
            throw new IllegalStateException(
                    "No Injector creation mode specified.");
        }

        Router router = new Router(getContext());

        // Route /hello/resource to whatever is bound to ServerResource
        // annotated with @HelloWorld.
        dependencyInjection.attach(router, "/hello/resource", ServerResource.class, HelloWorld.class);

        // Everything else goes here.
        dependencyInjection.attachDefault(router, DefaultResource.class);

        return router;
    }

    public void configure(Binder binder) {

        // These bindings are right next to the point where they are used, in
        // dependencyInjection.of(...), but in practice the module configuration
        // need not be in the same file or even package as the calls to DependencyInjection
        // methods.

        binder.bind(ServerResource.class)
            .annotatedWith(HelloWorld.class)
            .to(HelloServerResource.class);

        binder.bindConstant()
            .annotatedWith(named(HelloServerResource.HELLO_MSG))
            .to("Hello, Restlet 2.0 - Guice 1!");
    }
}
