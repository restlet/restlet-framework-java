
package org.restlet.test;

import org.restlet.data.FileReference;

import junit.framework.TestCase;

/**
 * Unit test case for the File Reference parsing.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class FileReferenceTestCase extends TestCase
{

	public void testCreation()
	{
		String path = "D:\\Restlet\\build.xml";
		FileReference fr = new FileReference(path);
		fr.getFile();
		
		assertEquals("file", fr.getScheme());
		assertEquals("", fr.getAuthority());
		assertEquals("/D:/Restlet/build.xml", fr.getPath());
	}
	
}
