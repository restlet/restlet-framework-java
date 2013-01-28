package org.restlet.ext.guice.example;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import org.restlet.ext.guice.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.*;

import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.*;


public class WrappedFinderTest {

    @Test public void testReturnsMessage() {
        ClientResource client = new ClientResource("http://localhost:8118");
        String msg = client.getChild("/hello", HelloResource.class).getMessage();
        assertEquals(HELLO_MSG, msg);
    }

    @Test public void testHiReturnsMessage() {
        ClientResource client = new ClientResource("http://localhost:8118");
        String msg = client.getChild("/hi", HelloResource.class).getMessage();
        assertEquals(HI_MSG, msg);
    }


    @Before public void createAndStartComponent() throws Exception {
        Injector injector = Guice.createInjector(
            new TestModule(),
            new SelfInjectingServerResourceModule()
        );
        component = injector.getInstance(MyComponent.class);
        component.start();
    }

    @After public void stopComponent() throws Exception {
        component.stop();
        component = null;
    }

    private volatile Component component;



    public static final class MyComponent extends Component {
        @Inject MyComponent(MyApp myApp) {
            getServers().add(Protocol.HTTP, 8118);
            getDefaultHost().attachDefault(myApp);
        }
    }

    public static final class MyApp extends ResourceInjectingApplication {
        @Override public Restlet createInboundRoot() {
            Router router = newRouter();
            router.setFinderClass(null);
            router.attach("/hello", HelloServerResource.class);
            router.attach("/hi", HiServerResource.class);
            return router;
        }
    }

    public interface HelloResource {
        @Get String getMessage();
    }

    public static class HiServerResource extends ServerResource implements HelloResource {
        @Override public String getMessage() {
            return HI_PREFIX + msg;
        }

        @Override protected void doInit() {
            System.out.println("Hi: before doInit: msg=" + msg);
            try {
                super.doInit();
            } finally {
                System.out.println("Hi: after doInit: msg=" + msg);
            }
        }

        @Inject @Named(HELLO_KEY) private String msg;
    }

    public static class HelloServerResource
            extends SelfInjectingServerResource implements HelloResource {

        @Override public String getMessage() {
            return msg;
        }

        @Override protected void doInit() {
            System.out.println("Hello: before doInit: msg=" + msg);
            try {
                super.doInit();
            } finally {
                System.out.println("Hello: after doInit: msg=" + msg);
            }
        }

        @Inject @Named(HELLO_KEY) private String msg;
    }

    static class TestModule extends AbstractModule {
        @Provides @Named(HELLO_KEY) String helloMessage() {
            return HELLO_MSG;
        }
        protected void configure() {}
    }

    static final String HELLO_KEY = "hello.message";
    static final String HELLO_MSG = "This resource was injected by Guice!";
    static final String HI_PREFIX = "Hi, there: ";
    static final String HI_MSG = HI_PREFIX + HELLO_MSG;
}
