package org.restlet.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.data.FileReference;
import org.restlet.ext.data.StringRepresentation;

/**
 * Unit test case for the File client connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class FileClientTestCase extends TestCase
{

	public void testFileClient() throws IOException
	{
		String text = "Test content\r\nLine 2\r\nLine2";
		Client fc = new Client(Protocol.FILE);
		FileReference fr = new FileReference(File.createTempFile("Restlet", ".txt"));

		// Write the text to temporary file
		Response response = fc.put(fr.toString(), new StringRepresentation(text));
		assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

		// Get the text and compare to the original
		response = fc.get(fr.toString());
		assertTrue(response.getStatus().equals(Status.SUCCESS_OK));

		// Delete the file
		response = fc.delete(fr.toString());
		assertTrue(response.getStatus().equals(Status.SUCCESS_NO_CONTENT));
	}

}
