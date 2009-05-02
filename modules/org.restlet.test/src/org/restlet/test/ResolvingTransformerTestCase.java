/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

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

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.ext.xml.TransformRepresentation;

/**
 * ResolvingTransformerTestCase tests the resolving aspects of the
 * Transformer/TransformerRepresentation to guarantee proper functioning of the
 * xsl :import, :include and document() features.
 * 
 * @author Marc Portier (mpo@outerthought.org)
 */
public class ResolvingTransformerTestCase extends RestletTestCase {

    class AssertResolvingHelper {

        final String baseUri;

        final URIResolver resolver;

        AssertResolvingHelper(String baseUri, URIResolver resolver) {
            this.baseUri = baseUri;
            this.resolver = resolver;
        }

        /**
         * Asserts that the testUri resolves into the expectedUri
         */
        void assertResolving(final String message, final String testUri,
                final String testData) throws TransformerException, IOException {

            final Source resolvedSource = this.resolver.resolve(testUri,
                    this.baseUri);
            assertNotNull("resolved source for " + testUri
                    + " should not be null", resolvedSource);
            final StringBuilder data = new StringBuilder();
            if (resolvedSource instanceof StreamSource) {
                final StreamSource streamSource = (StreamSource) resolvedSource;
                Reader dataReader = (streamSource).getReader();
                if (dataReader == null) {
                    final InputStream in = (streamSource.getInputStream());
                    assertNotNull("no reader or inputstream available", in);
                    dataReader = new InputStreamReader(in);
                }

                assertNotNull("no reader to data in source.", dataReader);
                final char[] buf = new char[1024];
                int len = 0;
                while ((len = dataReader.read(buf)) != -1) {
                    data.append(buf, 0, len);
                }
                dataReader.close();
            } else {
                // TODO support other source implementations (namely sax-source
                // impls)
                fail("test implementation currently doesn't handle other source (e.g. sax) implementations");
            }
            assertEquals(message, testData, data.toString());
        }
    }

    class SimpleUriMapApplication extends Application {
        private final Map<String, Representation> uriMap = new HashMap<String, Representation>();

        public SimpleUriMapApplication() {
            // Turn off the useless extension tunnel.
            getTunnelService().setExtensionsTunnel(false);
        }

        void add(String uri, Representation rep) {
            this.uriMap.put(uri, rep);
        }

        @Override
        public Restlet createRoot() {
            return new Restlet() {
                @Override
                public void handle(Request request, Response response) {
                    final String remainder = request.getResourceRef()
                            .getRemainingPart();
                    final Representation answer = SimpleUriMapApplication.this.uriMap
                            .get(remainder);
                    if (answer != null) {
                        response.setEntity(answer);
                    }
                }
            };
        }
    }

    private final static String MY_BASEPATH;

    private final static String MY_NAME;

    private final static String MY_PATH;

    static {
        MY_PATH = ResolvingTransformerTestCase.class.getName()
                .replace('.', '/');
        final int lastPos = MY_PATH.lastIndexOf('/');
        MY_NAME = MY_PATH.substring(lastPos);
        MY_BASEPATH = MY_PATH.substring(0, lastPos);
    }

    // testing purely the resolver, no active transforming context (ie xslt
    // engine) in this test
    public void testResolving() throws Exception {
        final Component comp = new Component();

        // create an xml input representation
        final Representation xml = new StringRepresentation(
                "<?xml version='1.0'><simpleroot/>", MediaType.TEXT_XML);

        // create an xsl template representation
        final Representation xslt = new StringRepresentation(
                "<?xml version=\"1.0\"?>"
                        + "<xsl:transform xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>"
                        + "<xsl:template match ='/'><newroot/></xsl:template></xsl:transform>",
                MediaType.TEXT_XML);

        final TransformRepresentation transRep = new TransformRepresentation(
                comp.getContext(), xml, xslt);

        // create a test-stream representation to be returned when the correct
        // code is presented
        final String testCode = "rnd." + (new Random()).nextInt();
        final String testData = "\"The resolver is doing OK\", said the testclass "
                + MY_NAME + ".";
        final Representation testRep = new StringRepresentation(testData);

        final SimpleUriMapApplication testApp = new SimpleUriMapApplication();
        testApp.add(testCode, testRep);

        comp.getInternalRouter().attach("/testApp/", testApp);
        final String testBase = "riap://component/testApp";

        final URIResolver uriResolver = transRep.getUriResolver();
        assertNotNull("no resolver present!", uriResolver);
        final String baseUri = testBase + "/dummy";

        final AssertResolvingHelper test = new AssertResolvingHelper(baseUri,
                uriResolver);

        final String absoluteUri = testBase + "/" + testCode;
        test.assertResolving("error in absolute resolving.", absoluteUri,
                testData);

        final String relUri = testCode;
        test.assertResolving("error in relative resolving.", relUri, testData);

        final String relLocalUri = "./" + testCode;
        test.assertResolving("error in relative resolving to ./", relLocalUri,
                testData);

        final String relParentUri = "../testApp/" + testCode;
        test.assertResolving("error in relative resolving to ../",
                relParentUri, testData);
    }

    // functional test in the actual xslt engine context
    public void testTransform() throws Exception {

        final Component comp = new Component();
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

        final String thirdDocData = "<data3>"
                + ("rnd." + (new Random()).nextInt()) + "</data3>";
        // Note below doesn't work,:
        // final String xsl2xmlLink = "riap://application/3rd.xml";
        // cause: the application-context one refers to with above is the one
        // that is creating the xslt sheet
        // (and the associated uri-resolver) Since that isn't an actual
        // application-context so it doesn't support
        // the riap-authority 'application'
        // This does work though:
        final String xsl2xmlLink = "./3rd.xml"; // and "/three/3rd.xml" would
        // too...

        final Representation xml3 = new StringRepresentation(
                "<?xml version='1.0' ?>" + thirdDocData, MediaType.TEXT_XML);
        final Representation xslt3 = new StringRepresentation(
                "<?xml version=\"1.0\"?>"
                        + "<xsl:transform xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>"
                        + "  <xsl:template match ='el3'>"
                        + "    <xsl:variable name='external' select=\"document('"
                        + xsl2xmlLink + "')\" />"
                        + "    <xsl:copy-of select='$external/data3' />"
                        + "  </xsl:template>" + "</xsl:transform>",
                MediaType.TEXT_XML);
        final SimpleUriMapApplication thirdLevel = new SimpleUriMapApplication();
        thirdLevel.add("3rd.xsl", xslt3);
        thirdLevel.add("3rd.xml", xml3);
        comp.getInternalRouter().attach("/three/", thirdLevel);

        // xml In
        final Representation xmlIn = new StringRepresentation(
                "<?xml version='1.0' ?><input><one/><any attTwo='2'/><el3>drie</el3></input>");
        // xslOne
        final Reference xsltOneRef = new LocalReference("clap://thread/"
                + MY_BASEPATH + "/xslt/one/1st.xsl");
        final Representation xsltOne = comp.getContext().getClientDispatcher()
                .get(xsltOneRef).getEntity();
        final TransformRepresentation tr = new TransformRepresentation(comp
                .getContext(), xmlIn, xsltOne);

        // TODO transformer output should go to SAX! The sax-event-stream should
        // then be fed into a DOMBuilder
        // and then the assertions should be written as DOM tests...
        // (NOTE: current string-compare assertion might fail on lexical aspects
        // as
        // ignorable whitespace, encoding settings etc etc)
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        tr.write(out);
        final String xmlOut = out.toString();

        final String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><output><data1>1st</data1><data2>2nd</data2>"
                + thirdDocData + "</output>";
        assertEquals("xslt result doesn't match expectations", expectedResult,
                xmlOut);
    }
}
