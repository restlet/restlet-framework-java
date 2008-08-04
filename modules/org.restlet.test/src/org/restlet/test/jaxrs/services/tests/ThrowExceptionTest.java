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
package org.restlet.test.jaxrs.services.tests;

import java.util.Set;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.test.jaxrs.services.providers.SqlExceptionMapper;
import org.restlet.test.jaxrs.services.resources.ThrowExceptionResource;

/**
 * @author Stephan Koops
 * @see ThrowExceptionResource
 */
public class ThrowExceptionTest extends JaxRsTestCase {

    @SuppressWarnings("unchecked")
    @Override
    protected Set<Class<?>> getProvClasses() {
        return (Set) Util.createSet(SqlExceptionMapper.class);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return ThrowExceptionResource.class;
    }

    /**
     * @throws Exception
     * @see ThrowExceptionResource
     */
    public void testIoe() {
        final Response response = get("IOException");
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }

    public void testSqlExc() throws Exception {
        final Response response = get("sqlExc");
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
        assertEquals(SqlExceptionMapper.MESSAGE, response.getEntity().getText());
    }

    /**
     * @see ThrowExceptionResource#getWebAppExc()
     */
    public void testWebAppExc() {
        final Response response = get("WebAppExc");
        final int actStatus = response.getStatus().getCode();
        assertEquals(ThrowExceptionResource.WEB_APP_EXC_STATUS, actStatus);
    }

    public void testWebAppExcNullStatus() {
        final Response response = get("WebAppExcNullStatus");
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }
}