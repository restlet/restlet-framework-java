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
import javax.ws.rs.Produces;

import org.restlet.test.jaxrs.services.tests.InheritAnnotationTest;

/**
 * @author Stephan Koops
 * @see InheritAnnotationTest
 * @see InheritAnnotationTestServiceInterface
 */
@Path("1234")
public class InheritAnnotationTestService2 implements
        InheritAnnotationTestServiceInterface {

    public static final String RETURN_STRING = "fromGetTextResource";

    public static final String RETURN_STRING_SUB = "fromGetTextExtResource";

    public static final String RETURN_STRING_SUB2 = "fromGetTextExt2Resource";

    @Path("getText")
    public GetTextResource getText() {
        return new GetTextResource();
    }

    @Path("getSubClassText")
    public SubClassResource getSubClass() {
        return new SubClassResource();
    }

    public class GetTextResource {
        @GET
        @Produces("text/plain")
        public String get() {
            return RETURN_STRING;
        }
    }

    public class SubClassResource extends GetTextResource {
        @Override
        public String get() {
            return RETURN_STRING_SUB;
        }

        @Path("sub")
        public SubClassResource2 getSub() {
            return new SubClassResource2();
        }
    }

    public class SubClassResource2 {
        @GET
        @Produces("text/plain")
        public String get() {
            return RETURN_STRING_SUB2;
        }
    }
}