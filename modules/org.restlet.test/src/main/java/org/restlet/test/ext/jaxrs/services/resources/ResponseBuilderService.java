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
import java.util.List;
import java.util.Locale;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.test.ext.jaxrs.util.TestUtils;

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
