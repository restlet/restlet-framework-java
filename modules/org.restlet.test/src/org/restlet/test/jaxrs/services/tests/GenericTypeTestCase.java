/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.providers.GenericTypeMBW;
import org.restlet.test.jaxrs.services.resources.GenericTypeResource;

/**
 * @author Stephan Koops
 * @see GenericTypeResource
 * @see GenericTypeMBW
 */
public class GenericTypeTestCase extends JaxRsTestCase {
    // LATER add to AllServicesTests

    @Override
    @SuppressWarnings("unchecked")
    protected Set<Class<?>> getProvClasses() {
        return (Set) Collections.singleton(GenericTypeMBW.class);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return GenericTypeResource.class;
    }

    public void testGet() throws IOException {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc\ndef\n", response.getEntity().getText());
    }
}