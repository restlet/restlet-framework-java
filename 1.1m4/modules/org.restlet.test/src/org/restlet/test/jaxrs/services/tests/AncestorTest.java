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
        Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("0\n0", response.getEntity().getText());
    }

    public void testUri() throws Exception {
        Response response = get("uris");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("1\n/ancestorTest", response.getEntity().getText());
    }

    public void testResourceClassNames() throws Exception {
        Response response = get("resourceClassNames");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(
                "1\norg.restlet.test.jaxrs.services.resources.AncestorTestService",
                response.getEntity().getText());
    }

    public void testGetSub() throws Exception {
        Response response = get("sub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("1\n1", response.getEntity().getText());
    }

    public void testGetSubSub() throws Exception {
        Response response = get("sub/sub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2\n2", response.getEntity().getText());
    }

    public void testGetSubSameSub() throws Exception {
        Response response = get("sub/sameSub");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("2\n2", response.getEntity().getText());
    }

    public void testSameSubSubUri() throws Exception {
        Response response = get("sameSub/sub/uris");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(
                "3\n/ancestorTest/sameSub/sub\n/ancestorTest/sameSub\n/ancestorTest",
                response.getEntity().getText());
    }
}