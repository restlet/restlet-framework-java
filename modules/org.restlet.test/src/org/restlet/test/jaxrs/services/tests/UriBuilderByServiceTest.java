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

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.resources.UriBuilderTestResource;

/**
 * @author Stephan Koops
 * @see UriBuilderTestResource
 */
public class UriBuilderByServiceTest extends JaxRsTestCase {

    private void assertUriAndMediaType(String expectedSubPath,
            MediaType expectedMT, Response response) throws IOException {
        super.sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(expectedMT, entity.getMediaType());
        Reference r = createReference(getRootResourceClass(), expectedSubPath);
        assertEquals(r.toString(), entity.getText());
    }

    private void assertBaseUriAndMediaType(MediaType expectedMT,
            Response response) throws IOException {
        super.sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEquals(expectedMT, entity.getMediaType());
        assertEquals(createBaseRef().toString(), entity.getText());
    }

    /**
     * @return
     */
    @Override
    protected ApplicationConfig getAppConfig() {
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Map<String, javax.ws.rs.core.MediaType> getMediaTypeMappings() {
                Map<String, javax.ws.rs.core.MediaType> map = new HashMap<String, javax.ws.rs.core.MediaType>();
                map.put("txt", javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE);
                map.put("html", javax.ws.rs.core.MediaType.TEXT_HTML_TYPE);
                map.put("htm", javax.ws.rs.core.MediaType.TEXT_HTML_TYPE);
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
        Response response = get("absolute", MediaType.TEXT_HTML);
        assertUriAndMediaType("absolute", MediaType.TEXT_HTML, response);

        response = get("absolute.txt", MediaType.TEXT_HTML);
        assertUriAndMediaType("absolute.txt", MediaType.TEXT_PLAIN, response);

        response = get("absolute.html", MediaType.IMAGE_GIF);
        assertUriAndMediaType("absolute.html", MediaType.TEXT_HTML, response);

        response = get("absolute.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testAbsoluteHead() throws Exception {
        Response response = head("absolute", MediaType.TEXT_HTML);
        assertUriAndMediaType("absolute", MediaType.TEXT_HTML, response);

        response = head("absolute.txt", MediaType.TEXT_HTML);
        assertUriAndMediaType("absolute.txt", MediaType.TEXT_PLAIN, response);

        response = head("absolute.html", MediaType.IMAGE_GIF);
        assertUriAndMediaType("absolute.html", MediaType.TEXT_HTML, response);

        response = head("absolute.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testAbsolutePost() throws Exception {
        Response response = post("absolute", MediaType.TEXT_HTML);
        assertUriAndMediaType("absolute", MediaType.TEXT_HTML, response);

        response = post("absolute.txt", MediaType.TEXT_HTML);
        assertUriAndMediaType("absolute.txt", MediaType.TEXT_PLAIN, response);

        response = post("absolute.html", MediaType.IMAGE_GIF);
        assertUriAndMediaType("absolute.html", MediaType.TEXT_HTML, response);

        response = post("absolute.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testBaseGet() throws Exception {
        Response response = get("base", MediaType.TEXT_HTML);
        assertBaseUriAndMediaType(MediaType.TEXT_HTML, response);

        response = get("base.txt", MediaType.TEXT_HTML);
        assertBaseUriAndMediaType(MediaType.TEXT_PLAIN, response);

        response = get("base.html", MediaType.IMAGE_GIF);
        assertBaseUriAndMediaType(MediaType.TEXT_HTML, response);

        response = get("base.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testBaseHead() throws Exception {
        Response response = head("base", MediaType.TEXT_HTML);
        assertBaseUriAndMediaType(MediaType.TEXT_HTML, response);

        response = head("base.txt", MediaType.TEXT_HTML);
        assertBaseUriAndMediaType(MediaType.TEXT_PLAIN, response);

        response = head("base.html", MediaType.IMAGE_GIF);
        assertBaseUriAndMediaType(MediaType.TEXT_HTML, response);

        response = head("base.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testBasePost() throws Exception {
        Response response = post("base", MediaType.TEXT_HTML);
        assertBaseUriAndMediaType(MediaType.TEXT_HTML, response);

        response = post("base.txt", MediaType.TEXT_HTML);
        assertBaseUriAndMediaType(MediaType.TEXT_PLAIN, response);

        response = post("base.html", MediaType.IMAGE_GIF);
        assertBaseUriAndMediaType(MediaType.TEXT_HTML, response);

        response = post("base.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testPlatonicGet() throws Exception {
        Response response = get("platonic", MediaType.TEXT_HTML);
        assertUriAndMediaType("platonic", MediaType.TEXT_HTML, response);

        response = get("platonic.txt", MediaType.TEXT_HTML);
        assertUriAndMediaType("platonic", MediaType.TEXT_PLAIN, response);

        response = get("platonic.html", MediaType.IMAGE_GIF);
        assertUriAndMediaType("platonic", MediaType.TEXT_HTML, response);

        response = get("platonic.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testPlatonicHead() throws Exception {
        Response response = head("platonic", MediaType.TEXT_HTML);
        assertUriAndMediaType("platonic", MediaType.TEXT_HTML, response);

        response = head("platonic.txt", MediaType.TEXT_HTML);
        assertUriAndMediaType("platonic", MediaType.TEXT_PLAIN, response);

        response = head("platonic.html", MediaType.IMAGE_GIF);
        assertUriAndMediaType("platonic", MediaType.TEXT_HTML, response);

        response = head("platonic.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    public void testPlatonicPost() throws Exception {
        Response response = post("platonic", MediaType.TEXT_HTML);
        assertUriAndMediaType("platonic", MediaType.TEXT_HTML, response);

        response = post("platonic.txt", MediaType.TEXT_HTML);
        assertUriAndMediaType("platonic", MediaType.TEXT_PLAIN, response);

        response = post("platonic.html", MediaType.IMAGE_GIF);
        assertUriAndMediaType("platonic", MediaType.TEXT_HTML, response);

        response = post("platonic.xml", MediaType.TEXT_HTML);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }
}