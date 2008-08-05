/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.test;

import junit.framework.TestCase;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;

import com.noelios.restlet.authentication.AuthenticationUtils;

/**
 * Unit tests for the SecurityData related classes.
 * 
 * @author Jerome Louvel
 */
public class AuthenticationTestCase extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        org.restlet.util.Engine.setInstance(null);
    }

    /**
     * Tests the cookies parsing.
     */
    public void testParsingBasic() {
        final String authenticate1 = "Basic realm=\"Restlet tutorial\"";
        final String authorization1 = "Basic c2NvdHQ6dGlnZXI=";

        assertEquals(authorization1, AuthenticationUtils.format(
                AuthenticationUtils.parseAuthorizationHeader(null, null,
                        authorization1), null, null));
        assertEquals(authenticate1, AuthenticationUtils
                .format(AuthenticationUtils
                        .parseAuthenticateHeader(authenticate1)));
    }

    /**
     * Tests the cookies parsing with Digest authentication.
     */
    public void testParsingDigest() {
        final String authorization1 = "Digest cnonce=\"MTE3NzEwMzIwMjkwMDoxNmMzODFiYzRjNWRjMmMyOTVkMWFhNDdkMTQ4OGFlMw==\",qop=auth,uri=\"/protected/asdass\",username=\"admin\",nonce=\"MTE3NzEwMzIwMjg0Mjo2NzFjODQyMjAyOWRlNWQ1YjFjNmEzYzJmOWRlZmE2Mw==\",response=\"a891ebedebb2046b83a9b7540f4e9554\",nc=00000001";
        final String authenticate1 = "Digest realm=\"realm\", domain=\"/protected/ /alsoProtected/\", qop=\"auth\", algorithm=MD5, nonce=\"MTE3NzEwMzIwMjg0Mjo2NzFjODQyMjAyOWRlNWQ1YjFjNmEzYzJmOWRlZmE2Mw==\"";

        final ChallengeResponse cres = AuthenticationUtils
                .parseAuthorizationHeader(null, null, authorization1);
        cres.setCredentials(null);
        assertEquals(authorization1, AuthenticationUtils.format(cres, null,
                null));

        final ChallengeRequest creq = AuthenticationUtils
                .parseAuthenticateHeader(authenticate1);
        assertEquals(authenticate1, AuthenticationUtils.format(creq));
    }
}
