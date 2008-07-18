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

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.jaxrs.services.tests.SimpleTrainTest;

/**
 * One of the first test services.
 * 
 * @author Stephan Koops
 * @see SimpleTrainTest
 */
@Path(SimpleTrain.PATH)
public class SimpleTrain {
    /**
     * The path is here available to check it from the tests. It is not required
     * for JAX-RS
     */
    public static final String PATH = "/train";

    public static boolean checkForValidConstructor = true;

    /** Text der ausgegebenen Plain-Text-Representation. */
    public static final String RERP_PLAIN_TEXT = "This is a simple text train";

    /** Text der ausgegebenen HTML-Text-Representation. */
    public static final String RERP_HTML_TEXT = "<html><body>This is a simple html train</body></html>";

    public SimpleTrain() {
        if (checkForValidConstructor) {
            throw new RuntimeException(
                    "This Constructor is not allowed, because another constructors has more elements");
        }
    }

    public SimpleTrain(Integer x) {
        "".equals(x);
        if (checkForValidConstructor) {
            throw new RuntimeException(
                    "This Constructor is not allowed, because the paramters are not correct annotated");
        }
    }

    public SimpleTrain(@HeaderParam("p") String p) {
        "".equals(p);
        // this is a valid constructor
    }

    public SimpleTrain(String x, @HeaderParam("p") String p) {
        "".equals(p);
        "".equals(x);
        if (checkForValidConstructor) {
            throw new RuntimeException(
                    "This Constructor is not allowed, because one of the parameters are not correct annotated");
        }
    }

    /**
     * 
     * @return
     */
    @GET
    @Produces("text/html")
    public String getHtmlText() {
        return RERP_HTML_TEXT;
    }

    /**
     * 
     * @return
     */
    @GET
    @Produces("text/plain")
    public String getPlainText() {
        return RERP_PLAIN_TEXT;
    }

    @GET
    @Path("decode/{string}")
    @Produces("text/plain")
    public String getTemplParamDecoded(@Context UriInfo uriInfo) {
        try {
            uriInfo.getPathParameters(true).add("jkghjk", "khlokh");
            return "The Template Parameter MultivaluedMap must be unmodifiable.";
        } catch (final UnsupportedOperationException e) {
            // ok
        }
        return uriInfo.getPathParameters(true).getFirst("string");
    }

    @GET
    @Path("encode/{string}")
    @Produces("text/plain")
    public String getTemplParamEncoded(@Context UriInfo uriInfo) {
        try {
            uriInfo.getPathParameters(false).add("jkghjk", "khlokh");
            return "The Template Parameter MultivaluedMap must be unmodifiable.";
        } catch (final UnsupportedOperationException e) {
            // ok
        }
        return uriInfo.getPathParameters(false).getFirst("string");
    }
}