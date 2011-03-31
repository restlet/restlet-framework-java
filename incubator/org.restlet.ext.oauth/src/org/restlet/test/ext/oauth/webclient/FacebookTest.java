package org.restlet.test.ext.oauth.webclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.ext.oauth.OAuthHelper;
import org.restlet.ext.oauth.OAuthUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.test.ext.oauth.test.resources.FbClientTestApplication;
import org.restlet.util.Series;

public class FacebookTest {
    public static Component component;

    public static int serverPort = 8080;

    public static final String prot = "http";

    public static FbClientTestApplication client = new FbClientTestApplication();

    @BeforeClass
    public static void startServer() throws Exception {
        Server server = new Server(Protocol.HTTP, serverPort);
        component = new Component();
        component.getServers().add(server);
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.CLAP);
        component.getClients().add(Protocol.RIAP);
        component.getDefaultHost().attach("/client", client);
        // server.getContext().getParameters().add("maxThreads", "30");
        // component.getDefaultHost();
        component.start();

        List<AuthenticatorHelper> authenticators = Engine.getInstance()
                .getRegisteredAuthenticators();
        for (AuthenticatorHelper helper : authenticators) {
            System.out.println("Found default auth helper : " + helper);
        }
        authenticators.add(new OAuthHelper());

        System.out.println(Engine.getInstance().getRegisteredClients().get(0));
    }

    @AfterClass
    public static void stopServer() throws Exception {
        component.stop();
    }

    @Ignore
    @Test
    public void testWebServerFlow() throws Exception {

    }

    @Ignore
    @Test
    public void testAgentFlow() throws IOException {
        // Same Uri as the web client
        String callbackUri = prot + "://localhost:" + serverPort
                + "/webclient/";
        Series<CookieSetting> cookies = OAuthUtils.fbUserAgent(
                client.getOauthParameters(), callbackUri, "changeme",
                "changeme");
        assertNotNull(cookies);

        // Try to use the token...
        Reference ref = new Reference("https://graph.facebook.com/me");
        ClientResource cr = new ClientResource(ref);
        for (Cookie c : cookies) {
            cr.getCookies().add(c);
        }
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
    }

}
