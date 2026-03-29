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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.engine.Engine;
import org.restlet.engine.local.ZipClientHelper;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Unit test case for the Zip client connector.
 *
 * @author Remi Dewitte
 */
class ZipClientTestCase {

    private File zipFile;

    @BeforeEach
    void setUpEach() throws Exception {
        Path testCaseDirectoryPath = Files.createTempDirectory("ZipClientTestCase");
        zipFile = testCaseDirectoryPath.resolve("test.zip").toFile();
        Engine.getInstance().getRegisteredClients().add(new ZipClientHelper(null));
    }

    @AfterEach
    void tearDownEach() {
        zipFile.delete();
    }

    @Test
    void testFileClient() throws IOException {
        String text = "Test content\r\nLine 2\r\nLine2";
        String text2 = "Test content\nLine 2";
        LocalReference fr = LocalReference.createFileReference(zipFile);
        Reference zr = new Reference("zip:" + fr);
        String testFileEntryReference = zr + "!/test.txt";
        String test2FileEntryReference = zr + "!/test2.txt";
        String dirEntryReference = zr + "!/dir/";
        String test3FileInDirEntryReference = dirEntryReference + "test3.txt";

        // Write test.txt as the first entry
        ClientResource testFileEntryClientResource = new ClientResource(testFileEntryReference);
        testFileEntryClientResource.put(new StringRepresentation(text));
        assertEquals(Status.SUCCESS_CREATED, testFileEntryClientResource.getStatus());

        // Get the text and compare to the original
        testFileEntryClientResource.get();
        assertEquals(Status.SUCCESS_OK, testFileEntryClientResource.getStatus());
        assertEquals(text, testFileEntryClientResource.getResponseEntity().getText());
        testFileEntryClientResource.release();

        // Write test2.txt as the second entry
        ClientResource test2FileEntryClientResource = new ClientResource(test2FileEntryReference);
        test2FileEntryClientResource.put(new StringRepresentation(text2));
        assertEquals(Status.SUCCESS_OK, test2FileEntryClientResource.getStatus());

        // Check that the first entry has not been overwritten
        testFileEntryClientResource.get();
        assertEquals(Status.SUCCESS_OK, testFileEntryClientResource.getStatus());
        assertEquals(text, testFileEntryClientResource.getResponseEntity().getText());
        testFileEntryClientResource.release();

        // Put a directory
        ClientResource dirEntryClientResource = new ClientResource(dirEntryReference);
        dirEntryClientResource.put(new EmptyRepresentation());
        assertEquals(Status.SUCCESS_OK, dirEntryClientResource.getStatus());

        dirEntryClientResource.get();
        assertEquals(Status.SUCCESS_OK, dirEntryClientResource.getStatus());

        // Add a file inside the directory
        ClientResource testFileInDirEntryCLientResource =
                new ClientResource(test3FileInDirEntryReference);
        testFileInDirEntryCLientResource.put(new StringRepresentation(text));
        assertEquals(Status.SUCCESS_OK, testFileInDirEntryCLientResource.getStatus());

        // Check that the second entry is still there
        test2FileEntryClientResource.get();
        assertEquals(
                Status.SUCCESS_OK,
                test2FileEntryClientResource.getStatus(),
                "Could not get " + test2FileEntryReference);
        assertEquals(text2, test2FileEntryClientResource.getResponseEntity().getText());

        // Check that content negotiation does not work
        ClientResource rTest2 = new ClientResource(zr + "!test2");
        assertThrows(ResourceException.class, rTest2::get);

        // Try to replace a file by directory
        ClientResource r2d = new ClientResource(test2FileEntryReference + "/");
        final EmptyRepresentation entity = new EmptyRepresentation();
        assertThrows(ResourceException.class, () -> r2d.put(entity));
    }
}
