/**
 * Copyright 2005-2012 Restlet S.A.S.
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

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.Flow;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.internal.CookieCopyClientResource;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class TimedTokenTestCase extends OAuthHttpTestBase {

    public TimedTokenTestCase() {
        this(false);
        //OAuthHttpTestBase.tokenTimeout = 5;
    }
    
    public TimedTokenTestCase(boolean https){
        super(false, https);
        OAuthHttpTestBase.tokenTimeout = 5;
    }

    /**
     * Test that a token times out and that it can be refreshed
     * 
     * @throws Exception
     */
    public void testTimedTokens() throws Exception {
        client.clearUser();
        assertNull(client.getToken());
        ChallengeResponse chresp = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "bob", "alice");
        ClientResource cr = new CookieCopyClientResource(getProt() + "://localhost:"
                + serverPort + "/client/webclient");
        cr.setNext(this.reqClient);
        cr.setChallengeResponse(chresp);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        assertNotNull(client.getToken());
        cr.release();
        // Query test
        assertNotNull(client.getToken());
        Reference ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", client.getToken());
        cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        r = cr.get();
        assertNotNull(r);
        text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
        Thread.sleep(6);

        // Check if token timed out...
        assertNotNull(client.getToken());
        ref.addQueryParameter("oauth_token", client.getToken());
        cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        try {
            r = cr.get();
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, re.getStatus());
        }
        cr.release();
        assertNotNull(r);

        // Finally check that we can refresh our token
        refresh();
    }

    protected void refresh() throws Exception {
        OAuthUser user = client.getUser();
        assertNotNull(user);
        OAuthUser refreshed = Flow.REFRESH.execute(client.getOauthParameters(),
                null, null, null, null, client.getUser().getRefreshToken(), this.reqClient);
        assertNotNull(user);
        String wrongToken = refreshed.getAccessToken();
        assertNotNull(wrongToken);

        // Back to back test
        refreshed = Flow.REFRESH.execute(client.getOauthParameters(), null,
                null, null, null, client.getUser().getRefreshToken(), this.reqClient);
        String newToken = refreshed.getAccessToken();
        assertNotNull(newToken);

        // Query test
        Reference ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", newToken);
        ClientResource cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertEquals("Response text test", text, "TestSuccessful");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
        // Check the token we got before
        ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", wrongToken);
        cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        try {
            r = cr.get();
        } catch (ResourceException re) { // Should be invalidated
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, re.getStatus());
        }
        cr.release();
        assertNotNull(r);
    }

}
