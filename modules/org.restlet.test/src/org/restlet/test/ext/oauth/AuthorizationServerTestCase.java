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

import java.io.IOException;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.oauth.Flow;
import org.restlet.ext.oauth.OAuthForm;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.ext.oauth.app.OAuthTestApplication;


public class AuthorizationServerTestCase extends OAuthHttpTestBase{
    
    public AuthorizationServerTestCase(){
        this(false);
    }
    
    public AuthorizationServerTestCase(boolean https){
        super(false, https);
        //Engine.setLogLevel(Level.FINE);
    }
    
    public void testAuthorizationServer() throws Exception{
        webFlow();
        noneFlow();
        passwordFlow();
        scopedResource();
    }
    
    public void webFlow() throws Exception {
        assertNull(client.getToken());
        ClientResource cr = new ClientResource(getProt() + "://localhost:"
                + serverPort + "/client/webclient");
        cr.setNext(this.reqClient);
        ChallengeResponse chresp = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, "bob", "alice");
        cr.setChallengeResponse(chresp);
        Representation r = cr.get();
        String text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        assertNotNull(client.getToken());
        cr.release();
        
        //reuse token
        // Testing Authorization header
        Reference ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/protected");
        cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        ChallengeResponse challengeResponse = new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH);
        challengeResponse.setRawValue(client.getToken());
        cr.setChallengeResponse(challengeResponse);
        r = cr.get();
        assertNotNull(r);
        text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();

        // Testing form
        ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/protected");
        cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        OAuthForm form = new OAuthForm(client.getToken());
        form.add("foo", "bar");
        r = cr.post(form.getWebRepresentation());
        assertNotNull(r);
        text = r.getText();
        assertEquals("Response text test", text, "Dummy");
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_PLAIN);
        cr.release();
        
        //Query
        ref = new Reference(getProt() + "://localhost:" + serverPort
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
        
    }

    public void noneFlow() throws IOException {
        OAuthUser user = Flow.NONE.execute(client.getOauthParameters(), null,
                null, null, null, null, this.reqClient);
        assertNotNull(user);

        // Try to use the token...
        Reference ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        ClientResource cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();

        // None flow scoped test
        ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/scoped");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        r = cr.get();
        assertNotNull(r);
        text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
    }

    /*
     * @Test public void testRestletRoles(){ Role r1 = new Role("foo",null);
     * Role r2 = new Role("foo",null);
     * 
     * Assert.assertTrue( r1.equals(r2) ); Assert.assertTrue( r2.equals(r1) ); }
     */

    
    public void scopedResource() throws IOException {
        // Query test
        assertNotNull(client.getToken());
        Reference ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/scoped");
        ref.addQueryParameter("oauth_token", client.getToken());
        ClientResource cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();
    }

    
    public void passwordFlow() throws IOException {
        OAuthUser user = Flow.PASSWORD.execute(client.getOauthParameters(),
                null, null, OAuthTestApplication.TEST_USER,
                OAuthTestApplication.TEST_PASS, null, this.reqClient);
        assertNotNull(user);

        // Try to use the token...
        Reference ref = new Reference(getProt() + "://localhost:" + serverPort
                + "/server/protected");
        ref.addQueryParameter("oauth_token", user.getAccessToken());
        ClientResource cr = new ClientResource(ref);
        cr.setNext(this.reqClient);
        Representation r = cr.get();
        assertNotNull(r);
        String text = r.getText();
        assertTrue(text.startsWith("TestSuccessful"));
        assertEquals("Response content type test", r.getMediaType(),
                MediaType.TEXT_HTML);
        cr.release();

        // Wrong username test
        try {
            user = Flow.PASSWORD.execute(client.getOauthParameters(), null,
                    null, "somewrong", OAuthTestApplication.TEST_PASS, null, this.reqClient);
        } catch (ResourceException re) { // Should be invalidated
            assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, re.getStatus());
        }

        // Wrong pasword test
        try {
            user = Flow.PASSWORD.execute(client.getOauthParameters(), null,
                    null, OAuthTestApplication.TEST_USER, "somewrong", null, this.reqClient);

        } catch (ResourceException re) { // Should be invalidated
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, re.getStatus());
        }
    }
}
