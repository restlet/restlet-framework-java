/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.test.jaxrs.services;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

/**
 * This class contains only data for one media type
 * 
 * @author Stephan Koops
 * 
 */
@Path("/ho%20use")
public class SimpleHouse {
    /** Text der ausgegebenen Plain-Text-Repräsentation. */
    public static final String RERP_PLAIN_TEXT = "  /\\ \n /  \\ \n |  | \n +--+ \n \n This is a simple text house";

    /**
     * 
     * @return
     */
    @GET
    @ProduceMime("text/plain")
    public String getPlainText() {
        return RERP_PLAIN_TEXT;
    }
    
    @HEAD
    @Path("headTest1")
    @ProduceMime("text/html")
    public String headTest1()
    {
        return null;
    }

    @GET
    @Path("headTest1")
    @ProduceMime("text/html")
    public String getTest1()
    {
        return "4711";
    }

    @HEAD
    @Path("headTest2")
    @ProduceMime("text/html")
    public String headTest2()
    {
        return "4711";
    }

    @GET
    @Path("headTest2")
    @ProduceMime("text/html")
    public String getTest2()
    {
        return "4711";
    }
}