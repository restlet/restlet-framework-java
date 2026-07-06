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

import java.util.Set;
import org.junit.jupiter.api.Test;

class SetUtilsTestCase {

    @Test
    void newHashSet_withElements_containsAllElements() {
        Set<String> set = SetUtils.newHashSet("a", "b", "c");
        assertEquals(3, set.size());
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
    }

    @Test
    void newHashSet_withNoElements_isEmpty() {
        Set<String> set = SetUtils.newHashSet();
        assertTrue(set.isEmpty());
    }
}
