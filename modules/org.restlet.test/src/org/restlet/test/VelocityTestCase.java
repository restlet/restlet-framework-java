/*
 * Copyright 2005-2008 Noelios Technologies.
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

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.restlet.resource.Representation;

/**
 * Test case for the Velocity extension.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class VelocityTestCase extends TestCase {

    public void testRepresentationTemplate() throws Exception {
        // Create a temporary directory for the tests
        final File testDir = new File(System.getProperty("java.io.tmpdir"),
                "VelocityTestCase");
        testDir.mkdir();

        // Create a temporary template file
        final File testFile = File.createTempFile("test", ".vm", testDir);
        final FileWriter fw = new FileWriter(testFile);
        fw.write("Value=$value");
        fw.close();

        final Map<String, Object> map = new TreeMap<String, Object>();
        map.put("value", "myValue");

        // Representation approach
        final Client client = new Client(Protocol.FILE);
        final Reference ref = LocalReference.createFileReference(testFile);
        final Representation templateFile = client.get(ref).getEntity();
        final TemplateRepresentation tr = new TemplateRepresentation(
                templateFile, map, MediaType.TEXT_PLAIN);
        final String result = tr.getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        testFile.delete();
        testDir.delete();
    }

    public void testStandardTemplate() throws Exception {
        // Create a temporary directory for the tests
        final File testDir = new File(System.getProperty("java.io.tmpdir"),
                "VelocityTestCase");
        testDir.mkdir();

        // Create a temporary template file
        final File testFile = File.createTempFile("test", ".vm", testDir);
        final FileWriter fw = new FileWriter(testFile);
        fw.write("Value=$value");
        fw.close();

        final Map<String, Object> map = new TreeMap<String, Object>();
        map.put("value", "myValue");

        // Standard approach
        final TemplateRepresentation tr = new TemplateRepresentation(testFile
                .getName(), map, MediaType.TEXT_PLAIN);
        tr.getEngine().setProperty("file.resource.loader.path",
                testDir.getAbsolutePath());
        final String result = tr.getText();
        assertEquals("Value=myValue", result);

        // Clean-up
        testFile.delete();
        testDir.delete();
    }
}
