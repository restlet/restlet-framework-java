/*
 * Copyright 2005-2008 Noelios Technologies.
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
