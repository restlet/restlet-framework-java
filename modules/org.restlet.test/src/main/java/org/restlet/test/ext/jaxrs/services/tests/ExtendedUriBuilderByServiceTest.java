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

package org.restlet.test.ext.jaxrs.services.tests;

import static org.restlet.data.MediaType.IMAGE_GIF;
import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.test.ext.jaxrs.services.resources.ExtendedUriBuilderTestResource;

/**
 * @author Stephan Koops
 * @see ExtendedUriBuilderTestResource
 * @see UriBuilderByServiceTest
 */
public class ExtendedUriBuilderByServiceTest extends JaxRsTestCase {

    private static final String SSP_START = "://";

    /**
     * @param expectedExtension
     *            may be null
     */
    private void assertBaseUriAndMediaType(MediaType expectedMT,
            Response response, boolean checkEntityText, String expectedExtension)
            throws IOException {
        String baseRef = createBaseRef().toString();
        if (expectedExtension != null) {
            int behindSlashes = baseRef.indexOf(SSP_START) + SSP_START.length();
            if (!baseRef.substring(behindSlashes).contains("/")) {
                // only host given
                baseRef += "/";
            }
            baseRef += "." + expectedExtension;
        }
        UriBuilderByServiceTest.assertBaseUriAndMediaType(expectedMT, response,
                checkEntityText, baseRef);
    }

    /**
     * @param expectedExtension
     *            may be null
     */
    private void assertUriAndMediaType(String expectedSubPath,
            MediaType expectedMT, Response response, boolean checkEntityText,
            String expectedExtension) throws IOException {
        String ref = createReference(expectedSubPath).toString();
        if (expectedExtension != null) {
            ref += "." + expectedExtension;
        }
        UriBuilderByServiceTest.assertUriAndMediaType(expectedMT, response,
                checkEntityText, ref);
    }

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(ExtendedUriBuilderTestResource.class);
            }
        };
        return appConfig;
    }

    @Override
    protected void modifyApplication(JaxRsApplication application) {
        application.getTunnelService().setExtensionsTunnel(true);
    }

    public void testAbsoluteGet() throws Exception {
        Response response = get("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, true, null);

        response = get("absolute.txt", TEXT_HTML);
        assertUriAndMediaType("absolute.txt", TEXT_PLAIN, response, true, "txt");

        response = get("absolute.html", IMAGE_GIF);
        assertUriAndMediaType("absolute.html", TEXT_HTML, response, true,
                "html");

        response = get("absolute.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    /**
     * @see ExtendedUriBuilderTestResource#getAbsoluteUriBuilder()
     * @throws Exception
     */
    public void testAbsoluteHead() throws Exception {
        Response response = head("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, false, null);

        response = head("absolute.txt", TEXT_HTML);
        assertUriAndMediaType("absolute.txt", TEXT_PLAIN, response, false,
                "txt");

        response = head("absolute.html", IMAGE_GIF);
        assertUriAndMediaType("absolute.html", TEXT_HTML, response, false,
                "html");

        response = head("absolute.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testBaseGet() throws Exception {
        Response response = get("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, true, null);

        response = get("base.txt", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_PLAIN, response, true, "txt");

        response = get("base.html", IMAGE_GIF);
        assertBaseUriAndMediaType(TEXT_HTML, response, true, "html");

        response = get("base.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testBaseHead() throws Exception {
        Response response = head("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, false, null);

        response = head("base.txt", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_PLAIN, response, false, "txt");

        response = head("base.html", IMAGE_GIF);
        assertBaseUriAndMediaType(TEXT_HTML, response, false, "html");

        response = head("base.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }
}
