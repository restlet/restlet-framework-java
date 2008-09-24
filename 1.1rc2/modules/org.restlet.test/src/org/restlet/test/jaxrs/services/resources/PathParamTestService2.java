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
    @Path("multSegm/{string:.*}")
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