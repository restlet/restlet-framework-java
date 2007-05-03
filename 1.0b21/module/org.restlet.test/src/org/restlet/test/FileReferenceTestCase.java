package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.data.LocalReference;

/**
 * Unit test case for the File Reference parsing.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class FileReferenceTestCase extends TestCase
{

	public void testCreation()
	{
		String path = "D:\\Restlet\\build.xml";
		LocalReference fr = LocalReference.createFileReference(path);
		fr.getFile();

		assertEquals("file", fr.getScheme());
		assertEquals("", fr.getAuthority());
		assertEquals("/D:/Restlet/build.xml", fr.getPath());
	}

}
