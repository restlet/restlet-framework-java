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

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.providers.CrazyTypeProvider;
import org.restlet.test.jaxrs.services.resources.OwnProviderTestService;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see CrazyTypeProvider
 * @see OwnProviderTestService
 */
public class OwnProviderTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return OwnProviderTestService.class;
    }

    @Override
    @SuppressWarnings("all")
    public Set<Class<?>> getProvClasses() {
        return (Set)TestUtils.createSet(CrazyTypeProvider.class);
    }

    /**
     * @see OwnProviderTestService#get()
     */
    public void test1() throws Exception {
        Response response = get();
        super.sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        MediaType respMediaType = response.getEntity().getMediaType();
        assertEquals(new MediaType("application/crazyType"), respMediaType);
        String actualEntity = response.getEntity().getText();
        String expectedEntity = "abc def is crazy.\nHeader value for name h1 is h1v\ncontentType is application/crazyType\ncontentType List contains application/crazyType";
        assertEquals(expectedEntity, actualEntity);
    }
}