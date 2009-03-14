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

package org.restlet.test.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.MatchedTestService;

/**
 * @author Stephan Koops
 * @see MatchedTestService
 */
public class MatchedTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(MatchedTestService.class);
            }
        };
    }
    public void testGet() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("1\n1", response.getEntity().getText());
    }

    public void testGetSub() throws Exception {
        final Response response = get("sub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2\n2", response.getEntity().getText());
    }

    public void testGetSubSameSub() throws Exception {
        final Response response = get("sub/sameSub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("3\n3", response.getEntity().getText());
    }

    public void testGetSubSub() throws Exception {
        final Response response = get("sub/sub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("3\n3", response.getEntity().getText());
    }

    public void testResourceClassNames() throws Exception {
        final Response response = get("resourceClassNames");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(
                "2\norg.restlet.test.jaxrs.services.resources.MatchedTestService\norg.restlet.test.jaxrs.services.resources.MatchedTestService",
                response.getEntity().getText());
    }

    public void testSameSubSubUri() throws Exception {
        final Response response = get("sameSub/sub/uris");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4" + "\n/matchedTest/sameSub/sub/uris"
                + "\n/matchedTest/sameSub/sub" + "\n/matchedTest/sameSub"
                + "\n/matchedTest", response.getEntity().getText());
    }

    public void testUri() throws Exception {
        final Response response = get("uris");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2\n/matchedTest/uris\n/matchedTest", response.getEntity()
                .getText());
    }

    /**
     * @see MatchedTestService#getUriInfoAttribute(javax.ws.rs.core.UriInfo,
     *      String)
     */
    public void testUriInfos() throws Exception {
        final Response response404 = get("uriInfo/abc");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response404.getStatus());

        final Response response = get("uriInfo/matchedURIs");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        final String expected = "[]\n[/matchedTest/uriInfo/matchedURIs, /matchedTest]";
        System.out.println("expected:\n" + expected + "\ngot:\n" + entity);
        assertEquals(expected, entity);
    }

    /**
     * @see MatchedTestService#getUriInfoAttribute(javax.ws.rs.core.UriInfo,
     *      String)
     */
    public void testUriInfosSub() throws Exception {
        final Response response404 = get("sub/uriInfo/abc");
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response404.getStatus());

        final Response response = get("sub/uriInfo/matchedURIs");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        final String expected = "[]\n[/matchedTest/sub/uriInfo/matchedURIs, /matchedTest/sub, /matchedTest]";
        System.out.println("expected:\n" + expected + "\ngot:\n" + entity);
        assertEquals(expected, entity);
    }
}