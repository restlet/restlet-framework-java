
package com.noelios.restlet.test;

import com.noelios.restlet.data.FileReference;

import junit.framework.TestCase;

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
