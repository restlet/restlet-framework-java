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