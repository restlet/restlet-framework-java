/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.services.resources;

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

import org.restlet.test.ext.jaxrs.services.tests.ListParamTest;

/**
 * @author Stephan Koops
 * @see ListParamTest
 */
@Path("/listParams")
public class ListParamService {

    @GET
    @Path("cookie")
    @Produces("text/plain")
    public String getCookie(@CookieParam("c")
    String c, @CookieParam("cc")
    List<String> cc) {
        return "c=" + c + "\ncc=" + cc;
    }

    @GET
    @Path("header")
    @Produces("text/plain")
    public String getHeader(@HeaderParam("h")
    String h, @HeaderParam("hh")
    Set<String> hh) {
        return "h=" + h + "\nhh=" + hh;
    }

    @GET
    @Path("matrix")
    @Produces("text/plain")
    public String getMatrix(@MatrixParam("m")
    String m, @MatrixParam("mm")
    Collection<String> mm) {
        return "m=" + m + "\nmm=" + mm;
    }

    // @Path("{other}")
    public ListParamService getOther() {
        return new ListParamService();
    }

    @GET
    @Path("path/{p}/{p}/{pp}/{pp}")
    @Produces("text/plain")
    public String getPath(@PathParam("p")
    PathSegment p, @PathParam("pp")
    SortedSet<PathSegment> pp) {
        return "p=" + p + "\npp=" + pp;
    }

    @GET
    @Path("query")
    @Produces("text/plain")
    public String getQuery(@QueryParam("q")
    String q, @QueryParam("qq")
    List<String> qq) {
        return "q=" + q + "\nqq=" + qq;
    }
}
