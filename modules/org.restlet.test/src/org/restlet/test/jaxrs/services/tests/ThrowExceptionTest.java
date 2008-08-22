/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
    protected Set<Object> getSingletons() {
        return (Set) Util.createSet(new SqlExceptionMapper());
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