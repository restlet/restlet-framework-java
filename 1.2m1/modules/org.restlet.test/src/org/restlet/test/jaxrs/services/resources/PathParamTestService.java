/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.restlet.test.jaxrs.services.tests.PathParamTest;

/**
 * @author Stephan Koops
 * @see PathParamTest
 * @see PathParam
 */
@Path("pathParamTest/{var1}")
public class PathParamTestService {

    @Path("checkUnmodifiable/{var1}")
    @GET
    @Produces("text/plain")
    public Object checkUnmodifiable(@PathParam("var1") List<PathSegment> var1s) {
        try {
            var1s.clear();
            throw new WebApplicationException(Response.serverError().entity(
                    "the List must be unmodifiable").build());
        } catch (UnsupportedOperationException uoe) {
            return null;
        }
    }

    @GET
    @Produces("text/plain")
    public String get(@PathParam("var1") String var1) {
        return var1;
    }

    @Path("abc/{var2}/def")
    @GET
    @Produces("text/plain")
    public String get(@PathParam("var1") String var1,
            @PathParam("var2") String var2) {
        return var1 + "\n" + var2;
    }
    
    @GET
    @Path("regExp/{buchstabe:[a-zA-Z]}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRegExpEinBuchstabe(@PathParam("buchstabe") String buchstabe) {
        return "ein Buchstabe: "+buchstabe;
    }
    
    @GET
    @Path("regExp/{string}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRegExpSonstwas(@PathParam("string") String string) {
        return "anderes: "+string;
    }

    @GET
    @Path("regExp/{zahl:[-]?[0-9]+}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRegExpZahl(@PathParam("zahl") int zahl) {
        return "Zahl: "+zahl;
    }

    @Path("st/{var1}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getVar1(@PathParam("var1") String var1) {
        return var1;
    }
}