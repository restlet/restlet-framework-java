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
import javax.ws.rs.Path;

import org.restlet.test.ext.jaxrs.services.tests.IllegalThingsTest;

/**
 * This class contains only data for one media type
 * 
 * @author Stephan Koops
 * @see IllegalThingsTest
 */
@Path("/illegalThingsInternal")
public class IllegalThingsTestService {

    @GET
    @Path("package")
    String getPackageVisible() {
        return "this method is package visible. Is there a warning?";
    }

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
    @Path("private")
    String getPrivateVisible() {
        return "this method is private visible. Is there a warning?";
    }

    @GET
    @Path("protected")
    protected String getProtected() {
        return "this method is protected. Is there a warning?";
    }
}
