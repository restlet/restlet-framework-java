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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.restlet.test.jaxrs.services.tests.PathParamTest;

/**
 * @author Stephan Koops
 * @see PathParamTest
 * @see PathParam
 */
@Path("pathParamTest/{var1}")
public class PathParamTestService {

    @Path("checkUnmodifiable/{var1}")
    @GET
    @Produces("text/plain")
    public Object checkUnmodifiable(@PathParam("var1") List<PathSegment> var1s) {
        try {
            var1s.clear();
            throw new WebApplicationException(Response.serverError().entity(
                    "the List must be unmodifiable").build());
        } catch (UnsupportedOperationException uoe) {
            return null;
        }
    }

    @GET
    @Produces("text/plain")
    public String get(@PathParam("var1") String var1) {
        return var1;
    }

    @Path("abc/{var2}/def")
    @GET
    @Produces("text/plain")
    public String get(@PathParam("var1") String var1,
            @PathParam("var2") String var2) {
        return var1 + "\n" + var2;
    }

    @Path("st/{var1}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getVar1(@PathParam("var1") String var1) {
        return var1;
    }
}