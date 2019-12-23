package org.restlet.test.engine;

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
import org.restlet.service.ConnectorService;
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

    public static class WebApiConnectorService extends ConnectorService {
        private boolean called = false;

        @Override
        public void beforeSend(Representation entity) {
            super.beforeSend(entity);
            this.called = true;
        }

        public boolean wasCalled() {
            return called;
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
    private WebApiConnectorService connectorService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.component = new Component();
        this.component.getServers().add(Protocol.HTTP, TEST_PORT);

        this.connectorService = new WebApiConnectorService();
        WebApiApplication webApiApplication = new WebApiApplication();
        webApiApplication.setConnectorService(this.connectorService);
        component.getDefaultHost().attach("/api", webApiApplication);
        component.getInternalRouter().attach("/internal", new InternalApplication());

        component.start();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
    }

    public void testApplicationContext() throws Exception {
        ClientResource res = new ClientResource("http://localhost:" + TEST_PORT + "/api/test");
        Representation rep = res.get(MediaType.TEXT_PLAIN);
        assertEquals("WebApiApplication", rep.getText());
    }

    public void testCurrentApplicationNotClearedBeforeSend() throws Exception {
        ClientResource res = new ClientResource("http://localhost:" + TEST_PORT + "/api/test");
        Representation rep = res.get(MediaType.TEXT_PLAIN);
        assertTrue(this.connectorService.wasCalled());
    }
}
