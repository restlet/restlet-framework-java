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
package org.restlet.test.jaxrs.wrappers;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathOnClassException;
import org.restlet.ext.jaxrs.internal.util.RemainingPath;
import org.restlet.ext.jaxrs.internal.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.internal.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperFactory;

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

    private static final WrapperFactory wrapperFactory = new WrapperFactory(
            new ThreadLocalizedContext(), null, null, null, Logger
                    .getAnonymousLogger());

    public void testIllegalMethodPath() throws Exception {
        final RootResourceClass rrc = wrapperFactory
                .getRootResourceClass(IllegalMethPathRrc.class);
        @SuppressWarnings("unused")
        Collection<ResourceMethod> rms;
        rms = rrc.getMethodsForPath(new RemainingPath("abc"));
        rms = rrc.getMethodsForPath(new RemainingPath(""));
        rms = rrc.getMethodsForPath(new RemainingPath("subpath"));
    }

    public void testIllegalRrcPath() throws Exception {
        try {
            final RootResourceClass rrc = wrapperFactory
                    .getRootResourceClass(IllegalRrcPathRrc.class);
            fail("must fail");
        } catch (final IllegalPathOnClassException iae) {
            // good
        }
    }
}