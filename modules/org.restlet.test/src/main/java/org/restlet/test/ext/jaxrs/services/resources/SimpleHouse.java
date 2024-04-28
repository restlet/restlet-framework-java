/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.jaxrs.services.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.test.ext.jaxrs.services.tests.SimpleHouseTest;

/**
 * This class contains only data for one media type.
 * 
 * @author Stephan Koops
 * @see SimpleHouseTest
 */
@Path("/ho%20use")
public class SimpleHouse {
    /** Text of the Plain-Text-Representation. */
    public static final String RERP_PLAIN_TEXT = "  /\\ \n /  \\ \n |  | \n +--+ \n \n This is a simple text house";

    @GET
    @Path("null")
    public Object getNull() {
        return null;
    }

    @GET
    @Path("nullWithMediaType")
    @Produces("text/plain")
    public Object getNullWithMediaType() {
        return null;
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
}
