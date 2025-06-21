/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.freemarker;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.io.IoUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for the FreeMarker extension.
 * 
 * @author Jerome Louvel
 */
public class FreeMarkerTestCase {

    @Test
    public void testTemplate() throws Exception {
        // Create a temporary directory for the tests
        final File testDir = Files.createTempDirectory("FreeMarkerTestCase").toFile();

        // Create a temporary template file
        final File testFile = File.createTempFile("test", ".ftl", testDir);
        final FileWriter fw = new FileWriter(testFile);
        fw.write("Value=${value}");
        fw.close();

        final Configuration fmc = new Configuration();
        fmc.setDirectoryForTemplateLoading(testDir);
        final Map<String, Object> map = Map.of("value", "myValue");

        final String result = new TemplateRepresentation(testFile.getName(), fmc, map, MediaType.TEXT_PLAIN).getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        IoUtils.delete(testDir, true);
    }

}
