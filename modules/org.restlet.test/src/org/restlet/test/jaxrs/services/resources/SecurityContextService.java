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

import java.net.URI;
import java.security.Principal;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.restlet.test.jaxrs.services.tests.SecurityContextTest;

/**
 * @author Stephan Koops
 * @see SecurityContextTest
 * @see SecurityContext
 */
@Path("/SecurityContextTestService")
public class SecurityContextService {

    @Context
    private SecurityContext securityContext;

    @GET
    @Produces("text/plain")
    public String get() {
        if (!this.securityContext.isUserInRole("bad")) {
            throw new WebApplicationException(403);
        }
        return "das darfst Du";
    }

    @GET
    @Path("authenticationScheme")
    @Produces("text/plain")
    public String getAuthenticationScheme() {
        return this.securityContext.getAuthenticationScheme();
    }

    @GET
    @Path("userPrincipal")
    @Produces("text/plain")
    public String getUserPrincipal() {
        final Principal principal = this.securityContext.getUserPrincipal();
        if (principal == null) {
            return "no principal found";
        }
        return principal.getName();
    }

    @GET
    @Path("secure")
    @Produces("text/plain")
    public String isSecure(@Context UriInfo uriInfo) {
        if (!this.securityContext.isSecure()) {
            final ResponseBuilder rb = Response
                    .status(Status.MOVED_PERMANENTLY);
            rb.entity("You must use a secure connection");
            rb.location(uriInfo.getRequestUriBuilder().scheme("https").build());
            throw new WebApplicationException(rb.build());
        }
        return "wonderful! It's a secure request.";
    }

    @POST
    public Response post(MultivaluedMap<String, String> entity,
            @Context UriInfo uriInfo) {
        if (!this.securityContext.isUserInRole("bat")) {
            throw new WebApplicationException(403);
        }
        entity.toString(); // typically the entity will be stored in the DB.
        final String id = "4711";
        final URI collectionUri = uriInfo.getRequestUri();
        final URI location = UriBuilder.fromUri(collectionUri).path("{id}")
                .build(id);
        return Response.created(location).build();
    }
}