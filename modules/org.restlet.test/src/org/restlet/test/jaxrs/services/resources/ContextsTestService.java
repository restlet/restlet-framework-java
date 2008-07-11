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
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.restlet.test.jaxrs.services.tests.ContextsTest;

/**
 * @author Stephan Koops
 * @see ContextsTest
 */
@Path("anchestorTest")
public class ContextsTestService {
    
    @Context Providers messageBodyWorkers;
    
    @Context ContextResolver<Integer> contextResolver;
    
    @Context UriInfo uriInfo;

    @GET
    @Produces("text/plain")
    @Path("fields")
    public String fieldsAvailable() {
        StringBuilder stb = new StringBuilder();
        if(messageBodyWorkers != null)
            stb.append("messageBodyWorkers\n");
        if(contextResolver != null)
            stb.append("contextResolver\n");
        if(uriInfo != null)
            stb.append("uriInfo\n");
        return stb.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("params")
    public String getResources(@Context UriInfo uriInfo,
            @Context Providers messageBodyWorkers,
            @Context ContextResolver<Integer> contextResolver) {
        StringBuilder stb = new StringBuilder();
        if(messageBodyWorkers != null)
            stb.append("messageBodyWorkers\n");
        if(contextResolver != null)
            stb.append("contextResolver\n");
        if(uriInfo != null)
            stb.append("uriInfo\n");
        return stb.toString();
    }
    
    @GET
    @Produces("text/plain")
    @Path("lastPathSegm")
    public String getPathSegm(@Context PathSegment lastPathSegment) {
        Set<Entry<String, List<String>>> entries;
        entries = lastPathSegment.getMatrixParameters().entrySet();
        StringBuilder stb = new StringBuilder();
        for(Map.Entry<String, List<String>> entry : entries)
            stb.append(entry.getKey()+" : "+entry.getValue()+"\n");
        return stb.toString();
    }
}