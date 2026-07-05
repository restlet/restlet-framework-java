/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.data.Status;

class ThrowableAnnotationInfoTestCase {

    @Test
    void constructor_parsesStatusFromAnnotationValue() {
        ThrowableAnnotationInfo info =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, true);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, info.getStatus());
        assertEquals("404", info.getAnnotationValue());
        assertEquals(RuntimeException.class, info.getJavaClass());
        assertNull(info.getJavaMethod());
        assertTrue(info.isSerializable());
    }

    @Test
    void equals_sameClassAndStatus_returnsTrue() {
        ThrowableAnnotationInfo info1 =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, true);
        ThrowableAnnotationInfo info2 =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, false);
        assertTrue(info1.equals(info2));
    }

    @Test
    void equals_differentStatus_returnsFalse() {
        ThrowableAnnotationInfo info1 =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, true);
        ThrowableAnnotationInfo info2 =
                new ThrowableAnnotationInfo(RuntimeException.class, 500, true);
        assertFalse(info1.equals(info2));
    }

    @Test
    void equals_differentType_returnsFalse() {
        ThrowableAnnotationInfo info =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, true);
        assertFalse(info.equals("not an info"));
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        ThrowableAnnotationInfo info =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, true);
        assertTrue(info.equals(info));
    }

    @Test
    void hashCode_matchesForEqualInstances() {
        ThrowableAnnotationInfo info1 =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, true);
        ThrowableAnnotationInfo info2 =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, false);
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void toString_containsStatusAndSerializable() {
        ThrowableAnnotationInfo info =
                new ThrowableAnnotationInfo(RuntimeException.class, 404, true);
        String result = info.toString();
        assertTrue(result.contains("404"));
        assertTrue(result.contains("serializable=true"));
    }
}
