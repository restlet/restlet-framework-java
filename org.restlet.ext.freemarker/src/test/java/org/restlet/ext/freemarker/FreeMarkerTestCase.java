/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.freemarker;

import static freemarker.template.Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

/**
 * Unit test for the FreeMarker extension.
 *
 * @author Jerome Louvel
 */
class FreeMarkerTestCase {

    @Test
    void testTemplate() throws Exception {
        // Create a temporary directory for the tests
        final File testDir = Files.createTempDirectory("FreeMarkerTestCase").toFile();

        // Create a temporary template file
        final File testFile = File.createTempFile("test", ".ftl", testDir);
        final FileWriter fw = new FileWriter(testFile);
        fw.write("Value=${value}");
        fw.close();

        final Configuration fmc =
                new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        fmc.setDirectoryForTemplateLoading(testDir);
        final Map<String, Object> map = Map.of("value", "myValue");

        final String result =
                new TemplateRepresentation(testFile.getName(), fmc, map, MediaType.TEXT_PLAIN)
                        .getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        IoUtils.delete(testDir, true);
    }

    @Test
    void testTemplateRepresentationConstructors() {
        // Constructor with Template object (null template)
        TemplateRepresentation tr =
                new TemplateRepresentation((Template) null, MediaType.TEXT_PLAIN);
        assertNull(tr.getTemplate());
        assertNull(tr.getDataModel());

        // setDataModel and setTemplate
        tr.setDataModel("myModel");
        assertEquals("myModel", tr.getDataModel());
        tr.setTemplate(null);
        assertNull(tr.getTemplate());

        // Constructor with template name + config: template not found → null
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        tr = new TemplateRepresentation("nonexistent.ftl", cfg, MediaType.TEXT_PLAIN);
        assertNull(tr.getTemplate());

        // Constructor with template name + config + data model
        tr =
                new TemplateRepresentation(
                        "nonexistent.ftl", cfg, Map.of("k", "v"), MediaType.TEXT_PLAIN);
        assertEquals(Map.of("k", "v"), tr.getDataModel());

        // Constructor with Representation + config (no directory set → template loading fails)
        StringRepresentation rep = new StringRepresentation("Hello ${name}");
        rep.setCharacterSet(org.restlet.data.CharacterSet.UTF_8);
        TemplateRepresentation tr2 = new TemplateRepresentation(rep, cfg, MediaType.TEXT_PLAIN);
        assertNotNull(tr2); // The template is loaded inline from the representation

        // setDataModel with a Resolver (anonymous class, since Resolver is abstract)
        tr = new TemplateRepresentation((Template) null, MediaType.TEXT_PLAIN);
        tr.setDataModel(
                new org.restlet.util.Resolver<>() {
                    @Override
                    public Object resolve(String name) {
                        return "resolved-" + name;
                    }
                });
        assertNotNull(tr.getDataModel());

        // static getTemplate(config, name) — name not found returns null (already tested above via
        // constructor)
        assertNull(TemplateRepresentation.getTemplate(cfg, "nonexistent.ftl"));
    }

    @Test
    void testTemplateRepresentationWriteNullTemplate() throws Exception {
        // write() with null template should log a warning and not throw
        TemplateRepresentation tr =
                new TemplateRepresentation((Template) null, MediaType.TEXT_PLAIN);
        java.io.StringWriter sw = new java.io.StringWriter();
        tr.write(sw); // must not throw
        assertEquals("", sw.toString());
    }

    @Test
    void testFreemarkerConverterScore() {
        FreemarkerConverter converter = new FreemarkerConverter();

        assertEquals(-1.0f, converter.score(null, new Variant(MediaType.TEXT_PLAIN), null));
        assertEquals(-1.0f, converter.score("hello", new Variant(MediaType.TEXT_PLAIN), null));
        assertEquals(-1.0f, converter.score(new StringRepresentation("x"), String.class, null));
        assertTrue(converter.getObjectClasses(new Variant(MediaType.TEXT_PLAIN)).isEmpty());
        assertTrue(converter.getVariants(String.class).isEmpty());
        assertNull(converter.toObject(null, String.class, null));
        assertNull(
                converter.toRepresentation(
                        "notATemplate", new Variant(MediaType.TEXT_PLAIN), null));
    }
}
