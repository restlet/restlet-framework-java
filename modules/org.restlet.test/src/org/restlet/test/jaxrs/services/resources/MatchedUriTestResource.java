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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * @author Stephan Koops
 * @see org.restlet.test.jaxrs.services.tests.AncestorTest
 * @see UriInfo#getAncestorResources()
 * @see UriInfo#getAncestorResourceURIs()
 */
@Path("ancestorTest")
public class MatchedUriTestResource {

    @SuppressWarnings("unused") // will be used later
    @Context
    private UriInfo mainUriInfo;

    @GET
    @Produces("text/plain")
    public String get(@Context UriInfo uriInfo) {
        final int uriSize = uriInfo.getAncestorResourceURIs().size();
        final int resourcesSize = uriInfo.getAncestorResources().size();
        return uriSize + "\n" + resourcesSize;
    }

    @GET
    @Produces("text/plain")
    @Path("sub")
    public String getResources(@Context UriInfo uriInfo) {
        final StringBuilder stb = new StringBuilder();
        final List<Object> resources = uriInfo.getAncestorResources();
        stb.append(resources.size());
        for (final Object resource : resources) {
            stb.append('\n');
            stb.append(resource.getClass().getName());
        }
        return stb.toString();
    }
}