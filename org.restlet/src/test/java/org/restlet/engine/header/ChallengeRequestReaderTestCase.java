/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.restlet.data.ChallengeRequest;

class ChallengeRequestReaderTestCase {

    @Test
    void readValue_schemeOnly_parsesSchemeName() throws IOException {
        ChallengeRequestReader reader = new ChallengeRequestReader("Basic");
        ChallengeRequest result = reader.readValue();
        assertEquals("Basic", result.getScheme().getTechnicalName());
    }

    @Test
    void readValue_schemeWithParameters_capturesRawValue() throws IOException {
        ChallengeRequestReader reader = new ChallengeRequestReader("Digest realm=\"testrealm\"");
        ChallengeRequest result = reader.readValue();
        assertEquals("Digest", result.getScheme().getTechnicalName());
        assertTrue(result.getRawValue().contains("realm"));
    }

    @Test
    void readValue_emptyHeader_returnsNull() throws IOException {
        ChallengeRequestReader reader = new ChallengeRequestReader("");
        assertNull(reader.readValue());
    }
}
