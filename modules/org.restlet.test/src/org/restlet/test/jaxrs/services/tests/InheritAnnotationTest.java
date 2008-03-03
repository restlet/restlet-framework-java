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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.InheritAnnotationTestService1;
import org.restlet.test.jaxrs.services.InheritAnnotationTestService2;

public class InheritAnnotationTest extends JaxRsTestCase {

    private static final Class<InheritAnnotationTestService1> SERVICE_1 = InheritAnnotationTestService1.class;

    private static final Class<InheritAnnotationTestService2> SERVICE_2 = InheritAnnotationTestService2.class;

    @Override
    protected ApplicationConfig getAppConfig() {
        return new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                Set<Class<?>> rrcs = new HashSet<Class<?>>(2);
                rrcs.add(SERVICE_1);
                rrcs.add(SERVICE_2);
                return rrcs;
            }
        };
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void test1() throws Exception {
        Reference reference = createReference(SERVICE_1, "getText");
        Response response = accessServer(Method.GET, reference);
        super.sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        String entityText = response.getEntity().getText();
        assertEquals(InheritAnnotationTestService1.RETURN_STRING, entityText);
    }

    public void test2a() throws Exception {
        Reference reference = createReference(SERVICE_2, "getText");
        Response response = accessServer(Method.GET, reference);
        super.sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        String entityText = response.getEntity().getText();
        assertEquals(InheritAnnotationTestService2.RETURN_STRING, entityText);
    }

    public void test2b() throws Exception {
        Reference reference = createReference(SERVICE_2, "getSubClassText");
        Response response = accessServer(Method.GET, reference);
        super.sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(MediaType.TEXT_PLAIN, response.getEntity().getMediaType());
        String entityText = response.getEntity().getText();
        assertEquals(InheritAnnotationTestService2.RETURN_STRING_SUB, entityText);
    }
}
