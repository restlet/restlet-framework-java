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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.PathSegment;

import org.restlet.test.jaxrs.services.tests.ListParamTest;

/**
 * @author Stephan Koops
 * @see ListParamTest
 */
@Path("/listParams")
public class ListParamService {

    @GET
    @Path("cookie")
    @Produces("text/plain")
    public String getCookie(@CookieParam("c") String c,
            @CookieParam("cc") List<String> cc) {
        return "c=" + c + "\ncc=" + cc;
    }

    @GET
    @Path("header")
    @Produces("text/plain")
    public String getHeader(@HeaderParam("h") String h,
            @HeaderParam("hh") Set<String> hh) {
        return "h=" + h + "\nhh=" + hh;
    }

    @GET
    @Path("matrix")
    @Produces("text/plain")
    public String getMatrix(@MatrixParam("m") String m,
            @MatrixParam("mm") Collection<String> mm) {
        return "m=" + m + "\nmm=" + mm;
    }

    // @Path("{other}")
    public ListParamService getOther() {
        return new ListParamService();
    }

    @GET
    @Path("path/{p}/{p}/{pp}/{pp}")
    @Produces("text/plain")
    public String getPath(@PathParam("p") PathSegment p,
            @PathParam("pp") SortedSet<PathSegment> pp) {
        return "p=" + p + "\npp=" + pp;
    }

    @GET
    @Path("query")
    @Produces("text/plain")
    public String getQuery(@QueryParam("q") String q,
            @QueryParam("qq") List<String> qq) {
        return "q=" + q + "\nqq=" + qq;
    }
}