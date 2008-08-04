/*
 * Copyright 2005-2008 Noelios Technologies.
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

import java.math.BigDecimal;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.restlet.data.MediaType;
import org.restlet.test.jaxrs.services.tests.PathParamTest2;

/**
 * @author Stephan Koops
 * @see PathParamTest2
 * @see PathParam
 */
@Path("pathParamTest2/")
public class PathParamTestService2 {

    @GET
    @Produces("text/plain")
    @Path("decoded/{string}")
    public String decoded(@PathParam("string") String string) {
        return string;
    }

    @Encoded
    @GET
    @Produces("text/plain")
    @Path("encoded/{string}")
    public String encoded(@PathParam("string") String string) {
        return string;
    }

    @GET
    @Path("BigDecimal/{id}")
    @Produces("text/plain")
    public String getBigDecimal(@PathParam("id") BigDecimal id) {
        return String.valueOf(id);
    }

    @GET
    @Path("int/{id}")
    @Produces("text/plain")
    public String getInt(@PathParam("id") int id) {
        return String.valueOf(id);
    }

    @GET
    @Path("Integer/{id}")
    @Produces("text/plain")
    public String getInteger(@PathParam("id") Integer id) {
        return String.valueOf(id);
    }

    @GET
    @Path("MediaType/{id}")
    @Produces("text/plain")
    public String getMediaType(@PathParam("id") MediaType id) {
        return String.valueOf(id);
    }

    @GET
    @Path("mn{id}")
    @Produces("text/plain")
    public String getMn(@PathParam("id") int id) {
        return String.valueOf(id);
    }

    @GET
    @Path(value = "multSegm/{string}", limited = false)
    @Produces("text/plain")
    public String getMultSegment(@PathParam("string") String string) {
        return string;
    }

    @GET
    @Path("abc{def}")
    public String getX(@PathParam("def") String def) {
        return def;
    }

    @GET
    @Path("a{bcd}ef/{12}34")
    public String getX2(@PathParam("bcd") String bcd, @PathParam("12") String tt) {
        return bcd + "\n" + tt;
    }
}