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

import org.restlet.data.Response;
import org.restlet.test.jaxrs.services.resources.PersonResource;
import org.restlet.test.jaxrs.services.resources.PersonsResource;

/**
 * @author Stephan Koops
 * @see PersonsResource
 * @see PersonResource
 */
public class PersonsTest extends JaxRsTestCase {
    @Override
    protected ApplicationConfig getAppConfig() {
        return new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                Set<Class<?>> rrcs = new HashSet<Class<?>>(2);
                rrcs.add(PersonResource.class);
                rrcs.add(PersonsResource.class);
                return rrcs;
            }
        };
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return PersonResource.class;
    }

    public void test1() {
        Response response = get();
        sysOutEntityIfError(response);
        "".toString(); // FIXME not ready
    }
}
