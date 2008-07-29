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
import java.util.ArrayList;
import java.util.List;

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
    protected Class<?> getRootResourceClass() {
        return ListParamService.class;
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
        } catch (final AssertionFailedError afe) {
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
        } catch (final AssertionFailedError afe) {
            assertEquals("m=m2", m);
        }
        try {
            assertEquals("mm=[mm1, mm2]", mm);
        } catch (final AssertionFailedError afe) {
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