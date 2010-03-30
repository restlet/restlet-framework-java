/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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