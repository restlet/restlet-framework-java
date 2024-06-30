/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
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
    @Test
    public void testAuthenticationInfoHeaderParse() {
        AuthenticationInfo authInfo = new AuthenticationInfo("00000002", 1,
                "MDAzMTAw1", "auth", null);
        String authInfoHeader = "nc=00000001, qop=auth, cnonce=\"MDAzMTAw1\", nextnonce=00000002";
        AuthenticationInfo parsedAuthInfo = AuthenticatorUtils
                .parseAuthenticationInfo(authInfoHeader);
        assertEquals(authInfo, parsedAuthInfo);
        assertEquals(parsedAuthInfo, authInfo);
    }

    /**
     * Test cnonce getting/setting.
     */
    @Test
    public void testCnonce() {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getClientNonce(), "testcnonce");
        String newCnonce = "newcnonce";
        authInfo.setClientNonce(newCnonce);
        assertEquals(authInfo.getClientNonce(), "newcnonce");
    }

    /**
     * Equality tests.
     */
    @Test
    public void testEquals() {
        final AuthenticationInfo authInfo1 = new AuthenticationInfo(
                "testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        final AuthenticationInfo authInfo2 = new AuthenticationInfo(
                "testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo1, authInfo2);
        assertEquals(authInfo1, authInfo2);
    }

    /**
     * Test nextnonce getting/setting.
     */
    @Test
    public void testNextNonce() {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getNextServerNonce(), "testnonce");
        String newNonce = "newnonce";
        authInfo.setNextServerNonce(newNonce);
        assertEquals(authInfo.getNextServerNonce(), "newnonce");
    }

    /**
     * Test nonce-count getting/setting.
     */
    @Test
    public void testNonceCount() {
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
    @Test
    public void testQop() {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getQuality(), "auth");
        String newQop = "auth-int";
        authInfo.setQuality(newQop);
        assertEquals(authInfo.getQuality(), "auth-int");
    }

    /**
     * Test response-auth getting/setting.
     */
    @Test
    public void testResponseAuth() {
        AuthenticationInfo authInfo = new AuthenticationInfo("testnonce",
                1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(authInfo.getResponseDigest(), "FFFFFF");
        String newResponseAuth = "000000";
        authInfo.setResponseDigest(newResponseAuth);
        assertEquals(authInfo.getResponseDigest(), "000000");
    }

    @Test
    public void testUnEquals() {
        final AuthenticationInfo authInfo1 = new AuthenticationInfo(
                "testnonce1", 1111111, "testcnonce1", "auth", "FFFFFF");
        final AuthenticationInfo authInfo2 = new AuthenticationInfo(
                "testnonce2", 1111111, "testcnonce2", "auth", "FFFFFF");
        assertNotEquals(authInfo1, authInfo2);
        assertNotEquals(null, authInfo1);
        assertNotEquals(null, authInfo2);
        assertNotEquals(authInfo1.getNextServerNonce(), authInfo2.getNextServerNonce());
        assertNotEquals(authInfo1.getClientNonce(), authInfo2.getClientNonce());
    }
}
