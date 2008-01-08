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
import javax.ws.rs.Path;

/**
 * Test what happens when two methods should be use for the same request
 * 
 * @author Stephan Koops
 * 
 */
@Path("/doubleMethodsForResources")
public class DoublePath2 {

    private static final String ANOTHER_RESULT = null;

    @GET
    public String getResource() {
        return ANOTHER_RESULT;
    }
}