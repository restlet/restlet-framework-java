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
     * @see ContextResolverTestResource#getHomeUri()
     * @see TestContextResolver
     * @throws Exception
     */
    public void test1() throws Exception {
        final List<Response> c = new ArrayList<Response>();
        new Thread() {
            @Override
            public void run() {
                final Response response = get(MediaType.TEXT_PLAIN);
                c.add(response);
            }
        }.start();
        Response response = get(MediaType.TEXT_HTML);
        assertEqualMediaType(MediaType.TEXT_HTML, response);

        while (c.isEmpty()) {
            TestUtils.sleep();
        }

        response = c.get(0);
        assertEqualMediaType(MediaType.TEXT_PLAIN, response);
    }
}