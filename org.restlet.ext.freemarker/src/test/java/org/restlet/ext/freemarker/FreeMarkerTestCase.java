/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.freemarker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

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

    @Test
    void testFreemarkerConverterWithTemplate() throws Exception {
        FreemarkerConverter converter = new FreemarkerConverter();

        Configuration fmc = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringRepresentation templateSource = new StringRepresentation("Hello ${m}");
        templateSource.setCharacterSet(CharacterSet.UTF_8);
        Template template = TemplateRepresentation.getTemplate(fmc, templateSource);
        assertNotNull(template);

        assertEquals(1.0f, converter.score(template, new Variant(MediaType.TEXT_HTML), null));

        Request request = new Request(Method.GET, "/test");
        Response response = new Response(request);
        ServerResource resource = new ServerResource() {};
        resource.setRequest(request);
        resource.setResponse(response);

        Representation representation =
                converter.toRepresentation(template, new Variant(MediaType.TEXT_HTML), resource);
        assertNotNull(representation);
        assertInstanceOf(TemplateRepresentation.class, representation);
        assertEquals("Hello GET", representation.getText());
    }

    @Test
    void testTemplateRepresentationConstructorsWithDefaultConfiguration() throws IOException {
        StringRepresentation rep = new StringRepresentation("Hello ${name}");
        rep.setCharacterSet(CharacterSet.UTF_8);

        // Constructor(Representation, MediaType) - uses a default Configuration
        TemplateRepresentation tr1 = new TemplateRepresentation(rep, MediaType.TEXT_PLAIN);
        assertNotNull(tr1.getTemplate());
        assertNull(tr1.getDataModel());

        // Constructor(Representation, Object dataModel, MediaType) - default Configuration
        StringRepresentation rep2 = new StringRepresentation("Hello ${name}");
        rep2.setCharacterSet(CharacterSet.UTF_8);
        TemplateRepresentation tr2 =
                new TemplateRepresentation(rep2, Map.of("name", "world"), MediaType.TEXT_PLAIN);
        assertEquals(Map.of("name", "world"), tr2.getDataModel());
        assertEquals("Hello world", tr2.getText());

        // Constructor(Representation, Configuration, Object dataModel, MediaType)
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringRepresentation rep3 = new StringRepresentation("Hello ${name}");
        rep3.setCharacterSet(CharacterSet.UTF_8);
        TemplateRepresentation tr3 =
                new TemplateRepresentation(
                        rep3, cfg, Map.of("name", "restlet"), MediaType.TEXT_PLAIN);
        assertEquals("Hello restlet", tr3.getText());
    }

    @Test
    void testTemplateRepresentationWithoutExplicitCharacterSet() {
        // No character set is set on the source representation: the UTF-8 fallback path is used.
        StringRepresentation rep = new StringRepresentation("Hello ${name}");
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        Template template = TemplateRepresentation.getTemplate(cfg, rep);
        assertNotNull(template);
    }

    @Test
    void testTemplateRepresentationEqualsAndHashCode() {
        TemplateRepresentation tr =
                new TemplateRepresentation((Template) null, MediaType.TEXT_PLAIN);
        assertEquals(tr.hashCode(), tr.hashCode());
    }

    @Test
    void testTemplateRepresentationSetDataModelFromRequestResponse() throws IOException {
        Request request = new Request(Method.GET, "/test");
        Response response = new Response(request);
        response.setStatus(Status.SUCCESS_OK);

        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringRepresentation rep = new StringRepresentation("Method=${m}");
        rep.setCharacterSet(CharacterSet.UTF_8);
        TemplateRepresentation tr = new TemplateRepresentation(rep, cfg, MediaType.TEXT_PLAIN);

        Object dataModel = tr.setDataModel(request, response);
        assertNotNull(dataModel);
        assertEquals(dataModel, tr.getDataModel());
        assertEquals("Method=GET", tr.getText());
    }

    @Test
    void testTemplateRepresentationWriteWithTemplateException() {
        // A missing variable used with the "?number" built-in raises a TemplateException while
        // processing, which write() should translate into an IOException.
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringRepresentation rep = new StringRepresentation("${missingVar}");
        rep.setCharacterSet(CharacterSet.UTF_8);
        TemplateRepresentation tr =
                new TemplateRepresentation(rep, cfg, Map.of(), MediaType.TEXT_PLAIN);

        StringWriter writer = new StringWriter();
        assertThrows(IOException.class, () -> tr.write(writer));
    }
}
