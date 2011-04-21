package org.restlet.test.ext.oauth.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.oauth.experimental.DiscoverableFilter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.test.ext.oauth.test.resources.OauthDiscoverableTestApplication;

public class DicsoveryTest {
    public static Component component;

    public static int serverPort = 8080;

    public static OauthDiscoverableTestApplication client = new OauthDiscoverableTestApplication();

    @BeforeClass
    public static void startServer() throws Exception {
        Logger log = Context.getCurrentLogger();
        log.info("Starting oauth discovery test!");

        Server server = new Server(Protocol.HTTP, serverPort);
        component = new Component();
        component.getServers().add(server);
        component.getClients().add(Protocol.HTTP);

        component.getDefaultHost().attach("/test", client);

        component.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
    }

    @Test
    public void testDiscoverableResource() throws Exception {
        ClientResource cr = new ClientResource("http://localhost:" + serverPort
                + "/test/resource.disc");
        Representation r = cr.options();
        assertNotNull(r);
        assertEquals("Response content type test", r.getMediaType(),
                DiscoverableFilter.MEDIA_TYPE);
        cr.delete();
        cr.release();
        
        cr = new ClientResource("http://localhost:" + serverPort
                + "/test/resource.wadl");
        r = cr.options();
        assertNotNull(r);
        cr.release();
    }

}
