/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.security;

import junit.framework.TestCase;

import org.restlet.Context;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.security.MapVerifier;

/**
 * Restlet unit tests for HTTP DIGEST authentication client/server.
 * 
 * @author Jerome Louvel
 */
public class HttpDigestTestCase extends TestCase {

    public void testDigest() {

        Context context = new Context();
        DigestAuthenticator da = new DigestAuthenticator(context, "TestRealm",
                "mySecretServerKey");
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put("scott", "tiger".toCharArray());
        da.setWrappedVerifier(mapVerifier);

        ClientResource cr = new ClientResource("http://localhost:8182/");
        cr.setNext(da);

        // Try unauthenticated request
        try {
            cr.get();
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, cr.getStatus());

            ChallengeRequest c1 = cr.getChallengeRequests().get(0);
            String realm = c1.getRealm();
            String nonce = c1.getParameters().getFirstValue("nonce");
            // String opaque = c1.getParameters().getFirstValue("opaque");
            // String qop = c1.getParameters().getFirstValue("qop");

            assertEquals(ChallengeScheme.HTTP_DIGEST, c1.getScheme());
            assertEquals("TestRealm", realm);
            // assertEquals(null, opaque);
            // assertEquals("auth", qop);

            // Try authenticated request
            ChallengeResponse c2 = new ChallengeResponse(
                    ChallengeScheme.HTTP_DIGEST);
            c2.setIdentifier("scott");
            c2.setSecret("tiger");
            c2.getParameters().add("realm", realm);
            c2.getParameters().add("nonce", nonce);
            // c2.getParameters().add("opaque", opaque);
            c2.getParameters().add("uri", "/");
            c2.getParameters().add("qop", "auth");
            c2.getParameters().add("nc", "00000001");
            c2.getParameters().add("cnonce", "123456");
            c2.getParameters().add("response", "");

            cr.setChallengeResponse(c2);
            cr.get();
            assertTrue(cr.getStatus().isSuccess());
        }
    }
}
