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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

import junit.framework.AssertionFailedError;

import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.Header;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.ListParamService;
import org.restlet.util.Series;

/**
 * @author Stephan Koops
 * @see ListParamService
 */
public class ListParamTest extends JaxRsTestCase {

    public static final boolean LATER = true;

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
            @SuppressWarnings({ "unchecked", "rawtypes" })
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
        Series<Header> addHeaders = new Series<Header>(Header.class);
        addHeaders.add("h", "h1");
        addHeaders.add("h", "h2");
        addHeaders.add("hh", "hh1");
        addHeaders.add("hh", "hh2");

        Response response = getWithHeaders("header", addHeaders);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String[] entity = response.getEntity().getText().split("\\n");
        String header = entity[0];
        String headers = entity[1];
        assertEquals("h=h1", header);

        try {
            assertEquals("hh=[hh1, hh2]", headers);
        } catch (AssertionFailedError afe) {
            assertEquals("hh=[hh2, hh1]", headers);
        }
    }

    /**
     * @see ListParamService#getMatrix(String, java.util.Collection)
     * @throws IOException
     */
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
        if (!LATER) {
            Response response = get("path/p1/p2/pp1/pp2");
            checkPathParam(response);

            response = get("path/p1/p2/pp2/pp1");
            checkPathParam(response);
        }
    }

    public void testQueryParams() throws IOException {
        Response response = get("query?q=q1&q=q2&qq=qq1&qq=qq2");
        assertEquals("q=q1\nqq=[qq1, qq2]", response.getEntity().getText());

        response = get("query?q=q2&q=q1&qq=qq2&qq=qq1");
        assertEquals("q=q2\nqq=[qq2, qq1]", response.getEntity().getText());
    }
}
