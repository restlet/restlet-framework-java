package org.restlet.ext.guice.example;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.resource.*;
import org.restlet.routing.*;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.guice.SelfInjectingServerResourceModule;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.inject.*;

import org.junit.*;
import org.junit.runner.*;
import static org.junit.Assert.*;


public class SelfInjectingServerResourceModuleTest extends Application {

    @Test public void testReturnsMessage() {
        ClientResource client = new ClientResource("http://localhost:8118");
        String msg = client.getChild("/hello", HelloResource.class).getMessage();
        assertEquals(HELLO_MSG, msg);
    }

    @Before public void createInjector() {
        Guice.createInjector(
            new TestModule(),
            new SelfInjectingServerResourceModule()
        );
    }

    @Before public void startComponent() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, 8118);
        component.getDefaultHost().attachDefault(this);
        component.start();
    }

    @After public void stopComponent() throws Exception {
        component.stop();
    }

    private volatile Component component;


    @Override public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/hello", HelloServerResource.class);
        return router;
    }

    public interface HelloResource {
        @Get String getMessage();
    }

    public static class HelloServerResource
            extends SelfInjectingServerResource implements HelloResource {

        @Override public String getMessage() {
            return msg;
        }

        @Override protected void doInit() {
            System.out.println("before doInit: msg=" + msg);
            try {
                super.doInit();
            } finally {
                System.out.println("after doInit: msg=" + msg);
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
}
