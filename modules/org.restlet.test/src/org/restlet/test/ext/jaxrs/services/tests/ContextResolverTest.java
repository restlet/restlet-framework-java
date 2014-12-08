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

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.providers.ContextResolverTestWriter;
import org.restlet.test.ext.jaxrs.services.providers.TestContextResolver;
import org.restlet.test.ext.jaxrs.services.resources.ContextResolverTestResource;
import org.restlet.test.ext.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see ContextResolverTestResource
 */
public class ContextResolverTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(ContextResolverTestResource.class);
            }

            @Override
            public Set<Object> getSingletons() {
                return TestUtils.createSet(new ContextResolverTestWriter(),
                        new TestContextResolver());
            }
        };
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
                "<html><head></head><body>\nThe virtual presence of <b>Helmut Kohl</b> is: <a href=\"http://restlet.org/persons/Kohl/Helmut\">http://restlet.org/persons/Kohl/Helmut</a></html>",
                entity);
    }
}
