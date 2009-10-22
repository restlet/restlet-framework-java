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

package org.restlet.test.engine;

import org.restlet.Request;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.engine.http.HttpConstants;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.ext.crypto.internal.HttpAwsS3Helper;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the SecurityData related classes.
 * 
 * @author Jerome Louvel
 */
public class AuthenticationTestCase extends RestletTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        org.restlet.engine.Engine.setInstance(null);
    }

    /**
     * Test Amazon S3 authentication.
     */
    public void testAwsS3() {
        HttpAwsS3Helper helper = new HttpAwsS3Helper();

        // Example Object GET
        StringBuilder sb = new StringBuilder();
        ChallengeResponse challenge = new ChallengeResponse(
                ChallengeScheme.HTTP_AWS_S3, "0PN5J17HBGZHT7JJ3X82",
                "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o");
        Request request = new Request(Method.GET,
                "http://johnsmith.s3.amazonaws.com/photos/puppy.jpg");
        Form httpHeaders = new Form();
        httpHeaders.add(HttpConstants.HEADER_DATE,
                "Tue, 27 Mar 2007 19:36:42 +0000");

        helper.formatCredentials(sb, challenge, request, httpHeaders);
        assertEquals("0PN5J17HBGZHT7JJ3X82:xXjDGYUmKxnwqr5KXNPGldn5LbA=", sb
                .toString());

        // Example Object PUT
        sb = new StringBuilder();
        request.setMethod(Method.PUT);
        httpHeaders.set(HttpConstants.HEADER_DATE,
                "Tue, 27 Mar 2007 21:15:45 +0000", true);
        httpHeaders.add(HttpConstants.HEADER_CONTENT_LENGTH, "94328");
        httpHeaders.add(HttpConstants.HEADER_CONTENT_TYPE, "image/jpeg");
        helper.formatCredentials(sb, challenge, request, httpHeaders);
        assertEquals("0PN5J17HBGZHT7JJ3X82:hcicpDDvL9SsO6AkvxqmIWkmOuQ=", sb
                .toString());
    }

    /**
     * Tests the authentication parsing for HTTP BASIC.
     */
    public void testParsingBasic() {
        final String authenticate1 = "Basic realm=\"Restlet tutorial\"";
        final String authorization1 = "Basic c2NvdHQ6dGlnZXI=";

        assertEquals(authorization1, AuthenticatorUtils.formatResponse(
                AuthenticatorUtils.parseResponse(null, authorization1, null),
                null, null));
        assertEquals(authenticate1, AuthenticatorUtils.formatRequest(
                AuthenticatorUtils.parseRequest(authenticate1, null), null,
                null));
    }

    /**
     * Tests the authentication parsing for HTTP DIGEST.
     */
    public void testParsingDigest() {
        final String authorization1 = "Digest username=\"admin\",response=\"a891ebedebb2046b83a9b7540f4e9554\",cnonce=\"MTE3NzEwMzIwMjkwMDoxNmMzODFiYzRjNWRjMmMyOTVkMWFhNDdkMTQ4OGFlMw==\",qop=auth,uri=\"/protected/asdass\",nonce=\"MTE3NzEwMzIwMjg0Mjo2NzFjODQyMjAyOWRlNWQ1YjFjNmEzYzJmOWRlZmE2Mw==\",nc=00000001";
        final String authenticate1 = "Digest realm=\"realm\", domain=\"/protected/ /alsoProtected/\", qop=\"auth\", algorithm=MD5, nonce=\"MTE3NzEwMzIwMjg0Mjo2NzFjODQyMjAyOWRlNWQ1YjFjNmEzYzJmOWRlZmE2Mw==\"";

        final ChallengeResponse cres = AuthenticatorUtils.parseResponse(null,
                authorization1, null);
        cres.setCredentials(null);
        assertEquals(authorization1, AuthenticatorUtils.formatResponse(cres,
                null, null));

        final ChallengeRequest creq = AuthenticatorUtils.parseRequest(
                authenticate1, null);
        assertEquals(authenticate1, AuthenticatorUtils.formatRequest(creq,
                null, null));
    }
}
