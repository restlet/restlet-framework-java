/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;

/**
 * Unit tests for the {@link ObjectRepresentation} class.
 *
 * @author Jerome Louvel
 */
class ObjectRepresentationTestCase {

    @Test
    void constructor_withObjectOnly_usesJavaObjectMediaType() throws IOException {
        ObjectRepresentation<String> representation = new ObjectRepresentation<>("payload");

        assertEquals(MediaType.APPLICATION_JAVA_OBJECT, representation.getMediaType());
        assertEquals("payload", representation.getObject());
    }

    @Test
    void constructor_withNullMediaType_defaultsToJavaObject() {
        ObjectRepresentation<String> representation = new ObjectRepresentation<>("payload", null);

        assertEquals(MediaType.APPLICATION_JAVA_OBJECT, representation.getMediaType());
    }

    @Test
    void setObjectThenGetObject_roundTrips() throws IOException {
        ObjectRepresentation<String> representation = new ObjectRepresentation<>("initial");

        representation.setObject("updated");

        assertEquals("updated", representation.getObject());
    }

    @Test
    void release_clearsObject() throws IOException {
        ObjectRepresentation<String> representation = new ObjectRepresentation<>("payload");

        representation.release();

        assertNull(representation.getObject());
    }

    @Test
    void writeThenReadBack_roundTripsBinarySerializedObject() throws Exception {
        ObjectRepresentation<String> original = new ObjectRepresentation<>("round-trip-value");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        original.write(out);

        Representation serialized =
                new ByteArrayRepresentation(out.toByteArray(), MediaType.APPLICATION_JAVA_OBJECT);
        ObjectRepresentation<Serializable> deserialized = new ObjectRepresentation<>(serialized);

        assertEquals("round-trip-value", deserialized.getObject());
    }

    @Test
    void constructor_fromRepresentationWithUnsupportedMediaType_throwsIllegalArgumentException() {
        Representation notSerialized = new StringRepresentation("plain text");

        assertThrows(
                IllegalArgumentException.class, () -> new ObjectRepresentation<>(notSerialized));
    }

    @Test
    void write_withXmlMediaType_producesXmlEncodedOutput() throws IOException {
        ObjectRepresentation<String> representation =
                new ObjectRepresentation<>("xml-value", MediaType.APPLICATION_JAVA_OBJECT_XML);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);

        String xml = out.toString(StandardCharsets.UTF_8);
        assertTrue(xml.contains("xml-value"));
    }
}
