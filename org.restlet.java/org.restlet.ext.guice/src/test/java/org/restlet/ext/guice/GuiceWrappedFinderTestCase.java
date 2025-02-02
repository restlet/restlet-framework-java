/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GuiceWrappedFinderTestCase {

    public interface HelloResource {
        @Get
        String getMessage();
    }

    public static class HelloServerResource extends SelfInjectingServerResource
            implements HelloResource {

        @Inject
        @Named(HELLO_KEY)
        private String msg;

        @Override
        protected void doInit() {
            System.out.println("Hello: before doInit: msg=" + msg);
            try {
                super.doInit();
            } finally {
                System.out.println("Hello: after doInit: msg=" + msg);
            }
        }

        @Override
        public String getMessage() {
            return msg;
        }
    }

    public static class HiServerResource extends ServerResource implements
            HelloResource {
        @Inject
        @Named(HELLO_KEY)
        private String msg;

        @Override
        protected void doInit() {
            System.out.println("Hi: before doInit: msg=" + msg);
            try {
                super.doInit();
            } finally {
                System.out.println("Hi: after doInit: msg=" + msg);
            }
        }

        @Override
        public String getMessage() {
            return _HI_PREFIX + msg;
        }
    }

    public static final class MyApp extends ResourceInjectingApplication {
        @Override
        public Restlet createInboundRoot() {
            Router router = newRouter();
            router.setFinderClass(null);
            router.attach("/hello", HelloServerResource.class);
            router.attach("/hi", HiServerResource.class);
            return router;
        }
    }

    public static final class MyComponent extends Component {
        private final Server server;

        @Inject
        MyComponent(MyApp myApp) {
            server = getServers().add(Protocol.HTTP, 0);
            getDefaultHost().attachDefault(myApp);
        }

        int getActualHttpServerPort() {
            return server.getActualPort();
        }
    }

    static class TestModule extends AbstractModule {
        protected void configure() {
        }

        @Provides
        @Named(HELLO_KEY)
        String helloMessage() {
            return HELLO_MSG;
        }
    }

    static final String _HI_PREFIX = "Hi, there: ";

    static final String HELLO_KEY = "hello.message";

    static final String HELLO_MSG = "This resource was injected by Guice!";

    static final String HI_MSG = _HI_PREFIX + HELLO_MSG;

    private volatile Client client;

    private volatile MyComponent component;
    private volatile int testPort;

    @BeforeEach
    protected void setUpEach() throws Exception {
        Injector injector = Guice.createInjector(new TestModule(),
                new SelfInjectingServerResourceModule());

        this.client = new Client(Protocol.HTTP);

        if (component == null) {
            component = injector.getInstance(MyComponent.class);
        }

        this.component.start();
        testPort = component.getActualHttpServerPort();
    }

    @AfterEach
    public void tearDownEach() throws Exception {
        this.client.stop();
        this.component.stop();
        this.component = null;
    }

    @Test
    public void testHiReturnsMessage() {
        ClientResource client = new ClientResource("http://localhost:" + testPort);
        client.accept(MediaType.TEXT_PLAIN);
        String msg = client.getChild("/hi", HelloResource.class).getMessage();
        assertEquals(HI_MSG, msg);
    }

    @Test
    public void testReturnsMessage() {
        ClientResource client = new ClientResource("http://localhost:" + testPort);
        client.accept(MediaType.TEXT_PLAIN);
        String msg = client.getChild("/hello", HelloResource.class).getMessage();
        assertEquals(HELLO_MSG, msg);
    }
}
