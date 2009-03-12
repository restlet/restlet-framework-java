/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;
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
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Object> getSingletons() {
                return (Set) TestUtils.createSet(new ThrowWebAppExcProvider());
            }
    
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(SimpleResource.class);
            }
        };
        return appConfig;
    }

    public void testPost() {
        final Response response = post(new StringRepresentation("jgjhsdhbf"));
        sysOutEntityIfError(response);
        final int statusCode = response.getStatus().getCode();
        assertEquals(ThrowWebAppExcProvider.STATUS_READ, statusCode);
    }
}