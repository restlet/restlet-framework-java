package org.restlet.test.ext.oauth.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthForm;
import org.restlet.ext.oauth.OAuthHelper;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.internal.OAuthUtils;
import org.restlet.ext.openid.OpenIdFormFrowarder;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.ext.oauth.test.resources.OauthClientTestApplication;
import org.restlet.test.ext.oauth.test.resources.OauthComboTestApplication;
import org.restlet.test.ext.oauth.test.resources.OauthProtectedTestApplication;
import org.restlet.test.ext.oauth.test.resources.OauthTestApplication;
import org.restlet.util.Series;

public class AuthorizationServerTest {
    public static Component component;

    // Use for http test when debugging
    public static int serverPort = 8080;

    public static final String prot = "http";

    // public static int serverPort = 8443;
    // public static final String prot = "https";

    public static OauthClientTestApplication client = new OauthClientTestApplication();

    @BeforeClass
    public static void startServer() throws Exception {

        // org.restlet.ext.httpclient.internal.IgnoreCookieSpecFactory i;

        Logger log = Context.getCurrentLogger();
        log.info("Starting server test!");

        // SSL global configuration
        String keystore = ClassLoader.getSystemResource("localhost.jks")
                .getPath();
        System.setProperty("javax.net.ssl.trustStore", keystore);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.trustStorePassword", "testpass");

        // Server server = new Server(Protocol.HTTPS, serverPort);
        Server server = new Server(new Context(), Protocol.HTTP, serverPort);
        // Strange workaround for the server to not hang.
        server.getContext().getParameters().add("maxQueued", "0");

        // server.setName("localhost.local");
        // server.setAddress("localhost.local");
        component = new Component();
        component.getServers().add(server);
        component.getClients().add(Protocol.HTTP);
        component.getClients().add(Protocol.HTTPS);
        // component.getClients().add(Protocol.CLAP);
        component.getClients().add(Protocol.RIAP);
        component.getDefaultHost()
                .attach("/oauth", new OauthTestApplication(0)); // unlimited
                                                                // token life
        component.getDefaultHost().attach("/client", client);
        component.getDefaultHost().attach("/server",
                new OauthProtectedTestApplication());
        component.getDefaultHost().attach("/combo",
                new OauthComboTestApplication(0)); // unlimited token life

        // Setup TLS
        Series<Parameter> parameters = server.getContext().getParameters();
        parameters.add("keystorePath", keystore);
        parameters.add("keystorePassword", "testpass");
        parameters.add("keyPassword", "testpass");
        parameters.add("keystoreType", "JKS");
        parameters.add("sslServerAlias", "localhost");

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

    @Test
    public void testWebServerFlow() throws Exception {
        client.clearUser();
        assertNull(client.getToken());
        ClientResource cr = new ClientResource(prot + "://localhost:"
                + serverPort + "/client/webclient");
        Representation r = cr.get();
        assertNotNull(r);
        r = OpenIdFormFrowarder.handleFormRedirect(r, cr);
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        assertNotNull(client.getToken());
        cr.release();
    }

    @Test
    public void testTokenReuse() throws IOException {
        // Query test
        assertNotNull(client.getToken());
        Reference ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", client.getToken());
        ClientResource cr = new ClientResource(ref);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();

        // Testing Authorization header
        ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/protected");
        cr = new ClientResource(ref);
        ChallengeResponse challengeResponse = new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH);
        challengeResponse.setRawValue(client.getToken());
        cr.setChallengeResponse(challengeResponse);
        r = cr.get();
        assertNotNull(r);
        text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();

        // Testing form
        ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/protected");
        cr = new ClientResource(ref);
        OAuthForm form = new OAuthForm(client.getToken());
        form.add("foo", "bar");
        r = cr.post(form.getWebRepresentation());
        assertNotNull(r);
        text = r.getText();
        assertEquals("Response text test", text, "Dummy");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_PLAIN);
        cr.release();
    }

    @Test
    public void testUserAgentFlow() throws IOException {
        // Same Uri as the web client
        String callbackUri = prot + "://localhost:" + serverPort + "/";
        OAuthUser user = OAuthUtils.userAgent(client.getOauthParameters(),
                callbackUri, null);
        assertNotNull(user);

        // Try to use the token...
        Reference ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        ClientResource cr = new ClientResource(ref);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
    }

    @Test
    public void testNoneFlow() throws IOException {
        OAuthUser user = OAuthUtils.noneFlow(client.getOauthParameters());
        assertNotNull(user);

        // Try to use the token...
        Reference ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        ClientResource cr = new ClientResource(ref);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();

        // None flow scoped test
        ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/scoped");
        ref.addQueryParameter("oauth_token", client.getToken());
        cr = new ClientResource(ref);
        r = cr.get();
        assertNotNull(r);
        text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
    }

    @Test
    public void testComboServer() throws IOException {
        // Same Uri as the web client
        String callbackUri = prot + "://localhost:" + serverPort + "/";
        String baseRef = prot + "://localhost:" + serverPort + "/combo/";
        OAuthParameters params = new OAuthParameters("1234567890",
                "1234567890", baseRef, "foo bar");
        OAuthUser user = OAuthUtils.userAgent(params, callbackUri, null);
        assertNotNull(user);

        // Try to use the token...
        Reference ref = new Reference(prot + "://localhost:" + serverPort
                + "/combo/protected");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        ClientResource cr = new ClientResource(ref);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
    }

    @Test
    public void testScopedResource() throws IOException {
        // Query test
        assertNotNull(client.getToken());
        Reference ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/scoped");
        ref.addQueryParameter("oauth_token", client.getToken());
        ClientResource cr = new ClientResource(ref);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
    }

    @Test
    public void testWrongScopedResource() throws IOException {
        // Same Uri as the web client
        String callbackUri = prot + "://localhost:" + serverPort + "/";
        OAuthParameters right = client.getOauthParameters();
        OAuthParameters wrong = new OAuthParameters(right.getClientId(),
                right.getClientSecret(), right.getBaseRef().toString(),
                "one two");
        OAuthUser user = OAuthUtils.userAgent(wrong, callbackUri, null);
        assertNotNull(user);

        // Try to use the token...
        Reference ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/scoped");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        ClientResource cr = new ClientResource(ref);
        try {
            cr.get();
        } catch (ResourceException re) { // Should be invalidated
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, re.getStatus());
            ChallengeRequest challenge = cr.getChallengeRequests().get(0);
            assertNotNull(challenge);
            String error = challenge.getParameters().getFirstValue("error");
            assertEquals("Checking error code",
                    OAuthError.ErrorCode.insufficient_scope.name(), error);
        }
        cr.release();
    }

    @Test
    public void testPasswordFlow() throws IOException {
        OAuthUser user = OAuthUtils.passwordFlow(client.getOauthParameters(),
                OauthTestApplication.TEST_USER, OauthTestApplication.TEST_PASS);
        assertNotNull(user);

        // Try to use the token...
        Reference ref = new Reference(prot + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        ClientResource cr = new ClientResource(ref);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();

        // Wrong username test
        try {
            user = OAuthUtils.passwordFlow(client.getOauthParameters(),
                    "sowrong", OauthTestApplication.TEST_PASS);
        } catch (ResourceException re) { // Should be invalidated
            assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, re.getStatus());
        }

        // Wrong pasword test
        try {
            user = OAuthUtils.passwordFlow(client.getOauthParameters(),
                    OauthTestApplication.TEST_USER, "sowrong");
        } catch (ResourceException re) { // Should be invalidated
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, re.getStatus());
        }
    }

    @Ignore
    @Test
    public void testValidationSingleConnection() throws Exception {
        ClientResource cr = new ClientResource(prot + "://localhost:"
                + serverPort + "/oauth/validate");
        JSONObject body = new JSONObject();
        body.put("access_token", client.getToken());
        body.put("uri", "http://test.com");

        for (int i = 0; i < 20; i++) {
            JsonRepresentation jr = new JsonRepresentation(body);
            Representation r = cr.post(jr);
            assertNotNull(r);
            assertEquals("Response content type test", r.getMediaType(),
                    MediaType.APPLICATION_JSON);
            r.release();
        }
        cr.release();
    }

    @Ignore
    @Test
    public void testValidationNewConnection() throws Exception {
        for (int i = 0; i < 20; i++) {
            ClientResource cr = new ClientResource(prot + "://localhost:"
                    + serverPort + "/oauth/validate");
            JSONObject body = new JSONObject();
            body.put("access_token", client.getToken());
            body.put("uri", "http://test.com");
            JsonRepresentation jr = new JsonRepresentation(body);
            Representation r = cr.post(jr);
            assertNotNull(r);
            assertEquals("Response content type test", r.getMediaType(),
                    MediaType.APPLICATION_JSON);
            r.release();
            cr.release();
        }
    }

    @Ignore
    @Test
    public void testPostMultiple() throws IOException {
        ClientResource cr = new ClientResource(prot + "://localhost:"
                + serverPort + "/client/unprotected");
        for (int i = 0; i < 50; i++) {

            Form f = new Form();
            f.add("foo", "bar");
            Representation r = cr.post(f.getWebRepresentation());
            assertNotNull(r);

            System.out.println("Body = " + r.getText());
            // r.release();

        }
        cr.release();
    }
}
