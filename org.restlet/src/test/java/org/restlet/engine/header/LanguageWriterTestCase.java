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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.data.Language;

class LanguageWriterTestCase {

    @Test
    void write_singleLanguage_writesLanguageName() {
        assertEquals(Language.ENGLISH.getName(), LanguageWriter.write(List.of(Language.ENGLISH)));
    }

    @Test
    void write_multipleLanguages_joinsWithSeparator() {
        String result =
                LanguageWriter.write(Arrays.asList(Language.ENGLISH, Language.FRENCH_FRANCE));
        assertEquals(Language.ENGLISH.getName() + ", " + Language.FRENCH_FRANCE.getName(), result);
    }
}
