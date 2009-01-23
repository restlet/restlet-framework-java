/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.providers.TextCrazyPersonProvider;
import org.restlet.test.jaxrs.services.resources.OwnProviderTestService;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see TextCrazyPersonProvider
 * @see OwnProviderTestService
 */
public class OwnProviderTest extends JaxRsTestCase {

    @Override
    @SuppressWarnings("unchecked")
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            public Set<Object> getSingletons() {
                return (Set) TestUtils.createSet(new TextCrazyPersonProvider());
            }
    
            @Override
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(OwnProviderTestService.class);
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