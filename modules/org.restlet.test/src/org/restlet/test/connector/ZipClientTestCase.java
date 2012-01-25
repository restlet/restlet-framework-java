/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.connector;

import java.io.File;
import java.io.IOException;

import org.restlet.data.LocalReference;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.io.BioUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the Zip client connector.
 * 
 * @author Remi Dewitte
 */
public class ZipClientTestCase extends RestletTestCase {

    private File testDir;

    private File zipFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testDir = new File(System.getProperty("java.io.tmpdir"),
                "zipClientTestCase");
        BioUtils.delete(testDir, true);
        testDir.mkdirs();
        zipFile = new File(testDir, "test.zip");
    }

    @Override
    protected void tearDown() throws Exception {
        // BioUtils.delete(testDir, true);
    }

    public void testFileClient() throws IOException, InterruptedException {
        String text = "Test content\r\nLine 2\r\nLine2";
        String text2 = "Test content\nLine 2";
        LocalReference fr = LocalReference.createFileReference(zipFile);
        Reference zr = new Reference("zip:" + fr.toString());
        String fzr = zr + "!/test.txt";
        String fzd = zr + "!/dir/";
        String fzr2 = zr + "!/test2.txt";
        String fzr3 = fzd + "test3.txt";

        // Write the text to file
        ClientResource r = new ClientResource(fzr);
        r.put(new StringRepresentation(text));
        assertTrue(r.getStatus().equals(Status.SUCCESS_CREATED));

        // Get the text and compare to the original
        r.get();
        assertTrue(r.getStatus().equals(Status.SUCCESS_OK));
        assertEquals(r.getResponseEntity().getText(), text);
        r.release();

        // Write the text to file
        ClientResource r2 = new ClientResource(fzr2);
        r2.put(new StringRepresentation(text2));
        assertTrue(r2.getStatus().equals(Status.SUCCESS_OK));

        // Checking first one was not overwritten
        r.get();
        assertTrue(r.getStatus().equals(Status.SUCCESS_OK));
        assertEquals(r.getResponseEntity().getText(), text);
        r.release();

        // Put a directory
        ClientResource rd = new ClientResource(fzd);
        rd.put(new EmptyRepresentation());
        assertTrue(rd.getStatus().equals(Status.SUCCESS_OK));

        rd.get();
        assertTrue(rd.getStatus().equals(Status.SUCCESS_OK));

        ClientResource r3 = new ClientResource(fzr3);
        r3.put(new StringRepresentation(text));
        assertTrue(r3.getStatus().equals(Status.SUCCESS_OK));

        // Checking second one was output
        r2.get();
        assertTrue("Could not get " + fzr2,
                r2.getStatus().equals(Status.SUCCESS_OK));
        assertEquals(r2.getResponseEntity().getText(), text2);

        try {
            ClientResource rTest2 = new ClientResource(zr + "!test2");
            rTest2.get();
            fail();
        } catch (ResourceException e) {
        }

        // Try to replace file by directory
        try {
            ClientResource r2d = new ClientResource(fzr2 + "/");
            r2d.put(new EmptyRepresentation());
            fail();
        } catch (ResourceException e) {
        }
    }
}
