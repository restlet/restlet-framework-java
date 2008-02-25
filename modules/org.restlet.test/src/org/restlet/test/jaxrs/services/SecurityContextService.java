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
package org.restlet.test.jaxrs.services;

import java.net.URI;
import java.security.Principal;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.restlet.data.Status;

@Path("/SecurityContextTestService")
public class SecurityContextService {

    @GET
    @ProduceMime("text/plain")
    public String get(@Context
    SecurityContext securityContext) {
        if (!securityContext.isUserInRole("bad"))
            throw new WebApplicationException(403);
        return "das darfst Du";
    }

    @POST
    public Response post(@Context
    SecurityContext securityContext, MultivaluedMap<String, String> entity, @Context
    UriInfo uriInfo) {
        if (!securityContext.isUserInRole("bat"))
            throw new WebApplicationException(403);
        entity.toString(); // typically the entity will be stored in the DB.
        String id = "4711";
        URI collectionUri = uriInfo.getRequestUri();
        URI location = UriBuilder.fromUri(collectionUri).path("{id}").build(id);
        return Response.created(location).build();
    }

    @GET
    @Path("authenticationScheme")
    @ProduceMime("text/plain")
    public String getAuthenticationScheme(@Context
    SecurityContext securityContext) {
        return securityContext.getAuthenticationScheme();
    }

    @GET
    @Path("userPrincipal")
    @ProduceMime("text/plain")
    public String getUserPrincipal(@Context
    SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if(principal == null)
            return "-";
        return principal.getName();
    }

    @GET
    @Path("secure")
    @ProduceMime("text/plain")
    public String isSecure(@Context
    SecurityContext securityContext) {
        if (!securityContext.isSecure())
            throw new WebApplicationException(Status.CLIENT_ERROR_NOT_FOUND.getCode());
        return "wonderful! It's a secure request.";
    }
}