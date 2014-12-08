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

package org.restlet.test.ext.oauth;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.restlet.ext.oauth.OAuthResourceDefs.ACCESS_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.CLIENT_ID;
import static org.restlet.ext.oauth.OAuthResourceDefs.CLIENT_SECRET;
import static org.restlet.ext.oauth.OAuthResourceDefs.EXPIRES_IN;
import static org.restlet.ext.oauth.OAuthResourceDefs.REFRESH_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.SCOPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.TOKEN_TYPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.TOKEN_TYPE_BEARER;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.AccessTokenClientResource;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class AccessTokenClientResourceTest extends OAuthTestBase {

    public static class StubApplication extends Application {

        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("/token1", StubServerResource1.class);
            router.attach("/token2", StubServerResource2.class);
            router.attach("/token3", StubServerResource3.class);
            router.attach("/token4", StubServerResource4.class);
            return router;
        }
    }

    @BeforeClass
    public static void setupStub() throws Exception {
        // Setup Restlet
        component = new Component();
        component.getClients().add(Protocol.HTTP);
        component.getServers().add(Protocol.HTTP, 8080);
        component.getDefaultHost().attach("/oauth", new StubApplication());
        component.start();
    }

    @AfterClass
    public static void destroyStub() throws Exception {
        component.stop();
    }

    /**
     * Test case 1: Successful Response(Pattern A) with Client Auth (Body).
     */
    public static class StubServerResource1 extends ServerResource {

        @Post
        public Representation requestToken(Representation input)
                throws JSONException {
            Form form = new Form(input);
            assertThat(form.getFirstValue(CLIENT_ID), is(STUB_CLIENT_ID));
            assertThat(form.getFirstValue(CLIENT_SECRET),
                    is(STUB_CLIENT_SECRET));
            JSONObject response = new JSONObject();
            response.put(ACCESS_TOKEN, "foo");
            response.put(TOKEN_TYPE, TOKEN_TYPE_BEARER);
            return new JsonRepresentation(response);
        }
    }

    @Test
    public void testCase1() throws OAuthException, IOException, JSONException {
        AccessTokenClientResource tokenResource = new AccessTokenClientResource(
                new Reference(baseURI, "/oauth/token1"));
        tokenResource.setClientCredentials(STUB_CLIENT_ID, STUB_CLIENT_SECRET);
        tokenResource.setAuthenticationMethod(null);
        Token token = tokenResource.requestToken(new OAuthParameters());
        assertThat(token.getAccessToken(), is("foo"));
        assertThat(token.getTokenType(), is(TOKEN_TYPE_BEARER));
        try {
            int exp = token.getExpirePeriod();
            fail("expires_in is included: " + exp);
        } catch (IllegalStateException ex) {
            assertTrue(true);
        }
        assertNull(token.getRefreshToken());
        assertThat(token.getScope(), is(arrayWithSize(0)));
    }

    /**
     * Test case 2: Successful Response(Pattern B) with Client Auth (Basic).
     */
    public static class StubServerResource2 extends ServerResource {

        @Post
        public Representation requestToken(Representation input)
                throws JSONException {
            ChallengeResponse cr = getChallengeResponse();
            assertThat(cr.getScheme(), is(equalTo(ChallengeScheme.HTTP_BASIC)));
            assertThat(cr.getIdentifier(), is(equalTo(STUB_CLIENT_ID)));
            assertThat(cr.getSecret(),
                    is(equalTo(STUB_CLIENT_SECRET.toCharArray())));
            Form form = new Form(input);
            assertThat(form.getFirstValue(CLIENT_ID), is(nullValue()));
            assertThat(form.getFirstValue(CLIENT_SECRET), is(nullValue()));
            JSONObject response = new JSONObject();
            response.put(ACCESS_TOKEN, "bar");
            response.put(TOKEN_TYPE, TOKEN_TYPE_BEARER);
            response.put(EXPIRES_IN, 3600);
            response.put(REFRESH_TOKEN, "qux");
            response.put(SCOPE, "a b");
            return new JsonRepresentation(response);
        }
    }

    @Test
    public void testCase2() throws OAuthException, IOException, JSONException {
        AccessTokenClientResource tokenResource = new AccessTokenClientResource(
                new Reference(baseURI, "/oauth/token2"));
        tokenResource.setClientCredentials(STUB_CLIENT_ID, STUB_CLIENT_SECRET);
        // tokenResource.setAuthenticationMethod(ChallengeScheme.HTTP_BASIC);
        Token token = tokenResource.requestToken(new OAuthParameters());
        assertThat(token.getAccessToken(), is("bar"));
        assertThat(token.getTokenType(), is(TOKEN_TYPE_BEARER));
        assertThat(token.getExpirePeriod(), is(3600));
        assertThat(token.getRefreshToken(), is("qux"));
        assertThat(token.getScope(), is(arrayContainingInAnyOrder("a", "b")));
    }

    /**
     * Test case 3: Error Response. (with HTTP Code 200)
     */
    public static class StubServerResource3 extends ServerResource {

        @Post
        public Representation requestToken(Representation input)
                throws JSONException {
            OAuthException oex = new OAuthException(OAuthError.invalid_client,
                    "Invalid Client", null);
            return new JsonRepresentation(oex.createErrorDocument());
        }
    }

    @Test(expected = OAuthException.class)
    public void testCase3() throws OAuthException, IOException, JSONException {
        AccessTokenClientResource tokenResource = new AccessTokenClientResource(
                new Reference(baseURI, "/oauth/token3"));
        tokenResource.setClientCredentials(STUB_CLIENT_ID, STUB_CLIENT_SECRET);
        tokenResource.requestToken(new OAuthParameters());
        fail("OAuthException is expected.");
    }

    /**
     * Test case 4: Error Response. (with HTTP Code 400)
     */
    public static class StubServerResource4 extends ServerResource {

        @Post
        public Representation requestToken(Representation input)
                throws JSONException {
            OAuthException oex = new OAuthException(OAuthError.invalid_client,
                    "Invalid Client", null);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new JsonRepresentation(oex.createErrorDocument());
        }
    }

    @Test(expected = OAuthException.class)
    public void testCase4() throws OAuthException, IOException, JSONException {
        AccessTokenClientResource tokenResource = new AccessTokenClientResource(
                new Reference(baseURI, "/oauth/token4"));
        tokenResource.setClientCredentials(STUB_CLIENT_ID, STUB_CLIENT_SECRET);
        tokenResource.requestToken(new OAuthParameters());
        fail("OAuthException is expected.");
    }
}
