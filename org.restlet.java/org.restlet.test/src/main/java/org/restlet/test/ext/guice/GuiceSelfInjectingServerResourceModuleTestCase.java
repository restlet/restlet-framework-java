/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.guice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.guice.SelfInjectingServerResource;
import org.restlet.ext.guice.SelfInjectingServerResourceModule;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.routing.Router;
import org.restlet.test.RestletTestCase;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;

public class GuiceSelfInjectingServerResourceModuleTestCase extends RestletTestCase {

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
            System.out.println("before doInit: msg=" + msg);
            try {
                super.doInit();
            } finally {
                System.out.println("after doInit: msg=" + msg);
            }
        }

        @Override
        public String getMessage() {
            return msg;
        }
    }

    private static class TestApplication extends Application {
        @Override
        public Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("/hello", HelloServerResource.class);
            return router;
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

    static final String HELLO_KEY = "hello.message";

    static final String HELLO_MSG = "This resource was injected by Guice!";

    private volatile Client client;

    private volatile Component component;

    @BeforeEach
    protected void setUpEach() throws Exception {
        Guice.createInjector(new TestModule(),
                new SelfInjectingServerResourceModule());

        this.client = new Client(Protocol.HTTP);

        if (component == null) {
            component = new Component();
            component.getServers().add(Protocol.HTTP, TEST_PORT);
            component.getDefaultHost().attachDefault(new TestApplication());
        }

        if (!this.component.isStarted()) {
            this.component.start();
        }

    }

    @AfterEach
    public void tearDownEach() throws Exception {
        this.client.stop();
        this.component.stop();
        this.component = null;
    }

    @Test
    public void testReturnsMessage() {
        ClientResource client = new ClientResource("http://localhost:"
                + TEST_PORT);
        client.accept(MediaType.TEXT_PLAIN);
        String msg = client.getChild("/hello", HelloResource.class)
                .getMessage();
        assertEquals(HELLO_MSG, msg);
    }
}
