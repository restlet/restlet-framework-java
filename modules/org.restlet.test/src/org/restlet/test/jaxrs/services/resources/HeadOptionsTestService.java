/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.test.jaxrs.services.others.OPTIONS;
import org.restlet.test.jaxrs.services.tests.HeadOptionsTest;

/**
 * This class contains only data for one media type
 * 
 * @author Stephan Koops
 * @see HeadOptionsTest
 * @see HEAD
 * @see OPTIONS
 */
@Path("/headOptionsTest")
public class HeadOptionsTestService {

    @GET
    public void get() {
        // do nothing
    }

    @GET
    @Path("headTest1")
    @Produces("text/html")
    public String getTest1() {
        return "4711";
    }

    @GET
    @Path("headTest1")
    @Produces("text/plain")
    public String getTest1a() {
        return "4711";
    }

    @GET
    @Path("headTest2")
    @Produces("text/html")
    public String getTest2() {
        return "4711";
    }

    @GET
    @Path("headTest2")
    @Produces("text/plain")
    public String getTest2plain() {
        return "4711";
    }

    @HEAD
    @Path("headTest1")
    @Produces("text/html")
    public String headTest1() {
        return null;
    }

    @HEAD
    @Path("headTest2")
    @Produces("text/html")
    public String headTest2() {
        return "4711";
    }

    @POST
    @Path("headTest1")
    public void post() {
        // do nothing yet
    }
}