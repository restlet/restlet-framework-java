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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * ResolvingTransformerTestCase tests the resolving aspects of the
 * Transformer/TransformerRepresentation to guarantee proper functioning of the xsl :import,
 * :include and document() features.
 *
 * @author Marc Portier
 */
class ResolvingTransformerTestCase {

    record AssertResolvingHelper(String baseUri, URIResolver resolver) {

        /** Asserts that the testUri resolves into the expectedUri */
        void assertResolving(String message, String testUri, String testData)
                throws TransformerException, IOException {
            StringBuilder data = new StringBuilder();

            Source resolvedSource = this.resolver.resolve(testUri, this.baseUri);
            assertNotNull(resolvedSource, "resolved source for " + testUri + " should not be null");

            if (resolvedSource instanceof StreamSource streamSource) {
                Reader dataReader = streamSource.getReader();

                if (dataReader == null) {
                    InputStream in = (streamSource.getInputStream());
                    assertNotNull(in, "no reader or inputStream available");
                    dataReader = new InputStreamReader(in);
                }

                assertNotNull(dataReader, "no reader to data in source.");
                char[] buf = new char[1024];
                int len;
                while ((len = dataReader.read(buf)) != -1) {
                    data.append(buf, 0, len);
                }

                dataReader.close();
            } else {
                // TODO support other source implementations (namely sax-source implementations)
                fail(
                        "test implementation currently doesn't handle other source (e.g., sax) implementations");
            }
            assertEquals(testData, data.toString(), message);
        }
    }

    static class SimpleUriMapApplication extends Application {
        private final Map<String, Representation> uriMap = new HashMap<>();

        public SimpleUriMapApplication() {
            // Turn off the useless extension tunnel.
            getTunnelService().setExtensionsTunnel(false);
        }

        void add(String uri, Representation rep) {
            this.uriMap.put(uri, rep);
        }

        @Override
        public Restlet createInboundRoot() {
            return new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    String remainder = request.getResourceRef().getRemainingPart();
                    Representation answer = SimpleUriMapApplication.this.uriMap.get(remainder);

                    if (answer != null) {
                        response.setEntity(answer);
                    }
                }
            };
        }
    }

    private static final String MY_BASEPATH;

    private static final String MY_NAME;

    private static final String MY_PATH;

    static {
        MY_PATH = ResolvingTransformerTestCase.class.getName().replace('.', '/');
        final int lastPos = MY_PATH.lastIndexOf('/');
        MY_NAME = MY_PATH.substring(lastPos);
        MY_BASEPATH = MY_PATH.substring(0, lastPos);
    }

    // testing purely the resolver, no active transforming context (i.e., xslt engine) in this test
    @Test
    void testResolving() throws Exception {
        Component comp = new Component();

        // create an xml input representation
        Representation xml =
                new StringRepresentation("<?xml version='1.0'><simpleroot/>", MediaType.TEXT_XML);

        // create a xsl template representation
        Representation xslt =
                new StringRepresentation(
                        "<?xml version=\"1.0\"?>"
                                + "<xsl:transform xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>"
                                + "<xsl:template match ='/'><newroot/></xsl:template></xsl:transform>",
                        MediaType.TEXT_XML);

        TransformRepresentation transRep =
                new TransformRepresentation(comp.getContext(), xml, xslt);

        // create a test-stream representation to be returned when the correct
        // code is presented
        String testCode = "rnd." + (new Random()).nextInt();
        String testData = "\"The resolver is doing OK\", said the testclass " + MY_NAME + ".";
        Representation testRep = new StringRepresentation(testData);

        SimpleUriMapApplication testApp = new SimpleUriMapApplication();
        testApp.add(testCode, testRep);

        comp.getInternalRouter().attach("/testApp/", testApp);
        String testBase = "riap://component/testApp";

        URIResolver uriResolver = transRep.getUriResolver();
        assertNotNull(uriResolver, "no resolver present!");

        String baseUri = testBase + "/dummy";

        AssertResolvingHelper test = new AssertResolvingHelper(baseUri, uriResolver);

        String absoluteUri = testBase + "/" + testCode;
        test.assertResolving("error in absolute resolving.", absoluteUri, testData);

        test.assertResolving("error in relative resolving.", testCode, testData);

        String relLocalUri = "./" + testCode;
        test.assertResolving("error in relative resolving to ./", relLocalUri, testData);

        String relParentUri = "../testApp/" + testCode;
        test.assertResolving("error in relative resolving to ../", relParentUri, testData);
    }

    // functional test in the actual xslt engine context
    @Test
    void testTransform() throws Exception {

        Component comp = new Component();
        comp.getClients().add(Protocol.CLAP);

        // here is the plan / setup
        // * make a transformer from clap://**/xslt/one/1st.xsl
        // * let it import a relative xsl ../two/2nd.xsl
        // * let that in turn import a riap://component/three/3rd.xsl
        // * provide input-xml-structure input/element-1st..-3rd/**
        // * let each xsl call-in as well an extra document() 1st-3rd.xml with a
        // simple <data>1st</data>
        // * each xsl should provide the template for one of the lines
        // * output should show all converted lines as read from the various
        // external documents

        String thirdDocData = "<data3>" + ("rnd." + (new Random()).nextInt()) + "</data3>";
        String xsl2xmlLink = "./3rd.xml"; // and "/three/3rd.xml" would too...
        // Note: setting xsl2xmlLink to "riap://application/3rd.xml" does not work.
        // Cause: the application-context one refers to with above is the one
        // that is creating the xslt sheet (and the associated uri-resolver).
        // Since that isn't an actual application-context, so it doesn't support
        // the riap-authority 'application'

        Representation xml3 =
                new StringRepresentation(
                        "<?xml version='1.0' ?>" + thirdDocData, MediaType.TEXT_XML);
        Representation xslt3 =
                new StringRepresentation(
                        "<?xml version=\"1.0\"?>"
                                + "<xsl:transform xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>"
                                + "  <xsl:template match ='el3'>"
                                + "    <xsl:variable name='external' select=\"document('"
                                + xsl2xmlLink
                                + "')\" />"
                                + "    <xsl:copy-of select='$external/data3' />"
                                + "  </xsl:template>"
                                + "</xsl:transform>",
                        MediaType.TEXT_XML);
        SimpleUriMapApplication thirdLevel = new SimpleUriMapApplication();
        thirdLevel.add("3rd.xsl", xslt3);
        thirdLevel.add("3rd.xml", xml3);
        comp.getInternalRouter().attach("/three/", thirdLevel);

        // xml In
        Representation xmlIn =
                new StringRepresentation(
                        "<?xml version='1.0' ?><input><one/><any attTwo='2'/><el3>drie</el3></input>");
        // xslOne
        Reference xsltOneRef =
                new LocalReference("clap://thread/" + MY_BASEPATH + "/xslt/one/1st.xsl");
        Representation xsltOne =
                comp.getContext()
                        .getClientDispatcher()
                        .handle(new Request(Method.GET, xsltOneRef))
                        .getEntity();
        TransformRepresentation tr = new TransformRepresentation(comp.getContext(), xmlIn, xsltOne);

        // TODO transformer output should go to SAX! The sax-event-stream should
        // then be fed into a DOMBuilder
        // and then the assertions should be written as DOM tests...
        // (NOTE: current string-compare assertion might fail on lexical aspects
        // as ignorable whitespace, encoding settings etc etc)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tr.write(out);
        String xmlOut = out.toString();

        String expectedResult =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><output><data1>1st</data1><data2>2nd</data2>"
                        + thirdDocData
                        + "</output>";
        assertEquals(expectedResult, xmlOut, "xslt result doesn't match expectations");
    }
}
