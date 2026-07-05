/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.freemarker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.representation.StringRepresentation;

class ContextTemplateLoaderTestCase {

    /** Context whose dispatcher records the last URI it received. */
    private static class CapturingContext extends Context {
        String lastRequestedUri;

        CapturingContext() {
            setClientDispatcher(
                    new Restlet() {
                        @Override
                        public void handle(Request request, Response response) {
                            lastRequestedUri = request.getResourceRef().toString();
                            response.setEntity(new StringRepresentation("template content"));
                        }
                    });
        }
    }

    /** Representation that tracks whether release() was called. */
    private static class TrackingRepresentation extends StringRepresentation {
        boolean released = false;

        TrackingRepresentation() {
            super("content");
        }

        @Override
        public void release() {
            released = true;
            super.release();
        }
    }

    @Test
    void findTemplateSource_nullContext_returnsNull() throws IOException {
        ContextTemplateLoader loader = new ContextTemplateLoader(null, "clap://test");
        assertNull(loader.findTemplateSource("test.ftl"));
    }

    @Test
    void findTemplateSource_baseUriWithTrailingSlash_buildsCorrectUri() throws IOException {
        CapturingContext ctx = new CapturingContext();
        ContextTemplateLoader loader = new ContextTemplateLoader(ctx, "clap://test/");
        loader.findTemplateSource("hello.ftl");
        assertEquals("clap://test/hello.ftl", ctx.lastRequestedUri);
    }

    @Test
    void findTemplateSource_baseUriWithoutTrailingSlash_buildsCorrectUri() throws IOException {
        CapturingContext ctx = new CapturingContext();
        ContextTemplateLoader loader = new ContextTemplateLoader(ctx, "clap://test");
        loader.findTemplateSource("hello.ftl");
        assertEquals("clap://test/hello.ftl", ctx.lastRequestedUri);
    }

    @Test
    void constructor_withReference_usesReferenceToString() throws IOException {
        CapturingContext ctx = new CapturingContext();
        Reference ref = new Reference("clap://test/");
        ContextTemplateLoader loader = new ContextTemplateLoader(ctx, ref);
        loader.findTemplateSource("hello.ftl");
        assertEquals("clap://test/hello.ftl", ctx.lastRequestedUri);
    }

    @Test
    void closeTemplateSource_withRepresentation_callsRelease() {
        ContextTemplateLoader loader = new ContextTemplateLoader(null, "clap://test");
        TrackingRepresentation rep = new TrackingRepresentation();
        loader.closeTemplateSource(rep);
        assertTrue(rep.released);
    }

    @Test
    void closeTemplateSource_withNonRepresentation_doesNotThrow() {
        ContextTemplateLoader loader = new ContextTemplateLoader(null, "clap://test");
        assertDoesNotThrow(() -> loader.closeTemplateSource("notARepresentation"));
    }

    @Test
    void getLastModified_withModificationDate_returnsTimestamp() {
        ContextTemplateLoader loader = new ContextTemplateLoader(null, "clap://test");
        Date now = new Date();
        StringRepresentation rep = new StringRepresentation("content");
        rep.setModificationDate(now);
        assertEquals(now.getTime(), loader.getLastModified(rep));
    }

    @Test
    void getLastModified_withNullModificationDate_returnsMinusOne() {
        ContextTemplateLoader loader = new ContextTemplateLoader(null, "clap://test");
        StringRepresentation rep = new StringRepresentation("content");
        assertEquals(-1L, loader.getLastModified(rep));
    }

    @Test
    void getReader_returnsReaderWithGivenCharacterSet() throws IOException {
        ContextTemplateLoader loader = new ContextTemplateLoader(null, "clap://test");
        StringRepresentation rep = new StringRepresentation("template content");
        try (java.io.Reader reader = loader.getReader(rep, "UTF-8")) {
            char[] buffer = new char[64];
            int read = reader.read(buffer);
            assertEquals("template content", new String(buffer, 0, read));
        }
    }
}
