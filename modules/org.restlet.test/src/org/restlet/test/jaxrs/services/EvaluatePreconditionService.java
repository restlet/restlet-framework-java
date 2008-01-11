package org.restlet.test.jaxrs.services;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/requestTestService")
public class EvaluatePreconditionService {

    @GET
    @Path("date")
    public Response m1(@HttpContext Request request) {
        Date modificationDate = getLastModificationDateFromDatastore();
        Response resp = request.evaluatePreconditions(modificationDate);
        if (resp != null)
            return resp;
        // Build new Response
        ResponseBuilder responseBuilder = Response.status(200);
        responseBuilder.entity("This is the Entity from " + modificationDate);
        responseBuilder.lastModified(modificationDate);
        return responseBuilder.build();
    }

    public static Date getLastModificationDateFromDatastore() {
        return new Date(1199790000000l); // 2008-01-08, 12h
    }
}
