/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.regression;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.xml.sax.InputSource;

/**
 * Simple test case to illustrate defect #717 and validate the fix when applied.
 */
public class Bug717TestCase extends TestCase {

    private static final String RESTLET_XML = "<?xml version=\"1.0\"?>\n"
            + "<component xmlns=\"http://www.restlet.org/schemas/2.0/Component\">\n"
            + "<server protocol=\"HTTP\" port=\"9090\"/>\n"
            + "<server protocol=\"HTTP\" port=\"9091\"/>\n"
            + "<defaultHost hostPort=\"9091\">\n"
            + "<attach uriPattern=\"/abcd\" targetClass=\"org.restlet.test.HelloWorldApplication\"/>\n"
            + "</defaultHost>\n"
            + "<host hostPort=\"9090\">\n"
            + "<attach uriPattern=\"/efgh\" targetClass=\"org.restlet.test.HelloWorldApplication\"/>\n"
            + "</host>\n" + "</component>\n";

    public void test() throws IOException {
        InputStream inStr = getClass().getResourceAsStream(
                "/org/restlet/Component.xsd");
        assertNotNull("Component.xsd input stream MUST NOT be null", inStr);

        InputSource inSrc = new InputSource(inStr);
        assertNotNull("Component.xsd SAX input source MUST NOT be null", inSrc);

        SaxRepresentation schema = new SaxRepresentation(
                MediaType.APPLICATION_W3C_SCHEMA, inSrc);
        assertNotNull("Component.xsd SAX Representation MUST NOT be null",
                schema);

        Representation xmlRep = new StringRepresentation(RESTLET_XML);
        assertNotNull("Restlet.xml SAX Representation MUST NOT be null", xmlRep);

        DomRepresentation xml = new DomRepresentation(xmlRep);
        assertNotNull("Restlet.xml DOM Representation MUST NOT be null", xml);

        try {
            xml.validate(schema);
            assertTrue(true);
        } catch (Exception x) {
            x.printStackTrace(System.err);
            fail("Bug717TestCase - Failed validating a correct restlet.xml "
                    + "representation against the current Component W3C schema: "
                    + x.getLocalizedMessage());
        }
    }
}
