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

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.DeterminingMediaTypeTestService;

/**
 * This test class checks if the
 * {@link javax.ws.rs.core.Request#evaluatePreconditions(java.util.Date, javax.ws.rs.core.EntityTag)}
 * methods works fine.
 * 
 * @author Stephan Koops
 */
public class DeterminingMediaTypeTest extends JaxRsTestCase {
    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(DeterminingMediaTypeTestService.class);
            }
        };
    }

    public void testHtmlPlainGif1() {
        final Response response = get("htmlPlainGif", MediaType.TEXT_ALL);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response);
    }

    public void testHtmlPlainGif2() {
        final Response response = get("htmlPlainGif", MediaType.ALL);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response);
    }

    public void testHtmlPlainGif3() {
        final Response response = get("htmlPlainGif", MediaType.IMAGE_GIF);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.IMAGE_GIF, response);
    }

    public void testHtmlPlainGif4() {
        final Response response = get("htmlPlainGif", MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
    }

    public void testTextStar1() {
        Response response = get("textStar", MediaType.TEXT_ALL);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());

        response = get("textStar", MediaType.ALL);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());

        response = get("textStar", MediaType.IMAGE_GIF);
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testTextStar2() {
        final Response response = get("textStar", MediaType.TEXT_HTML);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response);
    }

    public void testTextStar3() {
        final Response response = get("textStar", MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
    }
}