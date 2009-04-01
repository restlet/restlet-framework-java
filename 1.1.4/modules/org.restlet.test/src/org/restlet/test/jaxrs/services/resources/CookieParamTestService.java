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
        for (final Cookie c : cc) {
            result += c.getValue() + ", ";
        }
        return result.substring(0, result.length() - 2) + "]";
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
        for (final Cookie c : cc) {
            result += c.getValue() + ", ";
        }
        return result.substring(0, result.length() - 2) + "}";
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
    public String withDefault(
            @CookieParam("c") @DefaultValue("default") String cookieValue) {
        return cookieValue;
    }
}