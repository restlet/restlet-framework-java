/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.restlet.data.Reference;
import org.restlet.resource.Directory;

/**
 * Test case for the alphanum algorithm used by {@link Directory}.
 *
 * @author Davide Angelocola
 */
class AlphaNumericComparatorTestCase {

    private static List<Reference> refs(String... uris) {
        List<Reference> result = new LinkedList<>();

        for (String uri : uris) {
            result.add(new Reference(uri));
        }

        return result;
    }

    private static final List<Reference> unsorted =
            refs("1", "2", "3", "1.0", "1.1", "1.1.1", "2.0", "2.2", "2.2.2", "3.0", "3.3");

    private static final List<Reference> expected =
            refs("1", "1.0", "1.1", "1.1.1", "2", "2.0", "2.2", "2.2.2", "3", "3.0", "3.3");

    @Test
    void testBug() {
        List<Reference> result = new ArrayList<>(unsorted);
        result.sort(new AlphaNumericComparator());
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({"Intel 5000X,Intel 5500", "3,66", "200,66", "18,2"})
    void testFirstIsLessThan(final String first, final String second) {
        AlphaNumericComparator anc = new AlphaNumericComparator();
        assertTrue(anc.compare(first, second) < 0);
    }
}
