/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

/**
 * Tests of {@link ObjectRepresentation} with the default configuration, where Java object
 * deserialization is disabled. The flags are final, so these tests run in a dedicated surefire
 * execution that doesn't set the system properties enabling them.
 */
@Tag(ObjectRepresentationSerializationDisabledTestCase.TAG)
class ObjectRepresentationSerializationDisabledTestCase {

    static final String TAG = "serialization-disabled";

    @Test
    void serializationFlags_defaultToDisabled() {
        assertFalse(ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED);
        assertFalse(ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED);
    }

    @Test
    void constructor_fromBinaryRepresentation_throwsWhenBinaryNotSupported() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOut = new ObjectOutputStream(out)) {
            objectOut.writeObject("value");
        }
        Representation serialized =
                new ByteArrayRepresentation(out.toByteArray(), MediaType.APPLICATION_JAVA_OBJECT);

        assertThrows(IllegalArgumentException.class, () -> new ObjectRepresentation<>(serialized));
    }
}
