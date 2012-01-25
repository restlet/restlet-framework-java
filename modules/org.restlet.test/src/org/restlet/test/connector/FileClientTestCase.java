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

import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.RestletTestCase;

/**
 * Unit test case for the File client connector.
 * 
 * @author Jerome Louvel
 */
public class FileClientTestCase extends RestletTestCase {

    public void testFileClient() throws IOException {
        String text = "Test content\r\nLine 2\r\nLine2";
        LocalReference fr = LocalReference
                .createFileReference(File.createTempFile("Restlet", ".txt."
                        + Language.DEFAULT.getName()));

        ClientResource resource = new ClientResource(fr);
        try {
            // Update the text of the temporary file
            resource.put(new StringRepresentation(text));
        } catch (ResourceException e) {
        }
        assertTrue(resource.getStatus().isSuccess());

        try {
            // Get the text and compare to the original
            resource.get();
        } catch (ResourceException e) {
        }
        assertEquals(Status.SUCCESS_OK, resource.getStatus());

        try {
            // Delete the file
            resource.delete();
        } catch (ResourceException e) {
        }
        assertEquals(Status.SUCCESS_NO_CONTENT, resource.getStatus());
    }
}
