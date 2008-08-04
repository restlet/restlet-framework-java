/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.test.jaxrs.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.test.jaxrs.services.tests.MethodAheadLocatorTest;

/**
 * This service is used to test, if sub resourcemethods are preferd to sub
 * resource locators. It test also with methods ordered both directions (first
 * method, than locator; also vice versa).
 * 
 * @author Stephan Koops
 * @see MethodAheadLocatorTest
 */
@Path("/methodsAheadLocatorsTest")
public class MethodAheadLocatorTestService {

    class SubResource {
        @GET
        @Produces("text/plain")
        public String get() {
            return "locator";
        }
    }

    @GET
    @Path("p1")
    @Produces("text/plain")
    public String get1() {
        return "method";
        // do nothing
    }

    @GET
    @Path("p2")
    @Produces("text/plain")
    public String get2() {
        return "method";
        // do nothing
    }

    @Path("p1")
    public String locator1() {
        return null;
    }

    @Path("p2")
    public String locator2() {
        return null;
    }
}