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
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.AncestorTest;

/**
 * @author Stephan Koops
 * @see AncestorTest
 */
@Path("ancestorTest")
public class AncestorTestService {

    @GET
    @ProduceMime("text/plain")
    @Path("uris")
    public String getUris(@Context UriInfo uriInfo) {
        StringBuilder stb = new StringBuilder();
        List<String> uris = uriInfo.getAncestorResourceURIs();
        stb.append(uris.size());
        for (String uri : uris) {
            stb.append('\n');
            stb.append(uri);
        }
        return stb.toString();
    }

    @GET
    @ProduceMime("text/plain")
    @Path("resourceClassNames")
    public String getResources(@Context UriInfo uriInfo) {
        StringBuilder stb = new StringBuilder();
        List<Object> resources = uriInfo.getAncestorResources();
        stb.append(resources.size());
        for (Object resource : resources) {
            stb.append('\n');
            stb.append(resource.getClass().getName());
        }
        return stb.toString();
    }
    
    @GET
    @ProduceMime("text/plain")
    public String get(@Context UriInfo uriInfo) {
        int uriSize = uriInfo.getAncestorResourceURIs().size();
        int resourcesSize = uriInfo.getAncestorResources().size();
        return uriSize+"\n"+resourcesSize;
    }

    @Path("sub")
    public AncestorTestService getSub() {
        return new AncestorTestService();
    }

    @Path("sameSub")
    public AncestorTestService getSameSub() {
        return getSub();
    }
}