/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.test;

import java.io.File;
import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit test case for the Zip client connector.
 * 
 * @author Remi Dewitte
 */
public class ZipClientTestCase extends RestletTestCase {

    public void testFileClient() throws IOException, InterruptedException {
        File zipFile = File.createTempFile("Restlet", ".zip");
        // We just wanted a valid writable path
        zipFile.delete();
        final String text = "Test content\r\nLine 2\r\nLine2";
        final String text2 = "Test content\nLine 2";
        final Client fc = new Client(Protocol.ZIP);
        final LocalReference fr = LocalReference.createFileReference(zipFile);
        final Reference zr = new Reference("zip:" + fr.toString());
        final String fzr = zr + "!/test.txt";
        final String fzd = zr + "!/dir/";
        final String fzr2 = fzd + "test2.txt";

        // Write the text to file
        Response response = fc.put(fzr, new StringRepresentation(text));
        assertTrue(response.getStatus().equals(Status.SUCCESS_CREATED));

        // Get the text and compare to the original
        response = fc.get(fzr);
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
        assertEquals(response.getEntityAsText(), text);
        response.getEntity().release();

        // Write the text to file
        response = fc.put(fzr2, new StringRepresentation(text2));
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

        // Checking first one was not overwritten
        response = fc.get(fzr);
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));
        assertEquals(response.getEntityAsText(), text);
        response.getEntity().release();

        // Put a directory
        response = fc.put(fzd, new EmptyRepresentation());
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

        response = fc.get(fzd);
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

        // Checking second one was output
        response = fc.get(fzr2);
        assertTrue("Could not get " + fzr2, response.getStatus().equals(
                Status.SUCCESS_OK));
        assertEquals(response.getEntityAsText(), text2);

        response = fc.get(zr + "!test2");
        assertFalse(response.getStatus().equals(Status.SUCCESS_OK));

        // Try to replace file by directory
        response = fc.put(fzr2 + "/", new EmptyRepresentation());
        assertFalse(response.getStatus().equals(Status.SUCCESS_OK));

    }

}
