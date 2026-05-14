/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Test case for the {@link TransformRepresentation} class.
 *
 * @author Jerome Louvel
 */
class TransformRepresentationTestCase {

    final String output1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><buyer>cust123</buyer>";

    final String output2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><myBuyer>cust123</myBuyer>";

    // Create a source XML document
    final Representation source =
            new StringRepresentation(
                    "<?xml version=\"1.0\"?>"
                            + "<purchase id=\"p001\">"
                            + "<customer db=\"cust123\"/>"
                            + "<product db=\"prod345\">"
                            + "<amount>23.45</amount>"
                            + "</product>"
                            + "</purchase>",
                    MediaType.TEXT_XML);

    // Create a first transform XSLT sheet
    final Representation xslt1 =
            new StringRepresentation(
                    "<?xml version=\"1.0\"?>"
                            + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                            + "<xsl:template match =\"customer\">"
                            + "<buyer><xsl:value-of select=\"@db\"/></buyer>"
                            + "</xsl:template><xsl:template match =\"amount\"/>"
                            + "</xsl:transform>",
                    MediaType.TEXT_XML);

    // Create a second transform XSLT sheet
    final Representation xslt2 =
            new StringRepresentation(
                    "<?xml version=\"1.0\"?>"
                            + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                            + "<xsl:template match =\"buyer\">"
                            + "<myBuyer><xsl:value-of select=\"text()\"/></myBuyer>"
                            + "</xsl:template>"
                            + "</xsl:transform>",
                    MediaType.TEXT_XML);

    @Test
    void testSingleTransform() throws Exception {
        TransformRepresentation tr1 = new TransformRepresentation(this.source, this.xslt1);
        final String result = tr1.getText();
        assertEquals(this.output1, result);
    }

    @Test
    void testDoubleTransform() throws Exception {
        TransformRepresentation tr1 = new TransformRepresentation(this.source, this.xslt1);
        TransformRepresentation tr2 = new TransformRepresentation(tr1, this.xslt2);
        final String result = tr2.getText();
        assertEquals(this.output2, result);
    }
}
