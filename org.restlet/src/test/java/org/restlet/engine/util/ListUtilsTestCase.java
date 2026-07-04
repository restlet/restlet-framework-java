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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ListUtils}. */
class ListUtilsTestCase {

    @Test
    void copySubList_returnsCopyOfRange() {
        List<String> source = List.of("a", "b", "c", "d");
        assertEquals(List.of("b", "c"), ListUtils.copySubList(source, 1, 2));
    }

    @Test
    void copySubList_negativeFromIndex_throws() {
        assertThrows(
                IndexOutOfBoundsException.class, () -> ListUtils.copySubList(List.of("a"), -1, 0));
    }

    @Test
    void copySubList_toIndexBeyondSize_throws() {
        assertThrows(
                IndexOutOfBoundsException.class, () -> ListUtils.copySubList(List.of("a"), 0, 5));
    }

    @Test
    void copySubList_fromIndexGreaterThanToIndex_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ListUtils.copySubList(List.of("a", "b"), 1, 0));
    }
}
