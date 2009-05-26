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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.InheritAnnotationTestService1;
import org.restlet.test.jaxrs.services.resources.InheritAnnotationTestService2;
import org.restlet.test.jaxrs.services.resources.InheritAnnotationTestServiceInterface;

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