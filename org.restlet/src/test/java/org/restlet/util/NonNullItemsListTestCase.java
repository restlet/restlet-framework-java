/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class NonNullItemsListTestCase {

    @Test
    void add_nonNullElement_succeeds() {
        NonNullItemsList<String> list = new NonNullItemsList<>("no nulls allowed");
        assertTrue(list.add("a"));
        assertEquals("a", list.getFirst());
    }

    @Test
    void add_nullElement_throwsWithConfiguredMessage() {
        NonNullItemsList<String> list = new NonNullItemsList<>("no nulls allowed");
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> list.add(null));
        assertEquals("no nulls allowed", exception.getMessage());
    }

    @Test
    void addAtIndex_nullElement_throws() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        assertThrows(IllegalArgumentException.class, () -> list.add(0, null));
    }

    @Test
    void addFirst_nullElement_throws() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        assertThrows(IllegalArgumentException.class, () -> list.addFirst(null));
    }

    @Test
    void addFirst_nonNullElement_succeeds() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        list.add("b");
        list.addFirst("a");
        assertEquals("a", list.getFirst());
    }

    @Test
    void addLast_nullElement_throws() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        assertThrows(IllegalArgumentException.class, () -> list.addLast(null));
    }

    @Test
    void addLast_nonNullElement_succeeds() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        list.add("a");
        list.addLast("b");
        assertEquals("b", list.get(1));
    }

    @Test
    void addAll_collectionWithNull_throws() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        List<String> list1 = Arrays.asList("a", null, "b");
        assertThrows(IllegalArgumentException.class, () -> list.addAll(list1));
    }

    @Test
    void addAll_nullCollection_throws() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        assertThrows(IllegalArgumentException.class, () -> list.addAll(null));
    }

    @Test
    void addAll_validCollection_succeeds() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        assertTrue(list.addAll(Arrays.asList("a", "b")));
        assertEquals(2, list.size());
    }

    @Test
    void addAllAtIndex_collectionWithNull_throws() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        assertThrows(
                IllegalArgumentException.class,
                () -> list.addAll(0, Collections.singletonList(null)));
    }

    @Test
    void addAllAtIndex_validCollection_succeeds() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        list.add("c");
        assertTrue(list.addAll(0, Arrays.asList("a", "b")));
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    @Test
    void equals_sameElementsAndMessage_returnsTrue() {
        NonNullItemsList<String> list1 = new NonNullItemsList<>("msg");
        list1.add("a");
        NonNullItemsList<String> list2 = new NonNullItemsList<>("msg");
        list2.add("a");
        assertEquals(list1, list2);
    }

    @Test
    void equals_differentMessage_returnsFalse() {
        NonNullItemsList<String> list1 = new NonNullItemsList<>("msg1");
        list1.add("a");
        NonNullItemsList<String> list2 = new NonNullItemsList<>("msg2");
        list2.add("a");
        assertNotEquals(list1, list2);
    }

    @Test
    void equals_plainListWithSameElements_returnsFalse() {
        NonNullItemsList<String> list1 = new NonNullItemsList<>("msg");
        list1.add("a");
        ArrayList<String> plainList = new ArrayList<>(java.util.List.of("a"));
        assertNotEquals(list1, plainList);
    }

    @Test
    void hashCode_matchesDelegateHashCode() {
        NonNullItemsList<String> list = new NonNullItemsList<>("msg");
        list.add("a");
        assertEquals(list.getDelegate().hashCode(), list.hashCode());
    }
}
