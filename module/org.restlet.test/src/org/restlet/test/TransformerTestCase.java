package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Transformer;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

public class TransformerTestCase extends TestCase {
    public static void main(String[] args) {
        try {
            new TransformerTestCase().testTransform();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testTransform() throws Exception {
        // Create a source XML document
        Representation source = new StringRepresentation(
                "<?xml version=\"1.0\"?>" + "<purchase id=\"p001\">"
                        + "<customer db=\"cust123\"/>"
                        + "<product db=\"prod345\">" + "<amount>23.45</amount>"
                        + "</product>" + "</purchase>");

        // Create a transform XSLT sheet
        Representation xslt = new StringRepresentation(
                "<?xml version=\"1.0\"?>"
                        + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                        + "<xsl:template match =\"customer\">"
                        + "<buyer><xsl:value-of select=\"@db\"/></buyer>"
                        + "</xsl:template>" + "</xsl:transform>");

        Transformer transformer = new Transformer(xslt);
        String result = transformer.transform(source).getValue();

        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><buyer>cust123</buyer>23.45",
                result);
    }

}
