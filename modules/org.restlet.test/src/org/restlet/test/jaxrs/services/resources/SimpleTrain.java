/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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