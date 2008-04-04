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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.provider.ExceptionMappers;
import org.restlet.test.jaxrs.services.path.IllegalPathService1;
import org.restlet.test.jaxrs.services.path.IllegalPathService2;
import org.restlet.test.jaxrs.services.path.IllegalPathService3;
import org.restlet.test.jaxrs.services.providers.IllegalArgExcMapper;
import org.restlet.test.jaxrs.services.resources.DoublePath1;
import org.restlet.test.jaxrs.services.resources.DoublePath2;
import org.restlet.test.jaxrs.services.resources.SimpleTrain;

/**
 * @author Stephan Koops
 */
@SuppressWarnings("all")
public class ExceptionMappersTest extends TestCase {

    private static final int INTERNAL_SERVER_ERROR = Status.INTERNAL_SERVER_ERROR
            .getStatusCode();

    private ExceptionMappers exceptionMappers;

    /**
     * @param exc
     * @return
     */
    private Response convert(Throwable exc) {
        InvocationTargetException ite = new InvocationTargetException(exc);
        return exceptionMappers.convert(ite);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.exceptionMappers = new ExceptionMappers();
        this.exceptionMappers.add(new IllegalArgExcMapper());
    }

    public void testIae() throws Exception {
        Response r = convert(new IllegalArgumentException());
        assertNotNull(r);
        assertEquals(IllegalArgExcMapper.STATUS, r.getStatus());
    }

    public void testIoe() throws Exception {
        Response r = convert(new IOException());
        assertNotNull(r);
        assertEquals(INTERNAL_SERVER_ERROR, r.getStatus());
    }

    public void testNfe() throws Exception {
        Response r = convert(new NumberFormatException());
        assertNotNull(r);
        assertEquals(IllegalArgExcMapper.STATUS, r.getStatus());
    }
}