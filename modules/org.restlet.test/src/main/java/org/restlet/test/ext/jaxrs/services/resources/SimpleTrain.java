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

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.restlet.test.ext.jaxrs.services.tests.SimpleTrainTest;

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

    public SimpleTrain(@HeaderParam("p")
    String p) {
        "".equals(p);
        // this is a valid constructor
    }

    public SimpleTrain(String x, @HeaderParam("p")
    String p) {
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
    public String getTemplParamDecoded(@Context
    UriInfo uriInfo) {
        try {
            uriInfo.getPathParameters(true).add("jkghjk", "khlokh");
            return "The Template Parameter MultivaluedMap must be unmodifiable.";
        } catch (UnsupportedOperationException e) {
            // ok
        }
        return uriInfo.getPathParameters(true).getFirst("string");
    }

    @GET
    @Path("encode/{string}")
    @Produces("text/plain")
    public String getTemplParamEncoded(@Context
    UriInfo uriInfo) {
        try {
            uriInfo.getPathParameters(false).add("jkghjk", "khlokh");
            return "The Template Parameter MultivaluedMap must be unmodifiable.";
        } catch (UnsupportedOperationException e) {
            // ok
        }
        return uriInfo.getPathParameters(false).getFirst("string");
    }
}
