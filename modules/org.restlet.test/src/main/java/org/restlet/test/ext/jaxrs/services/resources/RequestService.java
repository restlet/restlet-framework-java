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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.test.ext.jaxrs.services.others.OPTIONS;
import org.restlet.test.ext.jaxrs.services.tests.RequestTest;

/**
 * @author Stephan Koops
 * @see RequestTest
 * @see Request
 * @see CallContext
 */
@Path("/requestTestService")
public class RequestService {

    public static final String GERMAN_TEXT = "Text auf deutsch";

    public static final String ENGLISH_TEXT = "Text in english";

    public static EntityTag getEntityTagFromDatastore() {
        return new EntityTag("validEntityTag");
    }

    public static Date getLastModificationDateFromDatastore() {
        return new Date(1199790000000l); // 2009-01-08, 12h
    }

    @GET
    @Path("date")
    @Produces("text/plain")
    public Response get(@Context
    Request request) {
        final Date modificDate = getLastModificationDateFromDatastore();
        final EntityTag entityTag = getEntityTagFromDatastore();
        final ResponseBuilder resp = request.evaluatePreconditions(modificDate,
                entityTag);
        if (resp != null) {
            return resp.build();
        }
        // Build new Response
        final ResponseBuilder responseBuilder = Response.status(200);
        responseBuilder.entity("This is the Entity from " + modificDate);
        responseBuilder.lastModified(modificDate);
        responseBuilder.tag(entityTag);
        return responseBuilder.build();
    }

    @GET
    @Path("selectVariants")
    public Response getSelectVariants(@Context
    Request request) {
        // TEST VariantListBuilder
        final List<Variant> variants = new ArrayList<Variant>();
        variants.add(new Variant(MediaType.TEXT_HTML_TYPE, Locale.ENGLISH, null));
        variants.add(new Variant(MediaType.TEXT_PLAIN_TYPE, Locale.ENGLISH,
                null));
        variants.add(new Variant(MediaType.TEXT_HTML_TYPE, Locale.GERMAN, null));
        variants.add(new Variant(MediaType.TEXT_PLAIN_TYPE, Locale.GERMAN, null));
        final Variant variant = request.selectVariant(variants);
        if (variant == null) {
            return Response.notAcceptable(variants).build();
        }
        String entity;
        if (variant.getLanguage().equals("en")) {
            entity = ENGLISH_TEXT;
        } else {
            entity = GERMAN_TEXT;
        }
        return Response.ok(entity).variant(variant).build();
    }

    @OPTIONS
    public Response options() {
        return Response.ok().header("Allow", "ABC, DEF").header("Allow", "GHI")
                .build();
    }

    @PUT
    @Path("date")
    public Response put(@Context
    Request request) {
        final Date modificDate = getLastModificationDateFromDatastore();
        final EntityTag entityTag = getEntityTagFromDatastore();
        final ResponseBuilder resp = request.evaluatePreconditions(modificDate,
                entityTag);
        if (resp != null) {
            return resp.build();
        }
        // Build new Response
        final ResponseBuilder responseBuilder = Response.status(200);
        return responseBuilder.build();
    }
}
