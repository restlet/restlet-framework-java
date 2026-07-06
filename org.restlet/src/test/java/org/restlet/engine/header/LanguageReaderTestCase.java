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
import org.junit.jupiter.api.Test;
import org.restlet.data.Language;

class LanguageReaderTestCase {

    @Test
    void readValue_english_parsesLanguage() throws IOException {
        assertEquals(Language.ENGLISH, new LanguageReader("en").readValue());
    }

    @Test
    void readValue_frenchFrance_parsesLanguage() throws IOException {
        assertEquals(Language.FRENCH_FRANCE, new LanguageReader("fr-fr").readValue());
    }
}
