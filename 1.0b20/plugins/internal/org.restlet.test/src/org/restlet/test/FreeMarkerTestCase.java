package org.restlet.test;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;


import freemarker.template.Configuration;

public class FreeMarkerTestCase extends TestCase
{
	public static void main(String[] args)
	{
		try
		{
			new FreeMarkerTestCase().testTemplate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void testTemplate() throws Exception
	{
		// Create a temporary directory for the tests
		File testDir = new File(System.getProperty("java.io.tmpdir"), "FreeMarkerTestCase");
		testDir.mkdir();

		// Create a temporary template file
		File testFile = File.createTempFile("test", ".ftl", testDir);
		FileWriter fw = new FileWriter(testFile);
		fw.append("Value=${value}");
		fw.close();

		Configuration fmc = new Configuration();
		fmc.setDirectoryForTemplateLoading(testDir);
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("value", "myValue");
		
		String result = new TemplateRepresentation(testFile.getName(), fmc, map, MediaType.TEXT_PLAIN).getValue();
		assertEquals("Value=myValue", result);

		// Clean-up
		testFile.delete();
		testDir.delete();
	}

}
