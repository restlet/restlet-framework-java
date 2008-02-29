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

package org.restlet.test.jaxrs;

import java.util.Collection;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.restlet.ext.jaxrs.util.RemainingPath;
import org.restlet.ext.jaxrs.wrappers.ResourceMethod;
import org.restlet.ext.jaxrs.wrappers.RootResourceClass;
import org.restlet.ext.jaxrs.wrappers.WrapperFactory;

import junit.framework.TestCase;

/**
 * @author Stephan Koops
 * 
 */
public class WrapperClassesTests extends TestCase {

    private static final WrapperFactory wrapperFactory = new WrapperFactory(
            Logger.getAnonymousLogger());

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

    @Path("abc")
    static class IllegalMethPathRrc {
        public IllegalMethPathRrc() {
        }

        @GET
        @Path("subpath;a=b")
        public String getSubPath() {
            return "sub resource value for subpath matrix parameter a";
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
    }

    public void testIllegalRrcPath() throws Exception {
        RootResourceClass rrc = wrapperFactory
                .getRootResourceClass(IllegalRrcPathRrc.class);
    }

    public void testIllegalMethodPath() throws Exception {
        RootResourceClass rrc = wrapperFactory
                .getRootResourceClass(IllegalMethPathRrc.class);
        Collection<ResourceMethod> rms;
        rms = rrc.getMethodsForPath(new RemainingPath("abc"));
        rms = rrc.getMethodsForPath(new RemainingPath(""));
        rms = rrc.getMethodsForPath(new RemainingPath("subpath"));
    }
}