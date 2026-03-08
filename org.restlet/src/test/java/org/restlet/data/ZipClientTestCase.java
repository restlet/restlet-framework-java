/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test case for the Zip client connector.
 *
 * @author Remi Dewitte
 */
@Disabled("flaky on github")
public class ZipClientTestCase {

    private File zipFile;

    @BeforeEach
    protected void setUpEach() throws Exception {
        Path testCaseDirectoryPath = Files.createTempDirectory("ZipClientTestCase");
        zipFile = testCaseDirectoryPath.resolve("test.zip").toFile();
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
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

        // Write test.txt as first entry
        ClientResource testFileEntryClientResource = new ClientResource(testFileEntryReference);
        testFileEntryClientResource.put(new StringRepresentation(text));
        assertEquals(testFileEntryClientResource.getStatus(), Status.SUCCESS_CREATED);

        // Get the text and compare to the original
        testFileEntryClientResource.get();
        assertEquals(testFileEntryClientResource.getStatus(), Status.SUCCESS_OK);
        assertEquals(testFileEntryClientResource.getResponseEntity().getText(), text);
        testFileEntryClientResource.release();

        // Write test2.txt as second entry
        ClientResource test2FileEntryClientResource = new ClientResource(test2FileEntryReference);
        test2FileEntryClientResource.put(new StringRepresentation(text2));
        assertEquals(test2FileEntryClientResource.getStatus(), Status.SUCCESS_OK);

        // Check that the first entry has not been overwritten
        testFileEntryClientResource.get();
        assertEquals(testFileEntryClientResource.getStatus(), Status.SUCCESS_OK);
        assertEquals(testFileEntryClientResource.getResponseEntity().getText(), text);
        testFileEntryClientResource.release();

        // Put a directory
        ClientResource dirEntryClientResource = new ClientResource(dirEntryReference);
        dirEntryClientResource.put(new EmptyRepresentation());
        assertEquals(dirEntryClientResource.getStatus(), Status.SUCCESS_OK);

        dirEntryClientResource.get();
        assertEquals(dirEntryClientResource.getStatus(), Status.SUCCESS_OK);

        // Add a file inside the directory
        ClientResource testFileInDirEntryCLientResource = new ClientResource(test3FileInDirEntryReference);
        testFileInDirEntryCLientResource.put(new StringRepresentation(text));
        assertEquals(testFileInDirEntryCLientResource.getStatus(), Status.SUCCESS_OK);

        // Check that the second entry is still there
        test2FileEntryClientResource.get();
        assertEquals(test2FileEntryClientResource.getStatus(), Status.SUCCESS_OK, "Could not get " + test2FileEntryReference);
        assertEquals(test2FileEntryClientResource.getResponseEntity().getText(), text2);

        // Check that content negotiation does not work
        ClientResource rTest2 = new ClientResource(zr + "!test2");
        assertThrows(ResourceException.class, rTest2::get);

        // Try to replace file by directory
        ClientResource r2d = new ClientResource(test2FileEntryReference + "/");
        assertThrows(ResourceException.class, () -> r2d.put(new EmptyRepresentation()));
    }
}
