/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import static org.restlet.data.Language.ENGLISH;
import static org.restlet.data.MediaType.IMAGE_GIF;
import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.resources.UriBuilderTestResource;

/**
 * @author Stephan Koops
 * @see UriBuilderTestResource
 */
public class UriBuilderByServiceTest extends JaxRsTestCase {

    public void _testPlatonicAndExts() throws Exception {
        // LATER move this tests to non platonic methods
        Response response = get("platonicAndExts.txt", TEXT_HTML);
        assertPlatonic("platonicAndExts", "txt", TEXT_PLAIN, null, response);

        response = get("platonicAndExts.abc.html.txt", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        response = get("platonicAndExts.abc.html.en.txt", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        response = get("platonicAndExts.en.txt", TEXT_HTML);
        assertPlatonic("platonicAndExts", "en.txt", TEXT_PLAIN, ENGLISH,
                response);
    }

    public void _testPlatonicAndExtsAndExt() throws Exception {
        // LATER move this tests to non platonic methods
        Response response = get("platonicAndExts.abc.txt", TEXT_HTML);
        assertPlatonic("platonicAndExts.abc", "abc.txt", TEXT_PLAIN, null,
                response);

        response = get("platonicAndExts.txt.abc", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        response = get("platonicAndExts.abc.html.txt", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());

        response = get("platonicAndExts.abc.en.txt", TEXT_HTML);
        assertPlatonic("platonicAndExts.abc", "abc.en.txt", TEXT_PLAIN,
                ENGLISH, response);

        response = get("platonicAndExts.def.en.txt", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void _testPlatonicGet() throws Exception {
        // LATER move this tests to non platonic methods
        Response response = get("platonic", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = get("platonic.txt", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_PLAIN, response, true);

        response = get("platonic.html", IMAGE_GIF);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = get("platonic.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void _testPlatonicHead() throws Exception {
        // LATER move this tests to non platonic methods
        Response response = head("platonic", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_HTML, response, false);

        response = head("platonic.txt", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_PLAIN, response, false);

        response = head("platonic.html", IMAGE_GIF);
        assertUriAndMediaType("platonic", TEXT_HTML, response, false);

        response = head("platonic.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void _testPlatonicPost() throws Exception {
        // LATER move this tests to non platonic methods
        Response response = post("platonic", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = post("platonic.txt", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_PLAIN, response, true);

        response = post("platonic.html", IMAGE_GIF);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = post("platonic.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    private void assertBaseUriAndMediaType(MediaType expectedMT,
            Response response, boolean checkEntityText) throws IOException {
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(expectedMT, entity);
        if (checkEntityText) {
            assertEquals(createBaseRef().toString(), entity.getText());
        }
    }

    /**
     * @param expSubPath
     * @param allExpExts
     * @param expMediaType
     * @param expLanguage
     * @param response
     * @throws IOException
     */
    private void assertPlatonic(String expSubPath, String allExpExts,
            MediaType expMediaType, Language expLanguage, Response response)
            throws IOException {
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(expMediaType, entity);
        assertEquals(expLanguage, Util.getFirstElementOrNull(entity
                .getLanguages()));
        final Reference r = createReference(getRootResourceClass(), expSubPath);
        assertEquals(r.toString() + "\n" + allExpExts, entity.getText());
    }

    /**
     * 
     * @param expectedSubPath
     * @param expectedMT
     * @param response
     * @throws IOException
     */
    private void assertUriAndMediaType(String expectedSubPath,
            MediaType expectedMT, Response response, boolean checkEntityText)
            throws IOException {
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(expectedMT, entity.getMediaType());
        if (checkEntityText) {
            final Reference r = createReference(getRootResourceClass(),
                    expectedSubPath);
            assertEquals(r.toString(), entity.getText());
        }
    }

    /**
     * @return
     */
    @Override
    protected ApplicationConfig getAppConfig() {
        final ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Map<String, String> getLanguageMappings() {
                final Map<String, String> map = new HashMap<String, String>();
                map.put("en", "en");
                map.put("de", "de");
                return map;
            }

            @Override
            public Map<String, javax.ws.rs.core.MediaType> getMediaTypeMappings() {
                final Map<String, javax.ws.rs.core.MediaType> map = new HashMap<String, javax.ws.rs.core.MediaType>();
                map.put("txt", javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE);
                map.put("html", javax.ws.rs.core.MediaType.TEXT_HTML_TYPE);
                map.put("htm", javax.ws.rs.core.MediaType.TEXT_HTML_TYPE);
                map.put("jpx", new javax.ws.rs.core.MediaType("image", "jpx"));
                return map;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getResourceClasses() {
                return (Set) Collections.singleton(getRootResourceClass());
            }
        };
        return appConfig;
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return UriBuilderTestResource.class;
    }

    public void testAbsoluteGet() throws Exception {
        Response response = get("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, true);

        response = get("absolute.txt", TEXT_HTML);
        assertUriAndMediaType("absolute.txt", TEXT_PLAIN, response, true);

        response = get("absolute.html", IMAGE_GIF);
        assertUriAndMediaType("absolute.html", TEXT_HTML, response, true);

        response = get("absolute.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testAbsoluteHead() throws Exception {
        Response response = head("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, false);

        response = head("absolute.txt", TEXT_HTML);
        assertUriAndMediaType("absolute.txt", TEXT_PLAIN, response, false);

        response = head("absolute.html", IMAGE_GIF);
        assertUriAndMediaType("absolute.html", TEXT_HTML, response, false);

        response = head("absolute.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testAbsolutePost() throws Exception {
        Response response = post("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, true);

        response = post("absolute.txt", TEXT_HTML);
        assertUriAndMediaType("absolute.txt", TEXT_PLAIN, response, true);

        response = post("absolute.html", IMAGE_GIF);
        assertUriAndMediaType("absolute.html", TEXT_HTML, response, true);

        response = post("absolute.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testBaseGet() throws Exception {
        Response response = get("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, true);

        response = get("base.txt", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_PLAIN, response, true);

        response = get("base.html", IMAGE_GIF);
        assertBaseUriAndMediaType(TEXT_HTML, response, true);

        response = get("base.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testBaseHead() throws Exception {
        Response response = head("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, false);

        response = head("base.txt", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_PLAIN, response, false);

        response = head("base.html", IMAGE_GIF);
        assertBaseUriAndMediaType(TEXT_HTML, response, false);

        response = head("base.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testBasePost() throws Exception {
        Response response = post("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, true);

        response = post("base.txt", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_PLAIN, response, true);

        response = post("base.html", IMAGE_GIF);
        assertBaseUriAndMediaType(TEXT_HTML, response, true);

        response = post("base.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }
}