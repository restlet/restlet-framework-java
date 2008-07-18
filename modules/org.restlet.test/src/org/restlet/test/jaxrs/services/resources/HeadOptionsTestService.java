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