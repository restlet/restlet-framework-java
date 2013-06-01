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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.restlet.ext.oauth.OAuthResourceDefs.ACCESS_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.SCOPE;
import static org.restlet.ext.oauth.OAuthResourceDefs.USERNAME;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.TokenVerifier;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.Verifier;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class TokenVerifierTest extends OAuthTestBase {

    private Reference tokenAuthURI = new Reference(baseURI, "/oauth/token_auth");

    public static class StubApplication extends Application {

        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("/token_auth", StubServerResource.class);
            return router;
        }
    }

    public static class StubServerResource extends ServerResource {

        @Post
        public Representation authenticate(Representation input)
                throws Exception {
            JSONObject call = new JsonRepresentation(input).getJsonObject();

            if (call.getString(ACCESS_TOKEN).equals(STUB_ACCESS_TOKEN)) {
                JSONObject resp = new JSONObject();
                resp.put(USERNAME, "testuser");
                resp.put(SCOPE, "a b");
                return new JsonRepresentation(resp);
            } else {
                OAuthException oex = new OAuthException(
                        OAuthError.invalid_token, "Invalid Token", null);
                return new JsonRepresentation(oex.createErrorDocument());
            }
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
     * Test case 1: Verifier not accept Body-method nor Query-method.
     */
    @Test
    public void testCase1() {
        TokenVerifier verifier = new TokenVerifier(tokenAuthURI);
        verifier.setAcceptBodyMethod(false);
        verifier.setAcceptQueryMethod(false);

        Request request = new Request();
        request.setMethod(Method.GET);
        request.setChallengeResponse(null);
        Reference ref = new Reference("http://localhost:8080/dummy");
        ref.addQueryParameter(ACCESS_TOKEN, STUB_ACCESS_TOKEN);
        request.setOriginalRef(ref);

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_MISSING));
    }

    /**
     * Test case 2: Token is missing. (Alternative methods)
     */
    @Test
    public void testCase2() {
        TokenVerifier verifier = new TokenVerifier(tokenAuthURI);
        verifier.setAcceptBodyMethod(true);
        verifier.setAcceptQueryMethod(true);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setChallengeResponse(null);
        request.setOriginalRef(new Reference("http://localhost:8080/dummy"));

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_MISSING));
    }

    /**
     * Test case 3: Token is missing. (Non-alternative methods)
     */
    @Test
    public void testCase3() {
        TokenVerifier verifier = new TokenVerifier(tokenAuthURI);

        Request request = new Request();
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH_BEARER));

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_MISSING));
    }

    /**
     * Test case 4: Unsupported ChallengeScheme.
     */
    @Test
    public void testCase4() {
        TokenVerifier verifier = new TokenVerifier(tokenAuthURI);

        Request request = new Request();
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.CUSTOM));

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_UNSUPPORTED));
    }

    /**
     * Test case 5: Invalid access_token.
     */
    @Test
    public void testCase5() {
        TokenVerifier verifier = new TokenVerifier(tokenAuthURI);

        Request request = new Request();
        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH_BEARER);
        cr.setRawValue("qux");
        request.setChallengeResponse(cr);

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_INVALID));
    }

    /**
     * Test case 6: Valid access_token(Success).
     */
    @Test
    public void testCase6() {
        TokenVerifier verifier = new TokenVerifier(tokenAuthURI);

        Request request = new Request();
        ChallengeResponse cr = new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH_BEARER);
        cr.setRawValue(STUB_ACCESS_TOKEN);
        request.setChallengeResponse(cr);

        int result = verifier.verify(request, new Response(request));
        assertThat(result, is(Verifier.RESULT_VALID));

        ClientInfo info = request.getClientInfo();
        assertThat(info.getUser().getIdentifier(), is("testuser"));
        assertThat(Scopes.parseScope(info.getRoles()),
                is(arrayContainingInAnyOrder("a", "b")));
    }
}
