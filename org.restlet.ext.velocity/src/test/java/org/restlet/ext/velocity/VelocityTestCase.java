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

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Test case for the Velocity extension.
 *
 * @author Jerome Louvel
 */
class VelocityTestCase {

    @Test
    void testRepresentationTemplate() throws Exception {
        // Create a temporary directory for the tests
        File testDir = new File(System.getProperty("java.io.tmpdir"), "VelocityTestCase");
        testDir.mkdir();

        // Create a temporary template file
        File testFile = File.createTempFile("test", ".vm", testDir);
        FileWriter fw = new FileWriter(testFile);
        fw.write("Value=$value");
        fw.close();

        Map<String, Object> map = new TreeMap<>();
        map.put("value", "myValue");

        // Representation approach
        Reference ref = LocalReference.createFileReference(testFile);
        ClientResource r = new ClientResource(ref);
        Representation templateFile = r.get();
        TemplateRepresentation tr =
                new TemplateRepresentation(templateFile, map, MediaType.TEXT_PLAIN);
        final String result = tr.getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        IoUtils.delete(testFile);
        IoUtils.delete(testDir, true);
    }

    @Test
    void testStandardTemplate() throws Exception {
        // Create a temporary directory for the tests
        final File testDir = new File(System.getProperty("java.io.tmpdir"), "VelocityTestCase");
        testDir.mkdir();

        // Create a temporary template file
        final File testFile = File.createTempFile("test", ".vm", testDir);
        final FileWriter fw = new FileWriter(testFile);
        fw.write("Value=$value");
        fw.close();

        final Map<String, Object> map = new TreeMap<>();
        map.put("value", "myValue");

        // Standard approach
        final TemplateRepresentation tr =
                new TemplateRepresentation(testFile.getName(), map, MediaType.TEXT_PLAIN);
        tr.getEngine().setProperty("file.resource.loader.path", testDir.getAbsolutePath());
        final String result = tr.getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        IoUtils.delete(testFile);
        IoUtils.delete(testDir, true);
    }
}
