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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.junit.jupiter.api.Test;

class WrapperListTestCase {

    @Test
    void defaultConstructor_startsEmpty() {
        assertTrue(new WrapperList<String>().isEmpty());
    }

    @Test
    void constructorWithCapacity_startsEmpty() {
        assertTrue(new WrapperList<String>(5).isEmpty());
    }

    @Test
    void constructorWithDelegate_wrapsGivenList() {
        List<String> delegate = new ArrayList<>(List.of("a"));
        WrapperList<String> list = new WrapperList<>(delegate);
        assertEquals(1, list.size());
        assertEquals(delegate, list.getDelegate());
    }

    @Test
    void add_appendsElement() {
        WrapperList<String> list = new WrapperList<>();
        assertTrue(list.add("a"));
        assertEquals("a", list.getFirst());
    }

    @Test
    void addAtIndex_insertsAtPosition() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        list.add("c");
        list.add(1, "b");
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void addFirstAndAddLast_insertAtBoundaries() {
        WrapperList<String> list = new WrapperList<>();
        list.add("b");
        list.addFirst("a");
        list.addLast("c");
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void addAll_appendsAllElements() {
        WrapperList<String> list = new WrapperList<>();
        assertTrue(list.addAll(Arrays.asList("a", "b")));
        assertEquals(2, list.size());
    }

    @Test
    void addAllAtIndex_insertsAllAtPosition() {
        WrapperList<String> list = new WrapperList<>();
        list.add("c");
        list.addAll(0, Arrays.asList("a", "b"));
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void clear_removesAllElements() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    void contains_findsElement() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        assertTrue(list.contains("a"));
        assertFalse(list.contains("b"));
    }

    @Test
    void containsAll_findsAllElements() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b"));
        assertTrue(list.containsAll(Arrays.asList("a", "b")));
        assertFalse(list.containsAll(Arrays.asList("a", "c")));
    }

    @Test
    void equals_sameElementsInOrder_returnsTrue() {
        WrapperList<String> list1 = new WrapperList<>();
        list1.addAll(Arrays.asList("a", "b"));
        WrapperList<String> list2 = new WrapperList<>();
        list2.addAll(Arrays.asList("a", "b"));
        assertEquals(list1, list2);
    }

    @Test
    void equals_differentElements_returnsFalse() {
        WrapperList<String> list1 = new WrapperList<>();
        list1.add("a");
        WrapperList<String> list2 = new WrapperList<>();
        list2.add("b");
        assertNotEquals(list1, list2);
    }

    @Test
    void hashCode_matchesDelegateHashCode() {
        List<String> delegate = new ArrayList<>(List.of("a"));
        WrapperList<String> list = new WrapperList<>(delegate);
        assertEquals(delegate.hashCode(), list.hashCode());
    }

    @Test
    void indexOfAndLastIndexOf_locateElements() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b", "a"));
        assertEquals(0, list.indexOf("a"));
        assertEquals(2, list.lastIndexOf("a"));
        assertEquals(-1, list.indexOf("z"));
    }

    @Test
    void iterator_iteratesOverElements() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b"));
        Iterator<String> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
    }

    @Test
    void listIterator_startsAtBeginning() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        ListIterator<String> iterator = list.listIterator();
        assertEquals("a", iterator.next());
    }

    @Test
    void listIteratorWithIndex_startsAtGivenPosition() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b"));
        ListIterator<String> iterator = list.listIterator(1);
        assertEquals("b", iterator.next());
    }

    @Test
    void removeByIndex_returnsRemovedElement() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        assertEquals("a", list.removeFirst());
        assertTrue(list.isEmpty());
    }

    @Test
    void removeByObject_removesFirstOccurrence() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        assertTrue(list.remove("a"));
        assertFalse(list.remove("a"));
    }

    @Test
    void removeAll_removesEachMatchingElement() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b", "c"));
        assertTrue(list.removeAll(Arrays.asList("a", "c")));
        assertEquals(List.of("b"), list);
    }

    @Test
    void retainAll_keepsOnlySpecifiedElements() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b", "c"));
        assertTrue(list.retainAll(List.of("b")));
        assertEquals(List.of("b"), list);
    }

    @Test
    void set_replacesElementAtIndexAndReturnsPrevious() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        assertEquals("a", list.set(0, "b"));
        assertEquals("b", list.getFirst());
    }

    @Test
    void subList_returnsPortionAsWrapperList() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b", "c"));
        List<String> sub = list.subList(1, 3);
        assertEquals(List.of("b", "c"), sub);
    }

    @Test
    void toArray_returnsAllElements() {
        WrapperList<String> list = new WrapperList<>();
        list.addAll(Arrays.asList("a", "b"));
        assertEquals(2, list.toArray().length);
    }

    @Test
    void toArrayWithType_returnsTypedArray() {
        WrapperList<String> list = new WrapperList<>();
        list.add("a");
        String[] result = list.toArray(new String[0]);
        assertEquals("a", result[0]);
    }

    @Test
    void toString_matchesDelegateToString() {
        List<String> delegate = new ArrayList<>(List.of("a"));
        WrapperList<String> list = new WrapperList<>(delegate);
        assertEquals(delegate.toString(), list.toString());
    }
}
