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

import javax.ws.rs.CookieParam;
import javax.ws.rs.Path;

import org.restlet.test.ext.jaxrs.services.tests.IllegalConstructorTest;

/**
 * This resource is used to test, if a constructor with a requirement to the
 * entity is ignored, and also a constructor which is not public.
 * 
 * @author Stephan Koops
 * @see IllegalConstructorTest
 */
@Path("IllegalConstructorResource")
@SuppressWarnings("all")
public class IllegalConstructorResource {

    /**
     * not to call, because it requires the entity
     * 
     * @param entity
     */
    public IllegalConstructorResource(String entity) {
    }

    /**
     * not to call because it is not public
     * 
     * @param c1
     * @param c2
     */
    IllegalConstructorResource(@CookieParam("c1")
    String c1, @CookieParam("c2")
    String c2) {
    }
}
