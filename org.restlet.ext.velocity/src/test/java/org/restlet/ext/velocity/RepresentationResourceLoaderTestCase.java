/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.velocity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import org.apache.velocity.Template;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.util.ExtProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link RepresentationResourceLoader}. */
class RepresentationResourceLoaderTestCase {

    /** A representation whose reader always fails, to simulate an I/O error while loading. */
    private static class BrokenRepresentation extends StringRepresentation {
        BrokenRepresentation(String text, MediaType mediaType) {
            super(text, mediaType);
        }

        @Override
        public Reader getReader() throws IOException {
            throw new IOException("Simulated I/O error");
        }
    }

    @AfterEach
    void tearDown() {
        RepresentationResourceLoader.getStore().clear();
    }

    @Test
    void getResourceReader_sourceInStore_returnsStoredRepresentationContent() {
        RepresentationResourceLoader.getStore()
                .put("storedKey", new StringRepresentation("stored-content"));
        RepresentationResourceLoader loader = new RepresentationResourceLoader(null);

        Reader reader = loader.getResourceReader("storedKey", "UTF-8");

        assertEquals("stored-content", IoUtils.toString(reader));
    }

    @Test
    void getResourceReader_sourceNotInStoreWithDefault_returnsDefaultRepresentationContent() {
        Representation defaultRepresentation = new StringRepresentation("default-content");
        RepresentationResourceLoader loader =
                new RepresentationResourceLoader(defaultRepresentation);

        Reader reader = loader.getResourceReader("unknownKey", "UTF-8");

        assertEquals("default-content", IoUtils.toString(reader));
    }

    @Test
    void getResourceReader_sourceNotInStoreAndNoDefault_throwsResourceNotFoundException() {
        RepresentationResourceLoader loader = new RepresentationResourceLoader(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> loader.getResourceReader("missingKey", "UTF-8"));
    }

    @Test
    void getResourceReader_whenReadingFails_wrapsIOExceptionInResourceNotFoundException() {
        RepresentationResourceLoader loader =
                new RepresentationResourceLoader(
                        new BrokenRepresentation("content", MediaType.TEXT_PLAIN));

        ResourceNotFoundException e =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> loader.getResourceReader("anyKey", "UTF-8"));
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    void getLastModified_resourceInStore_returnsStoredModificationDateMillis() {
        Date date = new Date(1_000_000L);
        Representation representation = new StringRepresentation("content");
        representation.setModificationDate(date);
        RepresentationResourceLoader.getStore().put("modKey", representation);

        Template resource = new Template();
        resource.setName("modKey");

        RepresentationResourceLoader loader = new RepresentationResourceLoader(null);
        assertEquals(date.getTime(), loader.getLastModified(resource));
    }

    @Test
    void getLastModified_resourceNotInStore_returnsZero() {
        Template resource = new Template();
        resource.setName("absentKey");

        RepresentationResourceLoader loader = new RepresentationResourceLoader(null);
        assertEquals(0L, loader.getLastModified(resource));
    }

    @Test
    void isSourceModified_whenLastModifiedDiffers_returnsTrue() {
        Date date = new Date(2_000_000L);
        Representation representation = new StringRepresentation("content");
        representation.setModificationDate(date);
        RepresentationResourceLoader.getStore().put("modKey2", representation);

        Template resource = new Template();
        resource.setName("modKey2");
        resource.setLastModified(date.getTime() - 1000);

        RepresentationResourceLoader loader = new RepresentationResourceLoader(null);
        assertTrue(loader.isSourceModified(resource));
    }

    @Test
    void isSourceModified_whenLastModifiedMatches_returnsFalse() {
        Date date = new Date(3_000_000L);
        Representation representation = new StringRepresentation("content");
        representation.setModificationDate(date);
        RepresentationResourceLoader.getStore().put("modKey3", representation);

        Template resource = new Template();
        resource.setName("modKey3");
        resource.setLastModified(date.getTime());

        RepresentationResourceLoader loader = new RepresentationResourceLoader(null);
        assertFalse(loader.isSourceModified(resource));
    }

    @Test
    void init_doesNotThrow() {
        RepresentationResourceLoader loader = new RepresentationResourceLoader(null);
        assertDoesNotThrow(() -> loader.init(new ExtProperties()));
    }
}
