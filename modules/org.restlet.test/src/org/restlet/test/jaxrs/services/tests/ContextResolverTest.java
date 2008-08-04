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

import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.providers.ContextResolverTestWriter;
import org.restlet.test.jaxrs.services.providers.TestContextResolver;
import org.restlet.test.jaxrs.services.resources.ContextResolverTestResource;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see ContextResolverTestResource
 */
public class ContextResolverTest extends JaxRsTestCase {

    @Override
    @SuppressWarnings("all")
    public Set<Class<?>> getProvClasses() {
        return TestUtils.createSet(ContextResolverTestWriter.class,
                TestContextResolver.class);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return ContextResolverTestResource.class;
    }

    /**
     * @throws Exception
     * @see ContextResolverTestResource#getHomeUri()
     * @see TestContextResolver
     */
    public void test1() throws Exception {
        final Response response = get(MediaType.TEXT_HTML);
        final String entity = response.getEntity().getText();
        System.out.println(entity);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_HTML, response.getEntity()
                .getMediaType());
        assertEquals(
                "<html><head></head><body>\nThe virtual presence of <b>Helmut Kohl</b> is: <a href=\"http://www.restlet.org/persons/Kohl/Helmut\">http://www.restlet.org/persons/Kohl/Helmut</a></html>",
                entity);
    }
}