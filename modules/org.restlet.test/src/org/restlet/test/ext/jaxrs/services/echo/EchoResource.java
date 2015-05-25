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

package org.restlet.test.ext.jaxrs.services.echo;

import java.awt.Point;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path(value = EchoResource.path)
public interface EchoResource {
    public static final String path = "echo";

    @GET
    @Path("point-header")
    @Consumes(MediaType.APPLICATION_JSON)
    Point echoPointHeaderParam(@HeaderParam("point")
    Point point);

    @GET
    @Path("point-query")
    @Consumes(MediaType.APPLICATION_JSON)
    Point echoPointQueryParam(/**
     * using @Deprecated to test the annotation
     * mapping logic
     */
    @Deprecated
    @QueryParam("point")
    Point point);

    @POST
    String echo(String input);

    @GET
    @Path("point-path/{point}")
    @Consumes(MediaType.APPLICATION_JSON)
    Point echoPointPathParam(@PathParam("point")
    Point point);

    @GET
    @Path("point-path/{input:[Tt]his_Is_A_Test\\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    String echoStringRegexPathParam(@PathParam("input")
    String input);

    @GET
    @Path("point-cookie")
    @Consumes(MediaType.APPLICATION_JSON)
    Point echoPointCookieParam(@CookieParam("point")
    Point point);

    @POST
    @Path("point-form")
    String echoStringFormParam(@FormParam("point")
    String value);

}
