package org.restlet.test;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.velocity.TemplateRepresentation;

public class VelocityTestCase extends TestCase {
	public static void main(String[] args) {
		try {
			new VelocityTestCase().testTemplate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testTemplate() throws Exception {
		// Create a temporary directory for the tests
		File testDir = new File(System.getProperty("java.io.tmpdir"),
				"VelocityTestCase");
		testDir.mkdir();

		// Create a temporary template file
		File testFile = File.createTempFile("test", ".vm", testDir);
		FileWriter fw = new FileWriter(testFile);
		fw.append("Value=$value");
		fw.close();

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("value", "myValue");

		TemplateRepresentation tr = new TemplateRepresentation(testFile
				.getName(), map, MediaType.TEXT_PLAIN);
		tr.getEngine().setProperty("file.resource.loader.path",
				testDir.getAbsolutePath());

		String result = tr.getValue();
		assertEquals("Value=myValue", result);

		// Clean-up
		testFile.delete();
		testDir.delete();
	}

}
