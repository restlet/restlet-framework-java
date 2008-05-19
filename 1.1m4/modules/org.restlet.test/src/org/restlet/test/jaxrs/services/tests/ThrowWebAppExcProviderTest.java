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
package org.restlet.test.jaxrs.services.tests;

import java.util.Set;

import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.providers.ThrowWebAppExcProvider;
import org.restlet.test.jaxrs.services.resources.SimpleResource;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see ThrowWebAppExcProvider
 * @see SimpleResource
 */
public class ThrowWebAppExcProviderTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return SimpleResource.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<Class<?>> getProvClasses() {
        return (Set) TestUtils.createSet(ThrowWebAppExcProvider.class);
    }

    public void testPost() {
        Response response = post(new StringRepresentation("jgjhsdhbf"));
        sysOutEntityIfError(response);
        int statusCode = response.getStatus().getCode();
        assertEquals(ThrowWebAppExcProvider.STATUS_READ, statusCode);
    }
}