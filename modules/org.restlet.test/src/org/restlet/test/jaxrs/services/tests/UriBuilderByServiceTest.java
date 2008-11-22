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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import static org.restlet.data.MediaType.TEXT_HTML;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.resources.UriBuilderTestResource;

/**
 * @author Stephan Koops
 * @see UriBuilderTestResource
 * @see ExtendedUriBuilderByServiceTest
 */
public class UriBuilderByServiceTest extends JaxRsTestCase {

    /**
     * @param baseReference
     *            {@link #createBaseRef()}
     */
    static void assertBaseUriAndMediaType(MediaType expectedMT,
            Response response, boolean checkEntityText, String baseRef)
            throws IOException {
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(expectedMT, entity);
        if (checkEntityText) {
            while (baseRef.endsWith("/"))
                baseRef = baseRef.substring(0, baseRef.length() - 1);
            String entityRef = entity.getText();
            while (entityRef.endsWith("/"))
                entityRef = baseRef.substring(0, entityRef.length() - 1);
            assertEquals(baseRef, entityRef);
        }
    }

    /**
     * @param reference
     *            {@link #createReference(String)}
     */
    static void assertUriAndMediaType(MediaType expectedMT, Response response,
            boolean checkEntityText, String reference) throws IOException {
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(expectedMT, entity.getMediaType());
        if (checkEntityText) {
            assertEquals(reference.toString(), entity.getText());
        }
    }

    private void assertBaseUriAndMediaType(MediaType expectedMT,
            Response response, boolean checkEntityText) throws IOException {
        assertBaseUriAndMediaType(expectedMT, response, checkEntityText,
                createBaseRef().toString());
    }

    private void assertUriAndMediaType(String expectedSubPath,
            MediaType expectedMT, Response response, boolean checkEntityText)
            throws IOException {
        assertUriAndMediaType(expectedMT, response, checkEntityText,
                createReference(expectedSubPath).toString());
    }

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(UriBuilderTestResource.class);
            }
        };
        return appConfig;
    }

    public void testAbsoluteGet() throws Exception {
        Response response = get("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, true);
    }

    public void testAbsoluteHead() throws Exception {
        Response response = head("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, false);
    }

    public void testAbsolutePost() throws Exception {
        Response response = post("absolute", TEXT_HTML);
        assertUriAndMediaType("absolute", TEXT_HTML, response, true);
    }

    public void testBaseGet() throws Exception {
        Response response = get("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, true);
    }

    public void testBaseHead() throws Exception {
        Response response = head("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, false);
    }

    public void testBasePost() throws Exception {
        Response response = post("base", TEXT_HTML);
        assertBaseUriAndMediaType(TEXT_HTML, response, true);
    }
}