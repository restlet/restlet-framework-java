package org.restlet.ext.guice.example;

import static com.google.inject.name.Names.named;

import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.ext.guice.FinderFactory;
import org.restlet.ext.guice.RestletGuice;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

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

        enum Mode {
            EXPLICIT_INJECTOR, AUTO_INJECTOR
        }

        final Mode mode = Mode.EXPLICIT_INJECTOR;

        final MainModule mainModule = new MainModule();

        @Override
        public Restlet createInboundRoot() {

            FinderFactory di = null;
            switch (mode) {

            case EXPLICIT_INJECTOR: // (1) Use explicit Injector creation.

                Injector injector = RestletGuice.createInjector(mainModule);
                di = injector.getInstance(FinderFactory.class);
                break;

            case AUTO_INJECTOR: // (2) Use a special module that is also a
                                // DependencyInjection
                // and that automatically creates the Injector when needed.
                di = new RestletGuice.Module(mainModule);
                break;
            }

            if (di == null) {
                throw new IllegalStateException(
                        "No Injector creation mode specified.");
            }

            Router router = new Router(getContext());

            // Route HELLO_PATH to whatever is bound to ServerResource annotated
            // with @HelloWorld.
            router.attach(HELLO_PATH, di.finder(ServerResource.class,
                    HelloWorld.class));

            // Everything else goes to DefaultResource.
            router.attachDefault(di.finder(DefaultResource.class));

            return router;
        }
    }

    public static class DefaultResource extends ServerResource {

        @Inject
        DefaultResource(@Named(HELLO_PATH_Q) String path) {
            this.path = path;
        }

        @Get
        public String represent() {
            return "Default resource, try " + path;
        }

        private final String path;
    }

    public static class HelloServerResource extends ServerResource {

        @Inject
        public HelloServerResource(@Named(HELLO_MSG_Q) String msg) {
            this.msg = msg;
        }

        @Get
        public String asString() {
            return String.format("%d: %s", count.incrementAndGet(), msg);
        }

        private final String msg;

        private static final AtomicInteger count = new AtomicInteger();
    }

    static final String HELLO_PATH = "/hello";

    static final String HELLO_PATH_Q = "hello.path";

    static final String HELLO_MSG_Q = "hello.message";

    static class MainModule extends AbstractModule {
        protected void configure() {
            bind(ServerResource.class).annotatedWith(HelloWorld.class).to(
                    HelloServerResource.class);

            bindConstant().annotatedWith(named(HELLO_MSG_Q)).to(
                    "Hello, Restlet 2.0 - Guice 2.0!");

            bindConstant().annotatedWith(named(HELLO_PATH_Q)).to(HELLO_PATH);
        }
    }
}
