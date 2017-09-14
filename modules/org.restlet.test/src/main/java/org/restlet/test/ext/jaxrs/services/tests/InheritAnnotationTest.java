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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.InheritAnnotationTestService1;
import org.restlet.test.ext.jaxrs.services.resources.InheritAnnotationTestService2;
import org.restlet.test.ext.jaxrs.services.resources.InheritAnnotationTestServiceInterface;

/**
 * Check, if the inheritation of method annotations works corerct.
 * 
 * @author Stephan Koops
 * @see InheritAnnotationTestService1
 * @see InheritAnnotationTestService2
 * @see InheritAnnotationTestServiceInterface
 */
public class InheritAnnotationTest extends JaxRsTestCase {

    private static final Class<InheritAnnotationTestService1> SERVICE_1 = InheritAnnotationTestService1.class;

    private static final Class<InheritAnnotationTestService2> SERVICE_2 = InheritAnnotationTestService2.class;

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                final Set<Class<?>> rrcs = new HashSet<Class<?>>(2);
                rrcs.add(SERVICE_1);
                rrcs.add(SERVICE_2);
                return rrcs;
            }
        };
    }

    public void test1() throws Exception {
        final Reference reference = createReference(SERVICE_1, "getText");
        final Response response = accessServer(Method.GET, reference);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
        final String entityText = response.getEntity().getText();
        assertEquals(InheritAnnotationTestService1.RETURN_STRING, entityText);
    }

    public void test2a() throws Exception {
        final Reference reference = createReference(SERVICE_2, "getText");
        final Response response = accessServer(Method.GET, reference);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
        final String entityText = response.getEntity().getText();
        assertEquals(InheritAnnotationTestService2.RETURN_STRING, entityText);
    }

    public void test2b() throws Exception {
        final Reference reference = createReference(SERVICE_2,
                "getSubClassText");
        final Response response = accessServer(Method.GET, reference);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
        final String entityText = response.getEntity().getText();
        assertEquals(InheritAnnotationTestService2.RETURN_STRING_SUB,
                entityText);
    }

    public void x_test2c() throws Exception {
        final Reference reference = createReference(SERVICE_2,
                "getSubClassText/sub");
        final Response response = accessServer(Method.GET, reference);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
        final String entityText = response.getEntity().getText();
        assertEquals(InheritAnnotationTestService2.RETURN_STRING_SUB2,
                entityText);
    }
}
