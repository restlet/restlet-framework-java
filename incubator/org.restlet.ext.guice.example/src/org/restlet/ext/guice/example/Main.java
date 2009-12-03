package org.restlet.ext.guice.example;

import static com.google.inject.name.Names.named;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import org.restlet.ext.guice.DependencyInjection;
import org.restlet.ext.guice.RestletGuice;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;


public class Main {

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getDefaultHost().attach(new MainApp());
        component.start();
    }


    static class MainApp extends Application {

        enum Mode { EXPLICIT_INJECTOR, AUTO_INJECTOR }

        final Mode mode = Mode.EXPLICIT_INJECTOR;

        final MainModule mainModule = new MainModule();

        @Override public Restlet createInboundRoot() {

            DependencyInjection dependencyInjection = null;
            switch (mode) {

            case EXPLICIT_INJECTOR: // (1) Use explicit Injector creation.

                Injector injector = RestletGuice.createInjector(mainModule);
                dependencyInjection = injector.getInstance(DependencyInjection.class);
                break;

            case AUTO_INJECTOR: // (2) Use a special module that is also a DependencyInjection
                                //     and that automatically creates the Injector when needed.
                dependencyInjection = new RestletGuice.Module(mainModule);
                break;
            }

            if (dependencyInjection == null) {
                throw new IllegalStateException("No Injector creation mode specified.");
            }

            Router router = new Router(getContext());

            // Route /hello to whatever is bound to ServerResource annotated with @HelloWorld.
            dependencyInjection.attach(router, HELLO_PATH, ServerResource.class, HelloWorld.class);

            // Everything else goes here.
            dependencyInjection.attachDefault(router, DefaultResource.class);

            return router;
        }
    }


    public static class DefaultResource extends ServerResource {
        @Get public String represent() {
            return "Default resource, try " + HELLO_PATH;
        }
    }

    public static class HelloServerResource extends ServerResource {

        private static final AtomicInteger count = new AtomicInteger();

        @Inject public HelloServerResource(@Named(HELLO_MSG) String msg) {
            this.msg = msg;
        }

        @Get public String asString() {
            return String.format("%d: %s", count.incrementAndGet(), msg);
        }

        private final String msg;
    }

    static final String HELLO_MSG = "hello.message.qualifier.name";
    static final String HELLO_PATH = "/hello";

    static class MainModule extends AbstractModule {
        protected void configure() {
            bind(ServerResource.class)
                .annotatedWith(HelloWorld.class)
                .to(HelloServerResource.class);

            bindConstant()
                .annotatedWith(named(HELLO_MSG))
                .to("Hello, Restlet 2.0 - Guice 2.0!");
        }
    }
}
