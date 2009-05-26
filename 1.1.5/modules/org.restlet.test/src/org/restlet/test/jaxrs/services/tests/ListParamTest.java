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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import junit.framework.AssertionFailedError;

import org.restlet.data.Cookie;
import org.restlet.data.Parameter;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.ListParamService;

/**
 * @author Stephan Koops
 * @see ListParamService
 */
public class ListParamTest extends JaxRsTestCase {

    /**
     * @param response
     * @throws IOException
     */
    private void checkPathParam(Response response) throws IOException {
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("p=p1\npp={pp1, pp2}", response.getEntity().getText());
    }

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(ListParamService.class);
            }
        };
        return appConfig;
    }
    public void testCookieParams() throws IOException {
        final List<Cookie> cookies = new ArrayList<Cookie>();
        cookies.add(new Cookie("c", "c1"));
        cookies.add(new Cookie("c", "c2"));
        cookies.add(new Cookie("c", "c3"));
        cookies.add(new Cookie("cc", "cc1"));
        cookies.add(new Cookie("cc", "cc2"));
        cookies.add(new Cookie("cc", "cc3"));
        final Response response = getWithCookies("cookie", cookies);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("c=c1\ncc=[cc1, cc2, cc3]", response.getEntity().getText());
    }

    public void testHeaderParams() throws IOException {
        final List<Parameter> addHeaders = new ArrayList<Parameter>();
        addHeaders.add(new Parameter("h", "h1"));
        addHeaders.add(new Parameter("h", "h2"));
        addHeaders.add(new Parameter("hh", "hh1"));
        addHeaders.add(new Parameter("hh", "hh2"));
        final Response response = getWithHeaders("header", addHeaders);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String[] entity = response.getEntity().getText().split("\\n");
        final String header = entity[0];
        final String headers = entity[1];
        assertEquals("h=h1", header);
        try {
            assertEquals("hh=[hh1, hh2]", headers);
        } catch (AssertionFailedError afe) {
            assertEquals("hh=[hh2, hh1]", headers);
        }
    }

    /** @see ListParamService#getMatrix(String, java.util.Collection) 
     * @throws IOException */
    public void testMatrixParams() throws IOException {
        final Response response = get("matrix;m=m1;m=m2;mm=mm1;mm=mm2");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String[] entity = response.getEntity().getText().split("\n");
        final String m = entity[0];
        final String mm = entity[1];
        try {
            // LATER test: get in given direction -> Resources.Parameters
            assertEquals("m=m1", m);
        } catch (AssertionFailedError afe) {
            assertEquals("m=m2", m);
        }
        try {
            assertEquals("mm=[mm1, mm2]", mm);
        } catch (AssertionFailedError afe) {
            assertEquals("mm=[mm2, mm1]", mm);
        }
    }

    /**
     * @see ListParamService#getPath(String, java.util.SortedSet)
     */
    public void testPathParams() throws IOException {
        if (true) {
            return;
        }
        Response response = get("path/p1/p2/pp1/pp2");
        checkPathParam(response);

        response = get("path/p1/p2/pp2/pp1");
        checkPathParam(response);
    }

    public void testQueryParams() throws IOException {
        Response response = get("query?q=q1&q=q2&qq=qq1&qq=qq2");
        assertEquals("q=q1\nqq=[qq1, qq2]", response.getEntity().getText());

        response = get("query?q=q2&q=q1&qq=qq2&qq=qq1");
        assertEquals("q=q2\nqq=[qq2, qq1]", response.getEntity().getText());
    }
}