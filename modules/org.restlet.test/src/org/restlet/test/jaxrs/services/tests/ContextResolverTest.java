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

import javax.ws.rs.core.ApplicationConfig;

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
    @SuppressWarnings("unchecked")
    protected ApplicationConfig getAppConfig() {
        return new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                return (Set) TestUtils.createSet(getRootResourceClass());
            }

            @Override
            public Set<Class<?>> getProviderClasses() {
                return TestUtils.createSet(ContextResolverTestWriter.class,
                        TestContextResolver.class);
            }
        };
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
        Response response = get(MediaType.TEXT_HTML);
        System.out.println(response.getEntity().getText());
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
        assertEquals(
                "<html><head></head><body>\nThe virtual presence of <b>Helmut Kohl</b> is: <a href=\"http://www.restlet.org/persons/Kohl/Helmut\">http://www.restlet.org/persons/Kohl/Helmut</a></html>",
                response.getEntity().getText());
    }
}