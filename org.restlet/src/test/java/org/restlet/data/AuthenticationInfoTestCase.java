/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.restlet.engine.security.AuthenticatorUtils;

/**
 * Test {@link org.restlet.data.Reference}.
 *
 * @author Kelly McLaughlin (mclaughlin77[at]gmail.com)
 */
class AuthenticationInfoTestCase {
    /** Test parsing an Authorization-Info header string. */
    @Test
    void testAuthenticationInfoHeaderParse() {
        AuthenticationInfo authInfo =
                new AuthenticationInfo("00000002", 1, "MDAzMTAw1", "auth", null);
        String authInfoHeader = "nc=00000001, qop=auth, cnonce=\"MDAzMTAw1\", nextnonce=00000002";
        AuthenticationInfo parsedAuthInfo =
                AuthenticatorUtils.parseAuthenticationInfo(authInfoHeader);

        assertEquals(authInfo, parsedAuthInfo);
        assertEquals(parsedAuthInfo, authInfo);
    }

    /** Test cnonce getting/setting. */
    @Test
    void testCnonce() {
        AuthenticationInfo authInfo =
                new AuthenticationInfo("testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals("testcnonce", authInfo.getClientNonce());

        String newCnonce = "newcnonce";
        authInfo.setClientNonce(newCnonce);
        assertEquals("newcnonce", authInfo.getClientNonce());
    }

    /** Equality tests. */
    @Test
    void testEquals() {
        final AuthenticationInfo authInfo1 =
                new AuthenticationInfo("testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        final AuthenticationInfo authInfo2 =
                new AuthenticationInfo("testnonce", 1111111, "testcnonce", "auth", "FFFFFF");

        assertEquals(authInfo1, authInfo2);
        assertEquals(authInfo1, authInfo2);
    }

    /** Test nextnonce getting/setting. */
    @Test
    void testNextNonce() {
        AuthenticationInfo authInfo =
                new AuthenticationInfo("testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals("testnonce", authInfo.getNextServerNonce());

        String newNonce = "newnonce";
        authInfo.setNextServerNonce(newNonce);
        assertEquals("newnonce", authInfo.getNextServerNonce());
    }

    /** Test nonce-count getting/setting. */
    @Test
    void testNonceCount() {
        AuthenticationInfo authInfo =
                new AuthenticationInfo("testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals(1111111, authInfo.getNonceCount());

        int newNonceCount = 2222222;
        authInfo.setNonceCount(newNonceCount);
        assertEquals(2222222, authInfo.getNonceCount());
    }

    /** Test message-qop getting/setting. */
    @Test
    void testQop() {
        AuthenticationInfo authInfo =
                new AuthenticationInfo("testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals("auth", authInfo.getQuality());

        String newQop = "auth-int";
        authInfo.setQuality(newQop);
        assertEquals("auth-int", authInfo.getQuality());
    }

    /** Test response-auth getting/setting. */
    @Test
    void testResponseAuth() {
        AuthenticationInfo authInfo =
                new AuthenticationInfo("testnonce", 1111111, "testcnonce", "auth", "FFFFFF");
        assertEquals("FFFFFF", authInfo.getResponseDigest());

        String newResponseAuth = "000000";
        authInfo.setResponseDigest(newResponseAuth);
        assertEquals("000000", authInfo.getResponseDigest());
    }

    @Test
    void testUnEquals() {
        final AuthenticationInfo authInfo1 =
                new AuthenticationInfo("testnonce1", 1111111, "testcnonce1", "auth", "FFFFFF");
        final AuthenticationInfo authInfo2 =
                new AuthenticationInfo("testnonce2", 1111111, "testcnonce2", "auth", "FFFFFF");

        assertNotEquals(authInfo1, authInfo2);
        assertNotEquals(null, authInfo1);
        assertNotEquals(null, authInfo2);
        assertNotEquals(authInfo1.getNextServerNonce(), authInfo2.getNextServerNonce());
        assertNotEquals(authInfo1.getClientNonce(), authInfo2.getClientNonce());
    }
}
