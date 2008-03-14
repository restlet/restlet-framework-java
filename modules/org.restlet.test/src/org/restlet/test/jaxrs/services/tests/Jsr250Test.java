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

import junit.framework.TestCase;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.others.Jsr250TestProvider;
import org.restlet.test.jaxrs.services.resources.Jsr250TestService;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see Jsr250TestService
 * @see Jsr250TestProvider
 */
public class Jsr250Test extends TestCase {

    @SuppressWarnings("unchecked")
    protected ApplicationConfig getAppConfig() {
        return new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                return (Set) TestUtils.createSet(Jsr250TestService.class);
            }

            @Override
            public Set<Class<?>> getProviderClasses() {
                return (Set) TestUtils.createSet(Jsr250TestProvider.class);
            }
        };
    }

    public void test1() throws Exception {
        JaxRsRouter jaxRsRouter = new JaxRsRouter(null, getAppConfig());
        jaxRsRouter.start();
        assertTrue(Jsr250TestProvider.initiated);
        
        Reference ref = new Reference(new Reference("http://localhost"), "http://localhost/jsr250test");
        Request request = new Request(Method.GET, ref);
        Response response = jaxRsRouter.handle(request);
        JaxRsTestCase.sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        String expected = "value: true\nMessageBodyWriter initiaied: true";
        assertEquals(expected, entity.getText());
        
        assertNotNull(Jsr250TestService.LastDestroyed);
        
        jaxRsRouter.stop();
        
        assertTrue(Jsr250TestProvider.destroyed);
    }
}