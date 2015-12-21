/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.xml;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.TransformRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Test case for the {@link TransformRepresentation} class.
 * 
 * @author Jerome Louvel
 */
public class TransformRepresentationTestCase extends RestletTestCase {

    final String output1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><buyer>cust123</buyer>";

    final String output2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><myBuyer>cust123</myBuyer>";

    // Create a source XML document
    final Representation source = new StringRepresentation(
            "<?xml version=\"1.0\"?>" + "<purchase id=\"p001\">"
                    + "<customer db=\"cust123\"/>" + "<product db=\"prod345\">"
                    + "<amount>23.45</amount>" + "</product>" + "</purchase>",
            MediaType.TEXT_XML);

    // Create a first transform XSLT sheet
    final Representation xslt1 = new StringRepresentation(
            "<?xml version=\"1.0\"?>"
                    + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                    + "<xsl:template match =\"customer\">"
                    + "<buyer><xsl:value-of select=\"@db\"/></buyer>"
                    + "</xsl:template><xsl:template match =\"amount\"/>"
                    + "</xsl:transform>", MediaType.TEXT_XML);

    // Create a second transform XSLT sheet
    final Representation xslt2 = new StringRepresentation(
            "<?xml version=\"1.0\"?>"
                    + "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                    + "<xsl:template match =\"buyer\">"
                    + "<myBuyer><xsl:value-of select=\"text()\"/></myBuyer>"
                    + "</xsl:template>" + "</xsl:transform>",
            MediaType.TEXT_XML);

    public void testSingleTransform() throws Exception {
        TransformRepresentation tr1 = new TransformRepresentation(this.source,
                this.xslt1);
        final String result = tr1.getText();
        assertEquals(this.output1, result);
    }

    public void testDoubleTransform() throws Exception {
        TransformRepresentation tr1 = new TransformRepresentation(this.source,
                this.xslt1);
        TransformRepresentation tr2 = new TransformRepresentation(tr1,
                this.xslt2);
        final String result = tr2.getText();
        assertEquals(this.output2, result);
    }

}
