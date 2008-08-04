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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.resources;

import javax.ws.rs.CookieParam;
import javax.ws.rs.Path;

import org.restlet.test.jaxrs.services.tests.IllegalConstructorTest;

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
    IllegalConstructorResource(@CookieParam("c1") String c1,
            @CookieParam("c2") String c2) {
    }
}
