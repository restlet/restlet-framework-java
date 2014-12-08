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
import org.restlet.data.Dimension;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.ResponseBuilderService;

/**
 * @author Stephan Koops
 * @see ResponseBuilderService
 * @see javax.ws.rs.core.Response.ResponseBuilder
 */
public class ResponseBuilderTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(ResponseBuilderService.class);
            }
        };
        return appConfig;
    }

    public void test1() {
        final Response response = get("1");
        final Set<Dimension> dimensions = response.getDimensions();

        assertTrue("dimension must contain MediaType",
                dimensions.contains(Dimension.MEDIA_TYPE));
        assertTrue("dimension must contain Encoding",
                dimensions.contains(Dimension.ENCODING));
    }

    public void test2() {
        final Response response = get("2");
        final Set<Dimension> dimensions = response.getDimensions();

        assertTrue("dimension must contain Language",
                dimensions.contains(Dimension.LANGUAGE));
        assertTrue("dimension must contain CharacterSet",
                dimensions.contains(Dimension.CHARACTER_SET));
    }

    public void testDelete() {
        final Response r = accessServer(Method.DELETE);

        assertEquals(Status.SUCCESS_OK, r.getStatus());
    }
}
