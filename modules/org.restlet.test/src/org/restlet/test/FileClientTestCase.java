/*
 * Copyright 2005-2007 Noelios Consulting.
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
import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.StringRepresentation;

/**
 * Unit test case for the File client connector.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class FileClientTestCase extends TestCase {

    public void testFileClient() throws IOException {
        String text = "Test content\r\nLine 2\r\nLine2";
        Client fc = new Client(Protocol.FILE);
        LocalReference fr = LocalReference.createFileReference(File
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
