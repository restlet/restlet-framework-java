/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Digest;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.security.AuthenticatorUtils;

class HttpDigestHelperTestCase {

    /** Tests the authentication parsing for HTTP DIGEST. */
    @Test
    void testParsingDigest() {
        // make sure the Digest authentication scheme is registered
        Engine.getInstance().getRegisteredAuthenticators().add(new HttpDigestHelper());
        ChallengeResponse cres1 =
                new ChallengeResponse(
                        ChallengeScheme.HTTP_DIGEST,
                        null,
                        "admin",
                        "12345".toCharArray(),
                        Digest.ALGORITHM_NONE,
                        null,
                        "qop",
                        new Reference("/protected/asdass"),
                        null,
                        null,
                        "MTE3NzEwMzIwMjkwMDoxNmMzODFiYzRjNWRjMmMyOTVkMWFhNDdkMTQ4OGFlMw==",
                        "MTE3NzEwMzIwMjkwMDoxNmMzODFiYzRjNWRjMmMyOTVkMWFhNDdkMTQ4OGFlMw==",
                        1,
                        0L);

        Request request = new Request(Method.GET, "http://remote.com/protected/asdass");
        String authorization1 = AuthenticatorUtils.formatResponse(cres1, request, null);
        String authenticate1 =
                "Digest realm=realm, domain=\"/protected/ /alsoProtected/\", qop=auth, algorithm=MD5, nonce=\"MTE3NzEwMzIwMjg0Mjo2NzFjODQyMjAyOWRlNWQ1YjFjNmEzYzJmOWRlZmE2Mw==\"";

        ChallengeResponse cres = AuthenticatorUtils.parseResponse(null, authorization1, null);
        cres.setRawValue(null);
        assertEquals(authorization1, AuthenticatorUtils.formatResponse(cres, request, null));

        List<ChallengeRequest> creq = AuthenticatorUtils.parseRequest(null, authenticate1, null);
        assertEquals(creq.size(), 1);
        assertEquals(authenticate1, AuthenticatorUtils.formatRequest(creq.getFirst(), null, null));
    }
}
