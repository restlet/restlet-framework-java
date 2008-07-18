/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;

import freemarker.template.Configuration;

/**
 * Unit test for the FreeMarker extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class FreeMarkerTestCase extends TestCase {
    public static void main(String[] args) {
        try {
            new FreeMarkerTestCase().testTemplate();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void testTemplate() throws Exception {
        // Create a temporary directory for the tests
        final File testDir = new File(System.getProperty("java.io.tmpdir"),
                "FreeMarkerTestCase");
        testDir.mkdir();

        // Create a temporary template file
        final File testFile = File.createTempFile("test", ".ftl", testDir);
        final FileWriter fw = new FileWriter(testFile);
        fw.write("Value=${value}");
        fw.close();

        final Configuration fmc = new Configuration();
        fmc.setDirectoryForTemplateLoading(testDir);
        final Map<String, Object> map = new TreeMap<String, Object>();
        map.put("value", "myValue");

        final String result = new TemplateRepresentation(testFile.getName(),
                fmc, map, MediaType.TEXT_PLAIN).getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        testFile.delete();
        testDir.delete();
    }

}
