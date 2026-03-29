/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test {@link org.restlet.data.Language}.
 *
 * @author Jerome Louvel
 */
class LanguageTestCase {

    /** Testing {@link Language#valueOf(String)} */
    @Test
    void testValueOf() {
        assertSame(Language.FRENCH_FRANCE, Language.valueOf("fr-fr"));
        assertSame(Language.ALL, Language.valueOf("*"));
    }

    @Test
    void testUnmodifiable() {
        final List<String> subTags = Language.FRENCH_FRANCE.getSubTags();
        assertThrows(UnsupportedOperationException.class, () -> subTags.add("foo"));
    }
}
