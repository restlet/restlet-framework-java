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
import org.restlet.test.ext.jaxrs.services.providers.TextCrazyPersonProvider;
import org.restlet.test.ext.jaxrs.services.resources.OwnProviderTestService;
import org.restlet.test.ext.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see TextCrazyPersonProvider
 * @see OwnProviderTestService
 */
public class OwnProviderTest extends JaxRsTestCase {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            public Set<Object> getSingletons() {
                return (Set) TestUtils.createSet(new TextCrazyPersonProvider());
            }

            @Override
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(OwnProviderTestService.class);
            }
        };
        return appConfig;
    }

    /**
     * @see OwnProviderTestService#get()
     */
    public void test1() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(new MediaType("text/crazy-person"), response);
        final String actualEntity = response.getEntity().getText();
        final String expectedEntity = "abc def is crazy.\nHeader value for name h1 is h1v";
        assertEquals(expectedEntity, actualEntity);
    }
}
