/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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