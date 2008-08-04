/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
