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
import javax.ws.rs.Path;

/**
 * This class contains only data for one media type
 * 
 * @author Stephan Koops
 * 
 */
@Path("/illegalThingsInternal")
public class IllegalThingsTestService {

    /**
     * This sub resource locator returns null; that is not allowed
     * 
     * @return
     */
    @Path("nullSubResource")
    public Object getPlainText() {
        return null;
    }

    @GET
    @Path("protected")
    protected String getProtected() {
        return "this method is protected. Is there a warning?";
    }

    @GET
    @Path("package")
    String getPackageVisible() {
        return "this method is package visible. Is there a warning?";
    }

    @GET
    @Path("private")
    String getPrivateVisible() {
        return "this method is private visible. Is there a warning?";
    }
}