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
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.InjectionTestService;
import org.restlet.test.jaxrs.services.resources.InjectionTestService2;

/**
 * @author Stephan Koops
 * @see InjectionTestService
 * @see InjectionTestService2
 */
public class InjectionTest extends JaxRsTestCase {

    /**
     * @return
     */
    @Override
    protected ApplicationConfig getAppConfig() {
        final ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                final Set<Class<?>> rrcs = new HashSet<Class<?>>();
                rrcs.add(getRootResourceClass());
                rrcs.add(InjectionTestService2.class);
                return rrcs;
            }
        };
        return appConfig;
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return InjectionTestService.class;
    }

    public void testGet() {
        final Response response = get("?qp1=56");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /** @see InjectionTestService2#get() */
    public void testGetWithIndex() throws IOException {
        Response response = get("two/56");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("56", response.getEntity().getText());

        response = get("two/97");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("97", response.getEntity().getText());
    }
}