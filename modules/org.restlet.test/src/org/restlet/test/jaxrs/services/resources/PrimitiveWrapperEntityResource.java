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

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.test.jaxrs.services.tests.PrimitiveWrapperEntityTest;

/**
 * This resource is to test, what happens, if a primitive and a primitive
 * wrapper is required, also if nothing is given
 * 
 * @author Stephan Koops
 * @see PrimitiveWrapperEntityTest
 */
@Path("PrimitiveWrapperEntity")
@Produces("text/plain")
public class PrimitiveWrapperEntityResource {

    @PUT
    @Path("intReturnInt")
    public int intReturnInt(int i) {
        return i;
    }

    @PUT
    @Path("charReturnCharacter")
    public Character charReturnCharacter(char c) {
        return c;
    }

    @PUT
    @Path("BooleanReturnboolean")
    public boolean BooleanReturnboolean(Boolean b) {
        if (b == null)
            return false;
        return b;
    }

    @PUT
    @Path("integerReturnInteger")
    public Integer integerReturnInteger(Integer i) {
        return i;
    }
}