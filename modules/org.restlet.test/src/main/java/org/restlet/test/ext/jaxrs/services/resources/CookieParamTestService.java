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

package org.restlet.test.ext.jaxrs.services.resources;

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

import org.restlet.test.ext.jaxrs.services.tests.CookieParamTest;

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
    public String array(@CookieParam("c")
    Cookie[] cc) {
        String result = "[";
        for (final Cookie c : cc) {
            result += c.getValue() + ", ";
        }
        return result.substring(0, result.length() - 2) + "]";
    }

    @GET
    @Produces("text/plain")
    public String get(@CookieParam("c")
    String cookieValue) {
        return cookieValue;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("Set")
    public String set(@CookieParam("c")
    Set<Cookie> cc) {
        String result = "{";
        for (final Cookie c : cc) {
            result += c.getValue() + ", ";
        }
        return result.substring(0, result.length() - 2) + "}";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("SortedSet")
    public String sortedSet(@CookieParam("c")
    SortedSet<String> cc) {
        return cc.toString();
    }

    @GET
    @Produces("text/plain")
    @Path("withDefault")
    @Encoded
    public String withDefault(@CookieParam("c")
    @DefaultValue("default")
    String cookieValue) {
        return cookieValue;
    }
}
