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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.test.jaxrs.util.TestUtils;

/**
 * Test what happens when two methods should be use for the same request.
 * 
 * @author Stephan Koops
 * @see ResponseBuilder
 * @see Response.ResponseBuilder
 */
@Path("/responseBuilder")
public class ResponseBuilderService {

    @DELETE
    public Response delete() {
        return Response.ok().build();
    }

    @GET
    @Path("1")
    public Response get1() {
        final List<Variant> variants = new ArrayList<Variant>();
        variants.add(new Variant(TestUtils.createMediaType("text", "sjk"),
                Locale.ENGLISH, "encoding"));
        variants.add(new Variant(TestUtils.createMediaType("text", "sfgs"),
                Locale.ENGLISH, "encoding2"));
        variants.add(new Variant(TestUtils.createMediaType("text", "ydgdsfg"),
                Locale.ENGLISH, "encoding"));
        variants.add(new Variant(TestUtils.createMediaType("text", "sjk"),
                Locale.ENGLISH, "encoding2"));
        return Response.notAcceptable(variants).build();
    }

    @GET
    @Path("2")
    public Response get2() {
        final List<Variant> variants = new ArrayList<Variant>();
        variants.add(new Variant(TestUtils.createMediaType("text", "sjk"),
                Locale.ENGLISH, "encoding"));
        variants.add(new Variant(TestUtils.createMediaType("text", "sjk",
                "charset", "enc"), Locale.ENGLISH, "encoding"));
        variants.add(new Variant(TestUtils.createMediaType("text", "sjk",
                "charset", "skl"), Locale.ENGLISH, "encoding"));
        variants.add(new Variant(TestUtils.createMediaType("text", "sjk"),
                Locale.GERMAN, "encoding"));
        return Response.notAcceptable(variants).build();
    }
}