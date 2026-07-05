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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.restlet.util.Series;

/**
 * Test {@link org.restlet.data.ChallengeMessage}, exercised through the concrete {@link
 * ChallengeRequest} subclass since ChallengeMessage is abstract.
 */
class ChallengeMessageTestCase {

    @Test
    void constructor_withSchemeOnly_hasNullRealmAndDigestAlgorithm() {
        ChallengeMessage message = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        assertEquals(ChallengeScheme.HTTP_BASIC, message.getScheme());
        assertNull(message.getRealm());
        assertEquals(Digest.ALGORITHM_MD5, message.getDigestAlgorithm());
        assertNull(message.getOpaque());
        assertNull(message.getServerNonce());
        assertNull(message.getRawValue());
    }

    @Test
    void constructor_withSchemeAndRealm() {
        ChallengeMessage message = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "myRealm");
        assertEquals("myRealm", message.getRealm());
    }

    @Test
    void getParameters_lazilyCreatesEmptySeries() {
        ChallengeMessage message = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        assertNotNull(message.getParameters());
        assertEquals(0, message.getParameters().size());
    }

    @Test
    void setParameters_replacesSeries() {
        ChallengeMessage message = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        Series<Parameter> parameters = new Series<>(Parameter.class);
        parameters.add("k", "v");
        message.setParameters(parameters);
        assertSame(parameters, message.getParameters());
    }

    @Test
    void settersUpdateState() {
        ChallengeMessage message = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        message.setDigestAlgorithm(Digest.ALGORITHM_SHA_256);
        message.setOpaque("opaque-value");
        message.setRawValue("raw-value");
        message.setRealm("realm");
        message.setScheme(ChallengeScheme.HTTP_DIGEST);
        message.setServerNonce("nonce");

        assertEquals(Digest.ALGORITHM_SHA_256, message.getDigestAlgorithm());
        assertEquals("opaque-value", message.getOpaque());
        assertEquals("raw-value", message.getRawValue());
        assertEquals("realm", message.getRealm());
        assertEquals(ChallengeScheme.HTTP_DIGEST, message.getScheme());
        assertEquals("nonce", message.getServerNonce());
    }

    @Test
    void equals_sameSchemeRealmAndParameters_returnsTrue() {
        ChallengeMessage m1 = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm");
        ChallengeMessage m2 = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm");
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        ChallengeMessage message = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        assertEquals(message, message);
    }

    @Test
    void equals_differentType_returnsFalse() {
        ChallengeMessage message = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        assertNotEquals(message, "not a challenge message");
    }

    @Test
    void equals_differentRealm_returnsFalse() {
        ChallengeMessage m1 = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm1");
        ChallengeMessage m2 = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "realm2");
        assertNotEquals(m1, m2);
    }
}
