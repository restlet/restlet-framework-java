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

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.PathParamTestService;

/**
 * @author Stephan Koops
 */
public class PathParamTest extends JaxRsTestCase {

    private static final Class<?> SERVICE_CLASS = PathParamTestService.class;

    @Override
    protected Class<?> getRootResourceClass() {
        return SERVICE_CLASS;
    }

    /**
     * 
     * @param subPath
     *                without beginning '/'
     * @return
     */
    private Reference createReference(String subPath) {
        return new Reference("http://localhost:" + super.getPort()
                + "/pathParamTest/" + subPath);
    }

    public void testGet1() throws IOException {
        Response response = accessServer(Method.GET, createReference("4711"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4711", response.getEntity().getText());
    }

    public void testGet2() throws IOException {
        Response response = accessServer(Method.GET, createReference("4711/abc/677/def"));
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("4711\n677", response.getEntity().getText());
    }
}