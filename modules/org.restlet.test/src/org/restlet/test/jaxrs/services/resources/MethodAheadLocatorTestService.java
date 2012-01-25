/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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
