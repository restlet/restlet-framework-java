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
package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

import static org.restlet.data.MediaType.*;
import static org.restlet.data.Language.*;

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

    private void assertBaseUriAndMediaType(MediaType expectedMT,
            Response response, boolean checkEntityText) throws IOException {
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(expectedMT, entity.getMediaType());
        if (checkEntityText)
            assertEquals(createBaseRef().toString(), entity.getText());
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
        Representation entity = response.getEntity();
        assertEquals(expMediaType, entity.getMediaType());
        assertEquals(expLanguage, Util.getFirstElementOrNull(entity
                .getLanguages()));
        Reference r = createReference(getRootResourceClass(), expSubPath);
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
        Representation entity = response.getEntity();
        assertEquals(expectedMT, entity.getMediaType());
        if (checkEntityText) {
            Reference r = createReference(getRootResourceClass(),
                    expectedSubPath);
            assertEquals(r.toString(), entity.getText());
        }
    }

    /**
     * @return
     */
    @Override
    protected ApplicationConfig getAppConfig() {
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Map<String, String> getLanguageMappings() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("en", "en");
                map.put("de", "de");
                return map;
            }

            @Override
            public Map<String, javax.ws.rs.core.MediaType> getMediaTypeMappings() {
                Map<String, javax.ws.rs.core.MediaType> map = new HashMap<String, javax.ws.rs.core.MediaType>();
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

    public void testPlatonicAndExts() throws Exception {
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

    public void testPlatonicAndExtsAndExt() throws Exception {
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

    public void testPlatonicGet() throws Exception {
        Response response = get("platonic", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = get("platonic.txt", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_PLAIN, response, true);

        response = get("platonic.html", IMAGE_GIF);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = get("platonic.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testPlatonicHead() throws Exception {
        Response response = head("platonic", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_HTML, response, false);

        response = head("platonic.txt", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_PLAIN, response, false);

        response = head("platonic.html", IMAGE_GIF);
        assertUriAndMediaType("platonic", TEXT_HTML, response, false);

        response = head("platonic.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testPlatonicPost() throws Exception {
        Response response = post("platonic", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = post("platonic.txt", TEXT_HTML);
        assertUriAndMediaType("platonic", TEXT_PLAIN, response, true);

        response = post("platonic.html", IMAGE_GIF);
        assertUriAndMediaType("platonic", TEXT_HTML, response, true);

        response = post("platonic.xml", TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }
}