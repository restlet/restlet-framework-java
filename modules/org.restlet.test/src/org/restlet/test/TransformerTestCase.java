/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

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

        Transformer transformer = new Transformer(Transformer.MODE_REQUEST,
                xslt);
        String result = transformer.transform(source).getText();

        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><buyer>cust123</buyer>23.45",
                result);
    }

}
