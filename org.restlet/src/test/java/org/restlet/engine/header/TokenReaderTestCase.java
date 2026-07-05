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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TokenReaderTestCase {

    @Test
    void readValue_singleToken_returnsToken() throws IOException {
        TokenReader reader = new TokenReader("gzip");
        assertEquals("gzip", reader.readValue());
    }

    @Test
    void addValues_multipleTokens_populatesCollection() {
        List<String> tokens = new ArrayList<>();
        new TokenReader("gzip, deflate").addValues(tokens);
        assertEquals(List.of("gzip", "deflate"), tokens);
    }
}
