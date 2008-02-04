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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/requestTestService")
public class RequestService {

    public static final String GERMAN_TEXT = "Text auf deutsch";
    public static final String ENGLISH_TEXT = "Text in english";

    @GET
    @Path("date")
    public Response get(@Context
    Request request) {
        Date modificDate = getLastModificationDateFromDatastore();
        EntityTag entityTag = getEntityTagFromDatastore();
        ResponseBuilder resp = request.evaluatePreconditions(modificDate, entityTag);
        if (resp != null)
            return resp.build();
        // Build new Response
        ResponseBuilder responseBuilder = Response.status(200);
        responseBuilder.entity("This is the Entity from " + modificDate);
        responseBuilder.lastModified(modificDate);
        responseBuilder.tag(entityTag);
        return responseBuilder.build();
    }

    @PUT
    @Path("date")
    public Response put(@Context
    Request request) {
        Date modificDate = getLastModificationDateFromDatastore();
        EntityTag entityTag = getEntityTagFromDatastore();
        ResponseBuilder resp = request.evaluatePreconditions(modificDate, entityTag);
        if (resp != null)
            return resp.build();
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
    
    @GET
    @Path("selectVariants")
    public Response getSelectVariants(@Context
    Request request)
    {
        List<Variant> variants = new ArrayList<Variant>();
        variants.add(new Variant(MediaType.parse("text/html"), "en", null));
        variants.add(new Variant(MediaType.parse("text/plain"), "en", null));
        variants.add(new Variant(MediaType.parse("text/html"), "de", null));
        variants.add(new Variant(MediaType.parse("text/plain"), "de", null));
        Variant variant = request.selectVariant(variants);
        if(variant == null)
            return Response.notAcceptable(variants).build();
        String entity;
        if(variant.getLanguage().equals("en"))
            entity = ENGLISH_TEXT;
        else
            entity = GERMAN_TEXT;
        return Response.ok(entity).variant(variant).build();
    }
}