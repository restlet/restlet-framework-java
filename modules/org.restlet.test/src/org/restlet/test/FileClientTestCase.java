/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;

/**
 * Unit test case for the File client connector.
 * 
 * @author Jerome Louvel
 */
public class FileClientTestCase extends RestletTestCase {

    public void testFileClient() throws IOException {
        final String text = "Test content\r\nLine 2\r\nLine2";
        final Client fc = new Client(Protocol.FILE);
        final LocalReference fr = LocalReference.createFileReference(File
                .createTempFile("Restlet", ".txt"));

        // Write the text to temporary file
        Response response = fc.put(fr.toString(),
                new StringRepresentation(text));
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

        // Get the text and compare to the original
        response = fc.get(fr.toString());
        assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

        // Delete the file
        response = fc.delete(fr.toString());
        assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));
    }

}
