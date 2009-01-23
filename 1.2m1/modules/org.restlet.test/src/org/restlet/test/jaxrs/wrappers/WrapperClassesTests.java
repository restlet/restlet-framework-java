/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.wrappers;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import junit.framework.TestCase;

import org.restlet.Context;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceClasses;

/**
 * Tests for classes in package {@link org.restlet.ext.jaxrs.internal.wrappers}.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("all")
public class WrapperClassesTests extends TestCase {

    @Path("abc")
    static class IllegalMethPathRrc {
        public IllegalMethPathRrc() {
        }

        @GET
        @Path(";a=b")
        public String get() {
            return "resource value for matrix parameter a=b";
        }

        @GET
        @Path("abc")
        public String getAbc() {
            return "sub resource value for abc without matrix parameters";
        }

        @GET
        @Path("subpath;a=b")
        public String getSubPath() {
            return "sub resource value for subpath matrix parameter a";
        }
    }

    /**
     * This root resource class contains an illegal path (matrix parameter)
     * 
     * @author Stephan Koops
     */
    @Path("df;gu=34")
    static class IllegalRrcPathRrc {
        public IllegalRrcPathRrc() {
        }

        @GET
        public String get() {
            return "whatever";
        }
    }

    private static final ResourceClasses resourceClasses = new ResourceClasses(
            new ThreadLocalizedContext(), null, null, Logger
                    .getAnonymousLogger());

    public void testIllegalMethodPath() throws Exception {
        final RootResourceClass rrc = RootResourceClassTest
                .getPerRequestRootClassWrapper(resourceClasses,
                        IllegalMethPathRrc.class);
        @SuppressWarnings("unused")
        Collection<ResourceMethod> rms;
        rms = rrc.getMethodsForPath(new RemainingPath("abc"));
        rms = rrc.getMethodsForPath(new RemainingPath(""));
        rms = rrc.getMethodsForPath(new RemainingPath("subpath"));
    }

    public void testIllegalRrcPath() throws Exception {
        try {
            final RootResourceClass rrc = RootResourceClassTest
                    .getPerRequestRootClassWrapper(resourceClasses,
                            IllegalRrcPathRrc.class);
            fail("must fail");
        } catch (IllegalPathOnClassException iae) {
            // good
        }
    }
}