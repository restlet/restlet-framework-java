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

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.AncestorTestService;

/**
 * @author Stephan Koops
 * @see AncestorTestService
 */
public class AncestorTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return AncestorTestService.class;
    }

    public void testGet() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("0\n0", response.getEntity().getText());
    }

    public void testGetSub() throws Exception {
        final Response response = get("sub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("1\n1", response.getEntity().getText());
    }

    public void testGetSubSameSub() throws Exception {
        final Response response = get("sub/sameSub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2\n2", response.getEntity().getText());
    }

    public void testGetSubSub() throws Exception {
        final Response response = get("sub/sub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2\n2", response.getEntity().getText());
    }

    public void testResourceClassNames() throws Exception {
        final Response response = get("resourceClassNames");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(
                "1\norg.restlet.test.jaxrs.services.resources.AncestorTestService",
                response.getEntity().getText());
    }

    public void testSameSubSubUri() throws Exception {
        final Response response = get("sameSub/sub/uris");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(
                "3\n/ancestorTest/sameSub/sub\n/ancestorTest/sameSub\n/ancestorTest",
                response.getEntity().getText());
    }

    public void testUri() throws Exception {
        final Response response = get("uris");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("1\n/ancestorTest", response.getEntity().getText());
    }

    /**
     * @see AncestorTestService#getUriInfoAttribute(javax.ws.rs.core.UriInfo,
     *      String)
     */
    public void testUriInfos() throws Exception {
        final Response response404 = get("uriInfo/abc");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response404.getStatus());

        final Response response = get("uriInfo/ancestorResourceURIs");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        final String expected = "[]\n[/ancestorTest]";
        System.out.println("expected:\n" + expected + "\ngot:\n" + entity);
        assertEquals(expected, entity);
    }

    /**
     * @see AncestorTestService#getUriInfoAttribute(javax.ws.rs.core.UriInfo,
     *      String)
     */
    public void testUriInfosSub() throws Exception {
        final Response response404 = get("sub/uriInfo/abc");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response404.getStatus());

        final Response response = get("sub/uriInfo/ancestorResourceURIs");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        final String expected = "[]\n[/ancestorTest/sub, /ancestorTest]";
        System.out.println("expected:\n" + expected + "\ngot:\n" + entity);
        assertEquals(expected, entity);
    }
}