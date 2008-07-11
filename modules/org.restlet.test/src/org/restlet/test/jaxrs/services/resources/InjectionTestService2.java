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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.InjectionTest;

/**
 * @author Stephan Koops
 * @see InjectionTest
 * @see Context
 */
@Path("/InjectionTestService/two/{idf}")
public class InjectionTestService2 {

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @Context
    private Request request;

    @Context
    private HttpHeaders httpHeaders;

    @PathParam("idf")
    private Integer idf;

    @HeaderParam("host")
    private String hostHost;

    @GET
    @Produces("text/plain")
    public Response get() {
        String msg = "";
        if (securityContext == null)
            msg += "\n* securityContext";
        if (uriInfo == null)
            msg += "\n* uriInfo";
        if (request == null)
            msg += "\n* request";
        if (httpHeaders == null)
            msg += "\n* httpHeaders";
        if (idf == null)
            msg += "\n* id";
        if (hostHost == null)
            msg += "\n* host";
        if (msg.length() > 0)
            return Response.serverError().entity("missing:" + msg).build();
        return Response.ok(String.valueOf(idf)).build();
    }
}