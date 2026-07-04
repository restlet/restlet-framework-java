/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link SpringResource}. */
class SpringResourceTestCase {

    @Test
    void constructor_withNullRepresentation_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SpringResource(null));
    }

    @Test
    void constructor_withoutDescription_usesDefaultDescription() {
        SpringResource resource = new SpringResource(new StringRepresentation("text"));
        assertEquals("Restlet Representation", resource.getDescription());
    }

    @Test
    void constructor_withNullDescription_usesEmptyDescription() {
        SpringResource resource = new SpringResource(new StringRepresentation("text"), null);
        assertEquals("", resource.getDescription());
    }

    @Test
    void constructor_withCustomDescription_usesIt() {
        SpringResource resource =
                new SpringResource(new StringRepresentation("text"), "my description");
        assertEquals("my description", resource.getDescription());
    }

    @Test
    void existsAndIsOpen_alwaysReturnTrue() {
        SpringResource resource = new SpringResource(new StringRepresentation("text"));
        assertTrue(resource.exists());
        assertTrue(resource.isOpen());
    }

    @Test
    void getInputStream_returnsUnderlyingStream() throws Exception {
        SpringResource resource = new SpringResource(new StringRepresentation("hello"));
        assertEquals("hello", IoUtils.toString(resource.getInputStream()));
    }

    @Test
    void getInputStream_readTwiceOnTransientRepresentation_throwsIllegalStateException()
            throws Exception {
        StringRepresentation transientRepresentation = new StringRepresentation("hello");
        transientRepresentation.setTransient(true);
        SpringResource resource = new SpringResource(transientRepresentation);

        resource.getInputStream();

        assertThrows(IllegalStateException.class, resource::getInputStream);
    }

    @Test
    void getInputStream_whenStreamIsNull_throwsIllegalStateException() {
        Representation nullStreamRepresentation =
                new StringRepresentation("hello") {
                    @Override
                    public InputStream getStream() throws IOException {
                        return null;
                    }
                };
        SpringResource resource = new SpringResource(nullStreamRepresentation);

        assertThrows(IllegalStateException.class, resource::getInputStream);
    }

    @Test
    void equalsAndHashCode_basedOnWrappedRepresentation() {
        StringRepresentation representation = new StringRepresentation("hello");
        SpringResource resource1 = new SpringResource(representation);
        SpringResource resource2 = new SpringResource(representation);
        SpringResource resource3 =
                new SpringResource(
                        new StringRepresentation("other", org.restlet.data.MediaType.TEXT_HTML));

        assertEquals(resource1, resource1);
        assertEquals(resource1, resource2);
        assertEquals(resource1.hashCode(), resource2.hashCode());
        assertNotEquals(resource1, resource3);
        assertFalse(resource1.equals("not a resource"));
    }
}
