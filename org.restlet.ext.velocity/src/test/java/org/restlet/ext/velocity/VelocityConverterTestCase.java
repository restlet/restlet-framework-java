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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.velocity.Template;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;

/** Unit tests for {@link VelocityConverter}. */
class VelocityConverterTestCase {

    private File testDir;

    private File testFile;

    private Template template;

    @BeforeEach
    void setUp() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), "VelocityConverterTestCase");
        testDir.mkdir();
        testFile = File.createTempFile("test", ".vm", testDir);

        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write("Value=$value");
        }

        TemplateRepresentation tr =
                new TemplateRepresentation(testFile.getName(), MediaType.TEXT_PLAIN);
        tr.getEngine().setProperty("file.resource.loader.path", testDir.getAbsolutePath());
        template = tr.getTemplate();
    }

    @AfterEach
    void tearDown() {
        IoUtils.delete(testFile);
        IoUtils.delete(testDir, true);
    }

    @Test
    void getObjectClasses_alwaysReturnsEmptyList() {
        assertTrue(
                new VelocityConverter()
                        .getObjectClasses(new Variant(MediaType.TEXT_PLAIN))
                        .isEmpty());
    }

    @Test
    void getVariants_forTemplate_returnsMediaTypeAll() {
        List<?> variants = new VelocityConverter().getVariants(Template.class);
        assertEquals(1, variants.size());
        assertTrue(MediaType.ALL.isCompatible(((Variant) variants.getFirst()).getMediaType()));
    }

    @Test
    void getVariants_forOtherClass_returnsEmptyList() {
        assertTrue(new VelocityConverter().getVariants(String.class).isEmpty());
    }

    @Test
    void score_template_returnsOne() {
        assertEquals(
                1.0f,
                new VelocityConverter().score(template, new Variant(MediaType.TEXT_PLAIN), null));
    }

    @Test
    void score_nonTemplate_returnsMinusOne() {
        assertEquals(
                -1.0f,
                new VelocityConverter()
                        .score("not a template", new Variant(MediaType.TEXT_PLAIN), null));
    }

    @Test
    void score_repr_alwaysReturnsMinusOne() {
        assertEquals(
                -1.0f,
                new VelocityConverter()
                        .score(new StringRepresentation("text"), String.class, null));
    }

    @Test
    void toObject_alwaysReturnsNull() {
        assertNull(
                new VelocityConverter()
                        .toObject(new StringRepresentation("text"), String.class, null));
    }

    @Test
    void toRepresentation_nonTemplate_returnsNull() {
        assertNull(
                new VelocityConverter()
                        .toRepresentation("string", new Variant(MediaType.TEXT_PLAIN), null));
    }

    @Test
    void toRepresentation_forTemplate_returnsTemplateRepresentationWithDataModel()
            throws Exception {
        ClientResource resource = new ClientResource("http://localhost/");

        Object result =
                new VelocityConverter()
                        .toRepresentation(template, new Variant(MediaType.TEXT_PLAIN), resource);

        assertTrue(result instanceof TemplateRepresentation);
        TemplateRepresentation tr = (TemplateRepresentation) result;
        assertEquals(MediaType.TEXT_PLAIN, tr.getMediaType());
        assertEquals("Value=$value", tr.getText());
    }

    @Test
    void updatePreferences_forTemplate_addsMediaTypeAll() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new VelocityConverter().updatePreferences(prefs, Template.class);
        assertEquals(1, prefs.size());
        assertEquals(MediaType.ALL, prefs.getFirst().getMetadata());
    }

    @Test
    void updatePreferences_forOtherClass_doesNotModify() {
        List<Preference<MediaType>> prefs = new ArrayList<>();
        new VelocityConverter().updatePreferences(prefs, String.class);
        assertTrue(prefs.isEmpty());
    }
}
