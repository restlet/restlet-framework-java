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

import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

import org.restlet.test.jaxrs.services.tests.CookieParamTest;

/**
 * @author Stephan Koops
 * @see CookieParamTest
 * @see CookieParam
 */
@Path("cookieParamTest")
public class CookieParamTestService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("array")
    public String array(@CookieParam("c") Cookie[] cc) {
        String result = "[";
        for(Cookie c : cc)
            result+=c.getValue()+", ";
        return result.substring(0, result.length()-2)+"]";
    }

    @GET
    @Produces("text/plain")
    public String get(@CookieParam("c") String cookieValue) {
        return cookieValue;
    }
   
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("Set")
    public String set(@CookieParam("c") Set<Cookie> cc) {
        String result = "{";
        for(Cookie c : cc)
            result+=c.getValue()+", ";
        return result.substring(0, result.length()-2)+"}";
    }
   
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("SortedSet")
    public String sortedSet(@CookieParam("c") SortedSet<String> cc) {
        return cc.toString();
    }
    
    @GET
    @Produces("text/plain")
    @Path("withDefault")
    @Encoded
    public String withDefault(@CookieParam("c") @DefaultValue("default")
               String cookieValue) {
        return cookieValue;
    }
}