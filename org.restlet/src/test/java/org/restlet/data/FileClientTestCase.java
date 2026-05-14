/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

/**
 * Unit test case for the File client connector.
 *
 * @author Jerome Louvel
 */
class FileClientTestCase {

    @Test
    void testFileClient() throws IOException {
        Engine.register();
        Engine.clearThreadLocalVariables();

        String fileContent = "Test content\r\nLine 2\r\nLine2";
        File temporaryfile = File.createTempFile("Restlet", ".txt." + Language.DEFAULT.getName());
        LocalReference fileReference = LocalReference.createFileReference(temporaryfile);

        ClientResource resource = new ClientResource(fileReference);

        // Update the text of the temporary file
        resource.put(new StringRepresentation(fileContent));
        assertTrue(resource.getStatus().isSuccess());

        // Get the text and compare to the original
        resource.get();
        assertEquals(Status.SUCCESS_OK, resource.getStatus());
        assertEquals(fileContent, resource.getResponse().getEntityAsText());

        // Delete the file
        resource.delete();
        assertEquals(Status.SUCCESS_NO_CONTENT, resource.getStatus());
        Engine.clearThreadLocalVariables();
    }
}
