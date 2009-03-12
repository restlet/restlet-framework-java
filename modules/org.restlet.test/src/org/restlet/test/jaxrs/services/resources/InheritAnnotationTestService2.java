/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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

    public static final String RETURN_STRING = "fromGetTextResource";

    public static final String RETURN_STRING_SUB = "fromGetTextExtResource";

    public static final String RETURN_STRING_SUB2 = "fromGetTextExt2Resource";

    @Path("getSubClassText")
    public SubClassResource getSubClass() {
        return new SubClassResource();
    }

    @Path("getText")
    public GetTextResource getText() {
        return new GetTextResource();
    }
}