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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.test.jaxrs.services.providers.TestContextResolver;
import org.restlet.test.jaxrs.services.providers.ThreadLocalContextTestExcMapper;
import org.restlet.test.jaxrs.services.resources.ContextResolverTestResource;
import org.restlet.test.jaxrs.services.resources.ThreadLocalContextTestResource;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see ContextResolverTestResource
 */
public class ThreadLocalContextTest extends JaxRsTestCase {

    @Override
    @SuppressWarnings("all")
    public Set<Class<?>> getProvClasses() {
        return (Set) TestUtils.createSet(ThreadLocalContextTestExcMapper.class);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return ThreadLocalContextTestResource.class;
    }

    /**
     * This test first 
     * @see ContextResolverTestResource#getHomeUri()
     * @see TestContextResolver
     */
    public void test1() throws Exception {
        // LATER this test sometimes blocks
        final List<Response> c = new ArrayList<Response>();
        new Thread() {
            @Override
            public void run() {
                Response response = get(MediaType.TEXT_PLAIN);
                c.add(response);
            }
        }.start();
        Response response = get(MediaType.TEXT_HTML);
        assertEqualMediaType(MediaType.TEXT_HTML, response);

        while (c.isEmpty())
            TestUtils.sleep();

        response = c.get(0);
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
    }
}