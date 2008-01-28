package org.restlet.test.jaxrs.services;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

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
    public Response get(@Context
    SecurityContext securityContext, String entity, @Context
    UriInfo uriInfo) {
        if (!securityContext.isUserInRole("fabc"))
            throw new WebApplicationException(403);
        entity.toString(); // typically the entity will be stored in the DB.
        String id = "4711";
        URI collectionUri = uriInfo.getRequestUri();
        URI location = UriBuilder.fromUri(collectionUri).path("{id}").build(id);
        return Response.created(location).build();
    }
}