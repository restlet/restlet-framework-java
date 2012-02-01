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

package org.restlet.test.ext.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Before;
import org.restlet.data.MediaType;
import org.restlet.engine.util.DefaultSaxHandler;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;
import org.xml.sax.SAXException;

/**
 * Basic JUnit test case for parsing and validating two well-formed restlet.xml
 * files with and without the {@code xmlns} attribute and two tests for parsing
 * and validating an invalid restlet.xml which violates the Component.xsd
 * schema.
 */
public class RestletXmlTestCase extends RestletTestCase {
    private static final String _XML_BODY = "<server protocol=\"HTTP\" port=\"9090\"/>\n"
            + "<server protocol=\"HTTP\" port=\"9091\"/>\n"
            + "<defaultHost hostPort=\"9091\">\n"
            + "<attach uriPattern=\"/abcd\" "
            + "targetClass=\"org.restlet.test.HelloWorldApplication\"/>\n"
            + "</defaultHost>\n"
            + "<host hostPort=\"9090\">\n"
            + "<attach uriPattern=\"/efgh\" "
            + "targetClass=\"org.restlet.test.HelloWorldApplication\"/>\n"
            + "</host>\n" + "</component>\n";

    private static final String BAD_XML = "<?xml version=\"1.0\"?>\n"
            + "<component xmlns=\"http://www.restlet.org/schemas/2.0/Component\">\n"
            + "<bad-element bad-attribute=\"some-value\">abcd</bad-element>"
            + _XML_BODY;

    private static final String XML_WITH_XMLNS = "<?xml version=\"1.0\"?>\n"
            + "<component xmlns=\"http://www.restlet.org/schemas/2.0/Component\">\n"
            + _XML_BODY;

    private static final String XML_WITHOUT_XMLNS = "<?xml version=\"1.0\"?>\n"
            + "<component>\n" + _XML_BODY;

    private DocumentBuilder builder;

    private Validator validator;

    // default 0-arguments constructor

    private Source getAsSource(String xmlString) {
        return new StreamSource(getAsStream(xmlString));
    }

    private InputStream getAsStream(String xmlString) {
        return new ByteArrayInputStream(xmlString.getBytes());
    }

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(true);

        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        DefaultSaxHandler handler = new DefaultSaxHandler();
        schemaFactory.setErrorHandler(handler);
        // [ifndef android] line
        schemaFactory.setResourceResolver(handler);
        InputStream is = getClass().getResourceAsStream(
                "/org/restlet/Component.xsd");
        assertNotNull("Component.xsd stream MUST NOT be null", is);
        StreamSource ss = new StreamSource(is);
        Schema schema = schemaFactory.newSchema(ss);
        dbf.setSchema(schema);

        builder = dbf.newDocumentBuilder();
        builder.setErrorHandler(handler);
        builder.setEntityResolver(handler);
        validator = schema.newValidator();
    }

    @Override
    protected void tearDown() throws Exception {
        builder = null;
        validator = null;
        super.tearDown();
    }

    public void testParserBadXML() {
        System.out.println("-- testParserBadXML");
        try {
            builder.parse(getAsStream(BAD_XML));
            assertTrue(true);
        } catch (SAXException x) {
            fail("MUST be able to parse a good restlet.xml with xmlns attribute");
        } catch (IOException x) {
            fail("MUST be able to parse a good restlet.xml with xmlns attribute");
        }
    }

    public void testParserWithoutXMLNS() {
        System.out.println("-- testParserWithoutXMLNS");
        try {
            builder.parse(getAsStream(XML_WITHOUT_XMLNS));
            assertTrue(true);
        } catch (SAXException x) {
            fail("MUST be able to parse a good restlet.xml without xmlns attribute");
        } catch (IOException x) {
            fail("MUST be able to parse a good restlet.xml without xmlns attribute");
        }
    }

    public void testParserWithXMLNS() {
        System.out.println("-- testParserWithXMLNS");
        try {
            builder.parse(getAsStream(XML_WITH_XMLNS));
            assertTrue(true);
        } catch (SAXException x) {
            fail("MUST be able to parse a good restlet.xml with xmlns attribute");
        } catch (IOException x) {
            fail("MUST be able to parse a good restlet.xml with xmlns attribute");
        }
    }

    public void testValidateMethod() {
        System.out.println("-- testValidateMethod");
        InputStream is = getClass().getResourceAsStream(
                "/org/restlet/Component.xsd");
        assertNotNull("Component.xsd stream MUST NOT be null", is);
        Representation schemaRepresentation = new InputRepresentation(is,
                MediaType.APPLICATION_W3C_SCHEMA);

        DomRepresentation configRepresentation = new DomRepresentation(
                new StringRepresentation(XML_WITH_XMLNS));

        try {
            configRepresentation.validate(schemaRepresentation);
            assertTrue(true);
        } catch (Exception x) {
            x.printStackTrace(System.err);
            fail(x.getLocalizedMessage());
        }
    }

    public void testValidatorBadXML() {
        System.out.println("-- testValidatorBadXML");
        try {
            validator.validate(getAsSource(BAD_XML));
            fail("MUST NOT be able to validate bad restlet.xml");
        } catch (SAXException x) {
            // the error must be a "cvc-complex-type.2.4.a"
            assertTrue("MUST detect schema violation", x.getLocalizedMessage()
                    .startsWith("cvc-complex-type.2.4.a"));
            // ...and it has to refer to 'bad-element'
            assertTrue("MUST detect schema violation related to 'bad-element'",
                    x.getLocalizedMessage().indexOf("bad-element") > 0);

        } catch (IOException x) {
            fail("MUST throw a SAXException only");
        }
    }

    public void testValidatorWithoutXMLNS() {
        System.out.println("-- testValidatorWithoutXMLNS");
        try {
            validator.validate(getAsSource(XML_WITHOUT_XMLNS));
            fail("MUST NOT be able to validate restlet.xml without xmlns attribute");
        } catch (SAXException x) {
            assertTrue(true);
        } catch (IOException x) {
            fail("");
        }
    }

    public void testValidatorWithXMLNS() {
        System.out.println("-- testValidatorWithXMLNS");
        try {
            validator.validate(getAsSource(XML_WITH_XMLNS));
            assertTrue(true);
        } catch (SAXException x) {
            fail("MUST be able to validate restlet.xml with xmlns attribute");
        } catch (IOException x) {
            fail("MUST be able to validate restlet.xml with xmlns attribute");
        }
    }
}
