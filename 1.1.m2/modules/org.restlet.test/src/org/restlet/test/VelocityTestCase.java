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
import org.restlet.ext.velocity.TemplateRepresentation;

/**
 * Test case for the Velocity extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class VelocityTestCase extends TestCase {
    public static void main(String[] args) {
        try {
            new VelocityTestCase().testTemplate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testTemplate() throws Exception {
        // Create a temporary directory for the tests
        File testDir = new File(System.getProperty("java.io.tmpdir"),
                "VelocityTestCase");
        testDir.mkdir();

        // Create a temporary template file
        File testFile = File.createTempFile("test", ".vm", testDir);
        FileWriter fw = new FileWriter(testFile);
        fw.write("Value=$value");
        fw.close();

        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("value", "myValue");

        TemplateRepresentation tr = new TemplateRepresentation(testFile
                .getName(), map, MediaType.TEXT_PLAIN);
        tr.getEngine().setProperty("file.resource.loader.path",
                testDir.getAbsolutePath());

        String result = tr.getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        testFile.delete();
        testDir.delete();
    }

}
