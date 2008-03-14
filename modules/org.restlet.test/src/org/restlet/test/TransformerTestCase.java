/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Transformer;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * Test case for the Transformer class.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
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
