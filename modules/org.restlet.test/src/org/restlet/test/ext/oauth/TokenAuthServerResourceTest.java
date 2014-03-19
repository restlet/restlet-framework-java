/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.oauth;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.restlet.ext.oauth.OAuthResourceDefs.ACCESS_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.ERROR;
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
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.TokenAuthServerResource;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.TokenManager;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class TokenAuthServerResourceTest extends OAuthTestBase {

    private Reference tokenAuthURI = new Reference(baseURI, "/oauth/token_auth");

    public static class StubApplication extends Application {

        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            getContext().getAttributes().put(TokenManager.class.getName(),
                    new StubTokenManager());
            router.attach("/token_auth", TokenAuthServerResource.class);
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
     * Test case 1: Invalid request(No token_type).
     */
    @Test
    public void testCase1() throws JSONException, IOException {
        ClientResource resource = new ClientResource(tokenAuthURI);
        JSONObject request = new JSONObject();
        request.put(ACCESS_TOKEN, STUB_ACCESS_TOKEN);
        JSONObject response = new JsonRepresentation(
                resource.post(new JsonRepresentation(request))).getJsonObject();
        assertTrue(response.has(ERROR));
    }

    /**
     * Test case 2: Invalid request(Unsupported token_type).
     */
    @Test
    public void testCase2() throws JSONException, IOException {
        ClientResource resource = new ClientResource(tokenAuthURI);
        JSONObject request = new JSONObject();
        request.put(TOKEN_TYPE, "buz");
        request.put(ACCESS_TOKEN, STUB_ACCESS_TOKEN);
        JSONObject response = new JsonRepresentation(
                resource.post(new JsonRepresentation(request))).getJsonObject();
        assertTrue(response.has(ERROR));
    }

    /**
     * Test case 3: Invalid request(Invalid token).
     */
    @Test
    public void testCase3() throws JSONException, IOException {
        ClientResource resource = new ClientResource(tokenAuthURI);
        JSONObject request = new JSONObject();
        request.put(TOKEN_TYPE, TOKEN_TYPE_BEARER);
        request.put(ACCESS_TOKEN, "buz");
        JSONObject response = new JsonRepresentation(
                resource.post(new JsonRepresentation(request))).getJsonObject();
        assertTrue(response.has(ERROR));
    }

    /**
     * Test case 4: Valid request(Success).
     */
    @Test
    public void testCase4() throws JSONException, IOException {
        ClientResource resource = new ClientResource(tokenAuthURI);
        JSONObject request = new JSONObject();
        request.put(TOKEN_TYPE, TOKEN_TYPE_BEARER);
        request.put(ACCESS_TOKEN, STUB_ACCESS_TOKEN);
        JSONObject response = new JsonRepresentation(
                resource.post(new JsonRepresentation(request))).getJsonObject();
        assertFalse(response.has(ERROR));
        assertThat(response.getString(USERNAME), is(STUB_USERNAME));
        assertThat(Scopes.parseScope(response.getString(SCOPE)),
                is(arrayContainingInAnyOrder("a", "b")));
    }
}
