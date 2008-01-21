package org.restlet.test.jaxrs.services;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/requestTestService")
public class EvaluatePreconditionService {

    @GET
    @Path("date")
    public Response get(@HttpContext
    Request request) {
        Date modificDate = getLastModificationDateFromDatastore();
        EntityTag entityTag = getEntityTagFromDatastore();
        Response resp = request.evaluatePreconditions(modificDate, entityTag);
        if (resp != null)
            return resp;
        // Build new Response
        ResponseBuilder responseBuilder = Response.status(200);
        responseBuilder.entity("This is the Entity from " + modificDate);
        responseBuilder.lastModified(modificDate);
        responseBuilder.tag(entityTag);
        return responseBuilder.build();
    }

    @PUT
    @Path("date")
    public Response put(@HttpContext
    Request request) {
        Date modificDate = getLastModificationDateFromDatastore();
        EntityTag entityTag = getEntityTagFromDatastore();
        Response resp = request.evaluatePreconditions(modificDate, entityTag);
        if (resp != null)
            return resp;
        // Build new Response
        ResponseBuilder responseBuilder = Response.status(200);
        return responseBuilder.build();
    }

    public static EntityTag getEntityTagFromDatastore() {
        return new EntityTag("validEntityTag");
    }

    public static Date getLastModificationDateFromDatastore() {
        return new Date(1199790000000l); // 2008-01-08, 12h
    }

    @OPTIONS
    public Response options() {
        return Response.ok().header("Allow", "ABC, DEF").header("Allow", "GHI")
                .build();
    }
}