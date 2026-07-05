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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.ChallengeRequest}. */
class ChallengeRequestTestCase {

    @Test
    void constructor_withSchemeOnly_hasDefaults() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        assertEquals(ChallengeScheme.HTTP_DIGEST, request.getScheme());
        assertFalse(request.isStale());
        assertNull(request.getRealm());
    }

    @Test
    void constructor_withRealm() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST, "myRealm");
        assertEquals("myRealm", request.getRealm());
    }

    @Test
    void getDomainRefs_defaultsToRootReference() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        List<Reference> refs = request.getDomainRefs();
        assertEquals(1, refs.size());
        assertEquals("/", refs.get(0).toString());
    }

    @Test
    void setDomainRefs_replacesList() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        request.setDomainRefs(Arrays.asList(new Reference("/a"), new Reference("/b")));
        assertEquals(2, request.getDomainRefs().size());
    }

    @Test
    void setDomainUris_convertsStringsToReferences() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        request.setDomainUris(Arrays.asList("/a", "/b"));
        List<Reference> refs = request.getDomainRefs();
        assertEquals(2, refs.size());
        assertEquals("/a", refs.get(0).toString());
        assertEquals("/b", refs.get(1).toString());
    }

    @Test
    void setDomainUris_withNull_setsNullDomainRefs() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        request.setDomainUris(Arrays.asList("/a"));
        request.setDomainUris(null);
        // getDomainRefs() re-initializes the lazy default when null.
        assertEquals(1, request.getDomainRefs().size());
        assertEquals("/", request.getDomainRefs().get(0).toString());
    }

    @Test
    void getQualityOptions_defaultsToAuthentication() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        List<String> options = request.getQualityOptions();
        assertEquals(1, options.size());
        assertEquals(ChallengeMessage.QUALITY_AUTHENTICATION, options.get(0));
    }

    @Test
    void setQualityOptions_replacesList() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        request.setQualityOptions(Arrays.asList(ChallengeMessage.QUALITY_AUTHENTICATION_INTEGRITY));
        assertEquals(1, request.getQualityOptions().size());
        assertEquals(
                ChallengeMessage.QUALITY_AUTHENTICATION_INTEGRITY,
                request.getQualityOptions().get(0));
    }

    @Test
    void setStale_updatesFlag() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        request.setStale(true);
        assertTrue(request.isStale());
    }

    @Test
    void equals_sameSchemeRealmAndParameters_returnsTrue() {
        ChallengeRequest r1 = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST, "realm");
        ChallengeRequest r2 = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST, "realm");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        assertEquals(request, request);
    }

    @Test
    void equals_differentType_returnsFalse() {
        ChallengeRequest request = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        assertNotEquals(request, "not a challenge request");
    }

    @Test
    void equals_differentRealm_returnsFalse() {
        ChallengeRequest r1 = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST, "realm1");
        ChallengeRequest r2 = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST, "realm2");
        assertNotEquals(r1, r2);
    }
}
