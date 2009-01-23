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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.UriBuilderByServiceTest;

/**
 * @author Stephan Koops
 * @see UriBuilderByServiceTest
 */
@Path("UriBuilder")
public class UriBuilderTestResource {

    @Context
    UriInfo uriInfo;

    @GET
    @Path("absolute")
    @Produces( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String getAbsoluteUriBuilder() {
        return this.uriInfo.getAbsolutePathBuilder().build().toString();
    }

    @GET
    @Path("base")
    @Produces( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String getBaseUriBuilder() {
        return this.uriInfo.getBaseUriBuilder().build().toString();
    }

    @POST
    @Path("absolute")
    @Produces( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String postAbsoluteUriBuilder() {
        // LATER test also with uri of sub resource
        return this.uriInfo.getAbsolutePathBuilder().build().toString();
    }

    @POST
    @Path("base")
    @Produces( { MediaType.TEXT_PLAIN, MediaType.TEXT_HTML })
    public String postBaseUriBuilder() {
        return this.uriInfo.getBaseUriBuilder().build().toString();
    }
}