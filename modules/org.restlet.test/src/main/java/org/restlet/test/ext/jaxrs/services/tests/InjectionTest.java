/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.jaxrs.services.tests;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.InjectionTestService;
import org.restlet.test.ext.jaxrs.services.resources.InjectionTestService2;
import org.restlet.test.ext.jaxrs.util.OrderedReadonlySet;

/**
 * @author Stephan Koops
 * @see InjectionTestService
 * @see InjectionTestService2
 */
public class InjectionTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return new OrderedReadonlySet<Class<?>>(
                        InjectionTestService.class, InjectionTestService2.class);
            }
        };
        return appConfig;
    }

    public void testGet() {
        final Response response = get("?qp1=56");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /** @see InjectionTestService2#get() */
    public void testGetWithIndex() throws IOException {
        Response response = get("two/56");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("56", response.getEntity().getText());

        response = get("two/97");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("97", response.getEntity().getText());
    }
}
