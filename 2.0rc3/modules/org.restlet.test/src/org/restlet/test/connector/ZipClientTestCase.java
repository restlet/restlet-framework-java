/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the Zip client connector.
 * 
 * @author Remi Dewitte
 */
public class ZipClientTestCase extends RestletTestCase {

    public void testFileClient() throws IOException, InterruptedException {
        File zipFile = File.createTempFile("Restlet", ".zip");
        // We just wanted a valid writable path
        BioUtils.delete(zipFile);
        String text = "Test content\r\nLine 2\r\nLine2";
        String text2 = "Test content\nLine 2";
        LocalReference fr = LocalReference.createFileReference(zipFile);
        Reference zr = new Reference("zip:" + fr.toString());
        String fzr = zr + "!/test.txt";
        String fzd = zr + "!/dir/";
        String fzr2 = fzd + "test2.txt";

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

        // Checking second one was output
        r2.get();
        assertTrue("Could not get " + fzr2, r2.getStatus().equals(
                Status.SUCCESS_OK));
        assertEquals(r2.getResponseEntity().getText(), text2);

        ClientResource rTest2 = new ClientResource(zr + "!test2");
        rTest2.get();
        assertFalse(rTest2.getStatus().equals(Status.SUCCESS_OK));

        // Try to replace file by directory
        ClientResource r2d = new ClientResource(fzr2 + "/");
        r2d.put(new EmptyRepresentation());
        assertFalse(r2d.getStatus().equals(Status.SUCCESS_OK));

    }

}
