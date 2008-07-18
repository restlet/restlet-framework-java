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

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.RecursiveTestService;

/**
 * @author Stephan Koops
 * @see RecursiveTestService
 */
public class RecursiveTest extends JaxRsTestCase {
    @Override
    protected Class<?> getRootResourceClass() {
        return RecursiveTestService.class;
    }

    public void test0() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("0", response.getEntity().getText());
    }

    public void test1a() throws Exception {
        final Response response = get("a");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("1", response.getEntity().getText());
    }

    public void test1b() throws Exception {
        final Response response = get("a/");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("1", response.getEntity().getText());
    }

    public void test2() throws Exception {
        final Response response = get("a/a");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2", response.getEntity().getText());
    }

    public void test2b() throws Exception {
        final Response response = get("a/a/");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2", response.getEntity().getText());
    }

    public void test3() throws Exception {
        final Response response = get("a/a/a");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("3", response.getEntity().getText());
    }
}