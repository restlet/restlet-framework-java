/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.velocity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import org.apache.velocity.Template;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.WriterRepresentation;
import org.restlet.util.Resolver;

/**
 * Unit tests for {@link TemplateRepresentation}, focused on its less commonly exercised branches.
 */
class TemplateRepresentationTestCase {

    /** A minimal representation that never exposes a character set nor a modification date. */
    private static class RawRepresentation extends WriterRepresentation {
        private final String content;

        RawRepresentation(String content, MediaType mediaType) {
            super(mediaType);
            this.content = content;
        }

        @Override
        public void write(Writer writer) throws IOException {
            writer.write(content);
        }
    }

    private File testDir;

    @AfterEach
    void tearDown() {
        if (testDir != null) {
            IoUtils.delete(testDir, true);
        }
    }

    private static org.apache.velocity.context.Context getInternalContext(TemplateRepresentation tr)
            throws Exception {
        Field field = TemplateRepresentation.class.getDeclaredField("context");
        field.setAccessible(true);
        return (org.apache.velocity.context.Context) field.get(tr);
    }

    private static Resolver<Object> knownValueResolver() {
        return new Resolver<Object>() {
            @Override
            public Object resolve(String name) {
                return "known".equals(name) ? "value1" : null;
            }
        };
    }

    @Test
    void resolverContext_containsKey_reflectsResolverPresenceOfValue() throws Exception {
        TemplateRepresentation tr = new TemplateRepresentation("template.vm", MediaType.TEXT_PLAIN);
        tr.setDataModel(knownValueResolver());

        org.apache.velocity.context.Context context = getInternalContext(tr);

        assertTrue(context.containsKey("known"));
        assertFalse(context.containsKey("unknown"));
    }

    @Test
    void resolverContext_get_delegatesToResolver() throws Exception {
        TemplateRepresentation tr = new TemplateRepresentation("template.vm", MediaType.TEXT_PLAIN);
        tr.setDataModel(knownValueResolver());

        org.apache.velocity.context.Context context = getInternalContext(tr);

        assertEquals("value1", context.get("known"));
    }

    @Test
    void resolverContext_getKeys_returnsNull() throws Exception {
        TemplateRepresentation tr = new TemplateRepresentation("template.vm", MediaType.TEXT_PLAIN);
        tr.setDataModel(knownValueResolver());

        org.apache.velocity.context.Context context = getInternalContext(tr);

        assertNull(context.getKeys());
    }

    @Test
    void resolverContext_put_returnsNullAndDoesNotStoreValue() throws Exception {
        TemplateRepresentation tr = new TemplateRepresentation("template.vm", MediaType.TEXT_PLAIN);
        tr.setDataModel(knownValueResolver());

        org.apache.velocity.context.Context context = getInternalContext(tr);

        assertNull(context.put("known", "other"));
        // The resolver is still consulted; nothing was actually stored.
        assertEquals("value1", context.get("known"));
    }

    @Test
    void resolverContext_remove_returnsNull() throws Exception {
        TemplateRepresentation tr = new TemplateRepresentation("template.vm", MediaType.TEXT_PLAIN);
        tr.setDataModel(knownValueResolver());

        org.apache.velocity.context.Context context = getInternalContext(tr);

        assertNull(context.remove("known"));
    }

    @Test
    void constructorWithTemplateAndMapDataModel_mergesProvidedValues() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "TemplateRepresentationTestCase");
        testDir.mkdir();
        File testFile = File.createTempFile("test", ".vm", testDir);

        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write("Value=$value");
        }

        TemplateRepresentation base =
                new TemplateRepresentation(testFile.getName(), MediaType.TEXT_PLAIN);
        base.getEngine().setProperty("file.resource.loader.path", testDir.getAbsolutePath());
        Template template = base.getTemplate();

        Map<String, Object> map = new TreeMap<>();
        map.put("value", "myValue2");

        TemplateRepresentation tr = new TemplateRepresentation(template, map, MediaType.TEXT_PLAIN);

        assertEquals(MediaType.TEXT_PLAIN, tr.getMediaType());
        assertEquals("Value=myValue2", tr.getText());
    }

    @Test
    void constructorWithRepresentationAndMediaType_withoutCharsetOrModDate_usesDefaults()
            throws Exception {
        RawRepresentation raw = new RawRepresentation("Value=fixed", MediaType.TEXT_PLAIN);

        TemplateRepresentation tr = new TemplateRepresentation(raw, MediaType.TEXT_PLAIN);

        assertEquals("Value=fixed", tr.getText());
    }

    @Test
    void constructorWithRepresentationMapAndMediaType_withoutCharset_usesDefaultCharset()
            throws Exception {
        RawRepresentation raw = new RawRepresentation("Value=$value", MediaType.TEXT_PLAIN);
        Map<String, Object> map = new TreeMap<>();
        map.put("value", "myValue3");

        TemplateRepresentation tr = new TemplateRepresentation(raw, map, MediaType.TEXT_PLAIN);

        assertEquals("Value=myValue3", tr.getText());
    }

    @Test
    void getTemplate_whenTemplateCannotBeLoaded_logsWarningAndReturnsNull() {
        Context previous = Context.getCurrent();
        try {
            Context.setCurrent(new Context());
            TemplateRepresentation tr =
                    new TemplateRepresentation("doesNotExist.vm", MediaType.TEXT_PLAIN);
            assertNull(tr.getTemplate());
        } finally {
            Context.setCurrent(previous);
        }
    }

    @Test
    void getText_whenTemplateCannotBeLoaded_throwsIOException() {
        TemplateRepresentation tr =
                new TemplateRepresentation("doesNotExist2.vm", MediaType.TEXT_PLAIN);

        IOException e = assertThrows(IOException.class, tr::getText);
        assertTrue(e.getMessage().startsWith("Template processing error."));
    }
}
