/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */
package org.restlet.test.ext.oauth;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.restlet.ext.oauth.OAuthResourceDefs.ACCESS_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.CODE;
import static org.restlet.ext.oauth.OAuthResourceDefs.EXPIRES_IN;
import static org.restlet.ext.oauth.OAuthResourceDefs.GRANT_TYPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.PASSWORD;
import static org.restlet.ext.oauth.OAuthResourceDefs.REFRESH_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.SCOPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.TOKEN_TYPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.TOKEN_TYPE_BEARER;
import static org.restlet.ext.oauth.OAuthResourceDefs.USERNAME;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.AccessTokenServerResource;
import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.ext.oauth.internal.ResourceOwnerManager;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.TokenManager;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.User;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class AccessTokenServerResourceTest extends OAuthTestBase {

    private Reference tokenURI = new Reference(baseURI, "/oauth/token");

    public static class StubApplication extends Application {

        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            getContext().getAttributes().put(TokenManager.class.getName(),
                    new StubTokenManager());
            getContext().getAttributes().put(ClientManager.class.getName(),
                    new StubClientManager());
            getContext().getAttributes().put(
                    ResourceOwnerManager.class.getName(),
                    new StubResourceOwnerManager());

            DummyAuthenticator authenticator = new DummyAuthenticator(
                    getContext());
            authenticator.setNext(AccessTokenServerResource.class);

            router.attach("/token", authenticator);

            return router;
        }
    }

    /**
     * Dummy authenticator to pretend that the client is authenticated.
     */
    private static class DummyAuthenticator extends Filter {

        public DummyAuthenticator(Context context) {
            super(context);
        }

        @Override
        protected int beforeHandle(Request request, Response response) {
            request.getClientInfo().setUser(new User(STUB_CLIENT_ID));
            return CONTINUE;
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

    @Test
    public void testAuthCodeFlow() throws JSONException, IOException {
        Form request = new Form();
        request.add(GRANT_TYPE, GrantType.authorization_code.name());
        request.add(CODE, STUB_CODE);
        ClientResource resource = new ClientResource(tokenURI);
        JSONObject response = new JsonRepresentation(resource.post(request
                .getWebRepresentation())).getJsonObject();
        assertThat(resource.getResponseCacheDirectives(),
                is(contains(CacheDirective.noStore())));
        assertThat(response.getString(TOKEN_TYPE), is(TOKEN_TYPE_BEARER));
        assertThat(response.getString(ACCESS_TOKEN), is(STUB_ACCESS_TOKEN));
        assertThat(response.getString(REFRESH_TOKEN), is(STUB_REFRESH_TOKEN));
        assertThat(response.get(EXPIRES_IN), is(instanceOf(Number.class)));
        assertThat(Scopes.parseScope(response.getString(SCOPE)),
                is(arrayContainingInAnyOrder("a", "b")));
    }

    @Test
    public void testPasswordFlow() throws JSONException, IOException {
        Form request = new Form();
        request.add(GRANT_TYPE, GrantType.password.name());
        request.add(USERNAME, STUB_USERNAME);
        request.add(PASSWORD, STUB_PASSWORD);
        request.add(SCOPE, "a b");
        ClientResource resource = new ClientResource(tokenURI);
        JSONObject response = new JsonRepresentation(resource.post(request
                .getWebRepresentation())).getJsonObject();
        assertThat(resource.getResponseCacheDirectives(),
                is(contains(CacheDirective.noStore())));
        assertThat(response.getString(TOKEN_TYPE), is(TOKEN_TYPE_BEARER));
        assertThat(response.getString(ACCESS_TOKEN), is(STUB_ACCESS_TOKEN));
        assertThat(response.getString(REFRESH_TOKEN), is(STUB_REFRESH_TOKEN));
        assertThat(response.get(EXPIRES_IN), is(instanceOf(Number.class)));
        assertFalse(response.has(SCOPE));
    }

    @Test
    public void testClientFlow() throws JSONException, IOException {
        Form request = new Form();
        request.add(GRANT_TYPE, GrantType.client_credentials.name());
        request.add(SCOPE, "a b");
        ClientResource resource = new ClientResource(tokenURI);
        JSONObject response = new JsonRepresentation(resource.post(request
                .getWebRepresentation())).getJsonObject();
        assertThat(resource.getResponseCacheDirectives(),
                is(contains(CacheDirective.noStore())));
        assertThat(response.getString(TOKEN_TYPE), is(TOKEN_TYPE_BEARER));
        assertThat(response.getString(ACCESS_TOKEN), is(STUB_ACCESS_TOKEN));
        assertThat(response.getString(REFRESH_TOKEN), is(STUB_REFRESH_TOKEN));
        assertThat(response.get(EXPIRES_IN), is(instanceOf(Number.class)));
        assertFalse(response.has(SCOPE));
    }

    @Test
    public void testRefreshFlow() throws JSONException, IOException {
        Form request = new Form();
        request.add(GRANT_TYPE, GrantType.refresh_token.name());
        request.add(REFRESH_TOKEN, STUB_REFRESH_TOKEN);
        request.add(SCOPE, "a b");
        ClientResource resource = new ClientResource(tokenURI);
        JSONObject response = new JsonRepresentation(resource.post(request
                .getWebRepresentation())).getJsonObject();
        assertThat(resource.getResponseCacheDirectives(),
                is(contains(CacheDirective.noStore())));
        assertThat(response.getString(TOKEN_TYPE), is(TOKEN_TYPE_BEARER));
        assertThat(response.getString(ACCESS_TOKEN), is(STUB_ACCESS_TOKEN));
        assertThat(response.getString(REFRESH_TOKEN), is(STUB_REFRESH_TOKEN));
        assertThat(response.get(EXPIRES_IN), is(instanceOf(Number.class)));
        assertFalse(response.has(SCOPE));
    }
}
