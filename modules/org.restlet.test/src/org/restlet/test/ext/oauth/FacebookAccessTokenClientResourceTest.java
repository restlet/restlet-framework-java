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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.restlet.ext.oauth.OAuthResourceDefs.ACCESS_TOKEN;
import static org.restlet.ext.oauth.OAuthResourceDefs.CLIENT_ID;
import static org.restlet.ext.oauth.OAuthResourceDefs.CLIENT_SECRET;
import static org.restlet.ext.oauth.OAuthResourceDefs.ERROR;
import static org.restlet.ext.oauth.OAuthResourceDefs.ERROR_DESC;

import java.io.IOException;

import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.FacebookAccessTokenClientResource;
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
public class FacebookAccessTokenClientResourceTest extends OAuthTestBase {

    public static class StubApplication extends Application {

        @Override
        public synchronized Restlet createInboundRoot() {
            Router router = new Router(getContext());
            router.attach("/token1", StubServerResource1.class);
            router.attach("/token2", StubServerResource2.class);
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
     * Test case 1: Successful Response with Client Auth.
     */
    public static class StubServerResource1 extends ServerResource {

        @Post
        public Representation requestToken(Representation input)
                throws JSONException {
            Form form = new Form(input);
            assertThat(form.getFirstValue(CLIENT_ID), is(STUB_CLIENT_ID));
            assertThat(form.getFirstValue(CLIENT_SECRET),
                    is(STUB_CLIENT_SECRET));
            Form response = new Form();
            response.add(ACCESS_TOKEN, "foo");
            response.add("expires", "3600");
            return response.getWebRepresentation();
        }
    }

    @Test
    public void testCase1() throws OAuthException, IOException, JSONException {
        FacebookAccessTokenClientResource tokenResource = new FacebookAccessTokenClientResource(
                new Reference(baseURI, "/oauth/token1"));
        tokenResource.setClientCredentials(STUB_CLIENT_ID, STUB_CLIENT_SECRET);
        Token token = tokenResource.requestToken(new OAuthParameters());
        assertThat(token.getAccessToken(), is("foo"));
        assertThat(token.getExpirePeriod(), is(3600));
    }

    /**
     * Test case 2: Error Response. (with HTTP Code 200)
     */
    public static class StubServerResource2 extends ServerResource {

        @Post
        public Representation requestToken(Representation input)
                throws JSONException {
            Form response = new Form();
            response.add(ERROR, OAuthError.invalid_client.name());
            response.add(ERROR_DESC, "Invalid Client");
            return response.getWebRepresentation();
        }
    }

    @Test(expected = OAuthException.class)
    public void testCase2() throws OAuthException, IOException, JSONException {
        FacebookAccessTokenClientResource tokenResource = new FacebookAccessTokenClientResource(
                new Reference(baseURI, "/oauth/token2"));
        tokenResource.setClientCredentials(STUB_CLIENT_ID, STUB_CLIENT_SECRET);
        tokenResource.requestToken(new OAuthParameters());
        fail("OAuthException is expected.");
    }
}
