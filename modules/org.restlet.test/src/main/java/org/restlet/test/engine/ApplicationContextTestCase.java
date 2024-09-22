package org.restlet.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.test.RestletTestCase;

/**
 * Tests that when issuing internal calls, the application context is kept intact in the caller server resource.
 */
public class ApplicationContextTestCase extends RestletTestCase {

    public static class InternalApplication extends Application {

        @Override
        public Restlet createInboundRoot() {
            Router router = new Router();
            router.attach("/test", InternalResource.class);
            return router;
        }
    }

    public static class InternalResource extends ServerResource {
        @Get
        public String hello() {
            return "hello, world";
        }
    }

    public static class WebApiApplication extends Application {
        @Override
        public Restlet createInboundRoot() {
            Router router = new Router();
            router.attach("/test", WebApiResource.class);
            return router;
        }
    }

    public static class WebApiResource extends ServerResource {
        @Get
        public String hello() {
            // issuing internal calls
            new ClientResource("riap://component/internal/test").get();
            // returns the current application
            return Application.getCurrent().getClass().getSimpleName();
        }
    }

    private Component component;

    @BeforeEach
    protected void setUpEach() throws Exception {
        this.component = new Component();
        this.component.getServers().add(Protocol.HTTP, TEST_PORT);
        this.component.getDefaultHost().attach("/api", new WebApiApplication());
        this.component.getInternalRouter().attach("/internal", new InternalApplication());
        this.component.start();
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
    	this.component.stop();
    }

    @Test
    public void testApplicationContext() throws Exception {
        ClientResource res = new ClientResource("http://localhost:" + TEST_PORT + "/api/test");
        Representation rep = res.get(MediaType.TEXT_PLAIN);
        // following https://github.com/restlet/restlet-framework-java/issues/1317 fix,
        // should return "InternalApplication" since the current Application thread variable has not been cleared
        assertEquals("InternalApplication", rep.getText());
    }
}
