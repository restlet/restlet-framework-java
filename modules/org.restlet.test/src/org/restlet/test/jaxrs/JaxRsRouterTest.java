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

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.restlet.ext.jaxrs.AllowAllAccess;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.test.jaxrs.services.path.IllegalPathService1;
import org.restlet.test.jaxrs.services.path.IllegalPathService2;
import org.restlet.test.jaxrs.services.path.IllegalPathService3;
import org.restlet.test.jaxrs.services.resources.DoublePath1;
import org.restlet.test.jaxrs.services.resources.DoublePath2;
import org.restlet.test.jaxrs.services.resources.SimpleTrain;

/**
 * 
 * @author Stephan
 * @see JaxRsRouter
 * @see DoublePath1
 * @see DoublePaths
 * @see SimpleTrain
 * @see IllegalPathService1
 * @see IllegalPathService2
 * @see IllegalPathService3
 */
@SuppressWarnings("all")
public class JaxRsRouterTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAttachDouble() throws Exception {
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                return (Set) Util.createSet(DoublePath1.class,
                        DoublePath1.class);
            }
        };
        // must create without Exception
        new JaxRsRouter(null, appConfig);
    }

    public void testAttachSamePathDouble() throws Exception {
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                return (Set) Util.createSet(DoublePath1.class, DoublePath2.class);
            }
        };
        try {
            JaxRsRouter router = new JaxRsRouter(null, appConfig);
            fail("Attach two root resource classes with the same @Path must raise an Excption");
        } catch (IllegalArgumentException e) {
            // wunderful, exception raised :-)
        }
    }

    public void test3() {
        if(true) // LATER ungueltigen Pfad feststellen
            return;
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                return (Set) Util.createSet(SimpleTrain.class, IllegalPathService1.class);
            }
        };
        try {
            JaxRsRouter router = new JaxRsRouter(null, appConfig);
            fail("must not fail");
        } catch (IllegalArgumentException e) {
            // wunderful, exception raised :-)
        }
    }

    public void test4() {
        if(true) // LATER ungueltigen Pfad feststellen
            return;
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                return (Set) Util.createSet(SimpleTrain.class, IllegalPathService2.class);
            }
        };
        try {
            JaxRsRouter router = new JaxRsRouter(null, appConfig);
            fail("must not fail");
        } catch (IllegalArgumentException e) {
            // wunderful, exception raised :-)
        }
    }

    public void test5() {
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                return (Set) Util.createSet(IllegalPathService3.class);
            }
        };
        try {
            JaxRsRouter router = new JaxRsRouter(null, appConfig);
            fail("must not fail");
        } catch (IllegalArgumentException e) {
            // wunderful, exception raised :-)
        }
    }
}