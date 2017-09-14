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

package org.restlet.test.data;

import org.restlet.data.AuthenticationInfo;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.data.Reference}.
 * 
 * @author Kelly McLaughlin (mclaughlin77[at]gmail.com)
 */
public class AuthenticationInfoTestCase extends RestletTestCase {
    /**
     * Test parsing an Authorization-Info header string.
     */
    public void testAuthenticationInfoHeaderParse() throws Exception {
        AuthenticationInfo authInfo = new AuthenticationInfo("00000002", 1,
                "MDAzMTAw1", "auth", null);
        String authInfoHeader = new String(
                "nc=00000001, qop=auth, cnonce=\"MDAzMTAw1\", nextnonce=00000002");
        AuthenticationInfo parsedAuthInfo = AuthenticatorUtils
                .parseAuthenticationInfo(authInfoHeader);
        assertTrue(authInfo.equals(parsedAuthInfo));
        assertTrue(parsedAuthInfo.equals(authInfo));
    }

    /**
     * Test cnonce getting/setting.
     */
    public void testCnonce() throws Exception {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getClientNonce(), "testcnonce");
        String newCnonce = new String("newcnonce");
        authInfo.setClientNonce(newCnonce);
        assertEquals(authInfo.getClientNonce(), "newcnonce");
    }

    /**
     * Equality tests.
     */
    public void testEquals() throws Exception {
        final AuthenticationInfo authInfo1 = new AuthenticationInfo(
                "testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        final AuthenticationInfo authInfo2 = new AuthenticationInfo(
                "testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo1, authInfo2);
        assertTrue(authInfo1.equals(authInfo2));
    }

    /**
     * Test nextnonce getting/setting.
     */
    public void testNextNonce() throws Exception {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getNextServerNonce(), "testnonce");
        String newNonce = new String("newnonce");
        authInfo.setNextServerNonce(newNonce);
        assertEquals(authInfo.getNextServerNonce(), "newnonce");
    }

    /**
     * Test nonce-count getting/setting.
     */
    public void testNonceCount() throws Exception {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getNonceCount(), 1111111);
        int newNonceCount = 2222222;
        authInfo.setNonceCount(newNonceCount);
        assertEquals(authInfo.getNonceCount(), 2222222);
    }

    /**
     * Test message-qop getting/setting.
     */
    public void testQop() throws Exception {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getQuality(), "auth");
        String newQop = new String("auth-int");
        authInfo.setQuality(newQop);
        assertEquals(authInfo.getQuality(), "auth-int");
    }

    /**
     * Test response-auth getting/setting.
     */
    public void testResponseAuth() throws Exception {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getResponseDigest(), "FFFFFF");
        String newResponseAuth = new String("000000");
        authInfo.setResponseDigest(newResponseAuth);
        assertEquals(authInfo.getResponseDigest(), "000000");
    }

    public void testUnEquals() throws Exception {
        final AuthenticationInfo authInfo1 = new AuthenticationInfo(
                "testnonce1", 1111111, "testcnonce1", "auth", "FFFFFF");
        final AuthenticationInfo authInfo2 = new AuthenticationInfo(
                "testnonce2", 1111111, "testcnonce2", "auth", "FFFFFF");
        assertFalse(authInfo1.equals(authInfo2));
        assertFalse(authInfo1.equals(null));
        assertFalse(authInfo2.equals(null));
        assertFalse(authInfo1.getNextServerNonce().equals(
                authInfo2.getNextServerNonce()));
        assertFalse(authInfo1.getClientNonce().equals(
                authInfo2.getClientNonce()));
    }
}
