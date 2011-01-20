package org.restlet.test.ext.oauth.test;

import static org.junit.Assert.assertNotNull;

import org.restlet.test.ext.oauth.test.resources.OauthTestApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class ProviderTest {
    public static Component component;

    public static int serverPort = 8080;

    @BeforeClass
    public static void startServer() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, serverPort);
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.CLAP);
        component.getClients().add(Protocol.RIAP);
        component.getDefaultHost()
                .attach("/oauth", new OauthTestApplication(0));
        component.start();
    }

    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
    }

    // Bug in restlet HEAD handling

    @Ignore
    @Test
    public void testWebServerFlow() throws Exception {
        ClientResource cr = new ClientResource("http://localhost:" + serverPort
                + "/oauth/provider");
        Representation r = cr.head();
        assertNotNull(r);
        // assertEquals("Response text test",r.getText(), "TestSuccessful");
        // assertEquals("Response content type test",r.getMediaType(),
        // MediaType.TEXT_HTML);
    }
}
