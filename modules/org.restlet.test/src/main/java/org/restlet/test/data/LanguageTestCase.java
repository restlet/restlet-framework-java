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

import org.junit.jupiter.api.Test;
import org.restlet.data.Language;
import org.restlet.test.RestletTestCase;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test {@link org.restlet.data.Language}.
 *
 * @author Jerome Louvel
 */
public class LanguageTestCase extends RestletTestCase {

    /**
     * Testing {@link Language#valueOf(String)}
     */
    @Test
    public void testValueOf() {
        assertSame(Language.FRENCH_FRANCE, Language.valueOf("fr-fr"));
        assertSame(Language.ALL, Language.valueOf("*"));
    }

    @Test
    public void testUnmodifiable() {
        try {
            Language.FRENCH_FRANCE.getSubTags().add("foo");
            fail("The subtags shouldn't be modifiable");
        } catch (UnsupportedOperationException uoe) {
            // As expected
        }
    }
}
