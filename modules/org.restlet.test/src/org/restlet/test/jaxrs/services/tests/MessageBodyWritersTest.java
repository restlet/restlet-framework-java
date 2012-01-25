/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.jaxrs.services.tests;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.test.jaxrs.services.providers.AppCrazyPersonProvider;
import org.restlet.test.jaxrs.services.providers.TextCrazyPersonProvider;
import org.restlet.test.jaxrs.services.resources.MessageBodyWriterTestResource;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see MessageBodyWriterTestResource
 * @see AppCrazyPersonProvider
 * @see TextCrazyPersonProvider
 */
@SuppressWarnings("all")
public class MessageBodyWritersTest extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(MessageBodyWriterTestResource.class);
            }

            @Override
            public Set<Object> getSingletons() {
                return (Set) TestUtils.createSet(new AppCrazyPersonProvider(),
                        new TextCrazyPersonProvider());
            }
        };
    }

    /** @see MessageBodyWriterTestResource#get() */
    public void test1() throws IOException {
        final Response response = get();
        final Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(new MediaType("application/crazy-person"), entity);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><firstname>Angela</firstname><lastname>Merkel</lastname></person>Angela Merkel is crazy.\nHeader value for name h1 is h1v",
                entity.getText());
    }
}
