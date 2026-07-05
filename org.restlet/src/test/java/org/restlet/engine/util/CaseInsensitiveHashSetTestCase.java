/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CaseInsensitiveHashSetTestCase {

    @Test
    void constructor_lowercasesInitialElements() {
        CaseInsensitiveHashSet set = new CaseInsensitiveHashSet(Arrays.asList("Foo", "BAR"));
        assertTrue(set.contains("foo"));
        assertTrue(set.contains("FOO"));
        assertTrue(set.contains("bar"));
    }

    @Test
    void add_lowercasesElement() {
        CaseInsensitiveHashSet set = new CaseInsensitiveHashSet(Collections.emptyList());
        set.add("Hello");
        assertTrue(set.contains("hello"));
        assertTrue(set.contains("HELLO"));
    }

    @Test
    void contains_stringOverload_isCaseInsensitive() {
        CaseInsensitiveHashSet set = new CaseInsensitiveHashSet(Collections.singletonList("value"));
        assertTrue(set.contains("VALUE"));
        assertFalse(set.contains("other"));
    }

    @Test
    void contains_objectOverload_delegatesToStringOverload() {
        CaseInsensitiveHashSet set = new CaseInsensitiveHashSet(Collections.singletonList("value"));
        Object key = "VALUE";
        assertTrue(set.contains(key));
    }
}
