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

import junit.framework.AssertionFailedError;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.AppPlusXmlResource;

/**
 * @author Stephan Koops
 * @see AppPlusXmlResource
 */
public class AppPlusXmlTest extends JaxRsTestCase {

    private static final MediaType APP_PERSON_XML = new MediaType(
            "application/Person+xml");

    private void getAndCheck(MediaType mt) {
        final Response response = get(mt);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final MediaType mediaType = response.getEntity().getMediaType();
        assertEqualMediaType(mt, mediaType);
    }

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(AppPlusXmlResource.class);
            }
        };
    }

    public void testGet() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final MediaType mediaType = response.getEntity().getMediaType();
        try {
            assertEqualMediaType(MediaType.TEXT_XML, mediaType);
        } catch (AssertionFailedError afe) {
            try {
                assertEqualMediaType(MediaType.APPLICATION_XML, mediaType);
            } catch (AssertionFailedError afe2) {
                assertEqualMediaType(APP_PERSON_XML, mediaType);
            }
        }
    }

    /**
     * @see AppPlusXmlResource#getPerson()
     */
    public void testGet2() throws Exception {
        getAndCheck(MediaType.TEXT_XML);
        getAndCheck(MediaType.APPLICATION_XML);
        getAndCheck(APP_PERSON_XML);
    }
}
