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

import org.junit.jupiter.api.Test;
import org.restlet.data.Reference;

class AlphabeticalComparatorTestCase {

    private final AlphabeticalComparator comparator = new AlphabeticalComparator();

    @Test
    void compare_bothNullIdentifiers_returnsZero() {
        Reference ref0 = new Reference((String) null);
        Reference ref1 = new Reference((String) null);
        assertEquals(0, comparator.compare(ref0, ref1));
    }

    @Test
    void compare_firstNullIdentifier_returnsNegative() {
        Reference ref0 = new Reference((String) null);
        Reference ref1 = new Reference("https://example.com/a");
        assertTrue(comparator.compare(ref0, ref1) < 0);
    }

    @Test
    void compare_secondNullIdentifier_returnsPositive() {
        Reference ref0 = new Reference("https://example.com/a");
        Reference ref1 = new Reference((String) null);
        assertTrue(comparator.compare(ref0, ref1) > 0);
    }

    @Test
    void compare_bothNonNull_delegatesToStringComparison() {
        Reference ref0 = new Reference("https://example.com/a");
        Reference ref1 = new Reference("https://example.com/b");
        assertTrue(comparator.compare(ref0, ref1) < 0);
        assertTrue(comparator.compare(ref1, ref0) > 0);
    }

    @Test
    void compareStrings_usesNaturalOrdering() {
        assertTrue(comparator.compare("abc", "abd") < 0);
        assertEquals(0, comparator.compare("abc", "abc"));
    }
}
