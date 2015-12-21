/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.guice;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
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

public class GuiceSelfInjectingServerResourceModuleTestCase extends
        RestletTestCase {

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

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

    @Override
    public void tearDown() throws Exception {
        this.client.stop();
        this.component.stop();
        this.component = null;
        super.tearDown();
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
