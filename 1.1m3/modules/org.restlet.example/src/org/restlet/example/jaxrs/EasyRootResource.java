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
package org.restlet.example.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

/**
 * This demonstrates an very easy JAX-RS resource. It is available on relative
 * URI "easy" (see annotation &#64;{@link Path} on this class). This resource
 * has representations of two media types: "text/html" ({@link #getHtml()} and
 * "text/plain").
 * 
 * @author Stephan Koops
 * @see ExampleAppConfig
 */
@Path("easy")
public class EasyRootResource {

    /**
     * Returns a HTML representation of this resource.
     * 
     * @return a HTML representation of this resource.
     */
    @GET
    @ProduceMime("text/html")
    public String getHtml() {
        return "<html><head></head><body>\n"
                + "This is an easy resource (as html text).\n"
                + "</body></html>";
    }

    /**
     * Returns a plain text representation of this resource.
     * 
     * @return a plain text representation of this resource.
     */
    @GET
    @ProduceMime("text/plain")
    public String getPlain() {
        return "This is an easy resource (as plain text)";
    }
}