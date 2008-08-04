/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;

import javax.ws.rs.CookieParam;

import junit.framework.AssertionFailedError;

import org.restlet.data.Cookie;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.resources.CookieParamTestService;

/**
 * @author Stephan Koops
 * @see CookieParamTestService
 * @see CookieParam
 */
public class CookieParamTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return CookieParamTestService.class;
    }

    public void test1() throws IOException {
        Response response = get(new Cookie("c", "value"));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("value", response.getEntity().getText());

        response = get(new Cookie("c", "sdfgdfg"));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("sdfgdfg", response.getEntity().getText());
    }

    public void test2() throws IOException {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        final Representation entity = response.getEntity();
        String text;
        if (entity != null) {
            text = entity.getText();
        } else {
            text = null;
        }
        assertEquals(null, text);
    }

    public void testCookieArray() throws Exception {
        final Request request = createGetRequest("array");
        request.getCookies().add(new Cookie("c", "c1"));
        request.getCookies().add(new Cookie("c", "c2"));
        request.getCookies().add(new Cookie("d", "c3"));
        final Response response = accessServer(request);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        final String entityWithoutBrackets = entity.substring(1, entity
                .length() - 1);
        assertEquals("c1, c2", entityWithoutBrackets);
    }

    public void testCookieSet() throws Exception {
        final Request request = createGetRequest("Set");
        request.getCookies().add(new Cookie("c", "c1"));
        request.getCookies().add(new Cookie("c", "c2"));
        request.getCookies().add(new Cookie("d", "c3"));
        final Response response = accessServer(request);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        final String entityWithoutBrackets = entity.substring(1, entity
                .length() - 1);
        try {
            assertEquals("c1, c2", entityWithoutBrackets);
        } catch (final AssertionFailedError afe) {
            assertEquals("c2, c1", entityWithoutBrackets);
        }
    }

    public void testCookieSortedSet() throws Exception {
        final Request request = createGetRequest("SortedSet");
        request.getCookies().add(new Cookie("c", "c1"));
        request.getCookies().add(new Cookie("c", "c2"));
        request.getCookies().add(new Cookie("d", "c3"));
        final Response response = accessServer(request);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        assertEquals("c1, c2", entity.substring(1, entity.length() - 1));
    }

    public void testWithDefault() throws IOException {
        Response response = get("withDefault", new Cookie("c", "value"));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("value", response.getEntity().getText());

        response = get("withDefault", new Cookie("c", "sdfgdfg"));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("sdfgdfg", response.getEntity().getText());

        response = get("withDefault");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("default", response.getEntity().getText());
    }
}