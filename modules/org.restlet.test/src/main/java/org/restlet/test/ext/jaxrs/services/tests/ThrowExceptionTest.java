/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.ext.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.test.ext.jaxrs.services.providers.SqlExceptionMapper;
import org.restlet.test.ext.jaxrs.services.resources.ThrowExceptionResource;

/**
 * @author Stephan Koops
 * @see ThrowExceptionResource
 */
public class ThrowExceptionTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(ThrowExceptionResource.class);
            }

            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Object> getSingletons() {
                return (Set) Util.createSet(new SqlExceptionMapper());
            }
        };
        return appConfig;
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
