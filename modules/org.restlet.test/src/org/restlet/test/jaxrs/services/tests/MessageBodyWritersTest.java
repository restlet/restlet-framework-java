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

import java.io.IOException;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.providers.CrazyTypeProvider;
import org.restlet.test.jaxrs.services.providers.ProidersTestProvider;
import org.restlet.test.jaxrs.services.resources.MessageBodyWriterTestResource;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see ProidersTestProvider
 * @see MessageBodyWriterTestResource
 */
@SuppressWarnings("all")
public class MessageBodyWritersTest extends JaxRsTestCase {

    @Override
    protected Set<Class<?>> getProvClasses() {
        return (Set) TestUtils.createSet(ProidersTestProvider.class,
                CrazyTypeProvider.class);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return MessageBodyWriterTestResource.class;
    }

    public void test1() throws IOException {
        final Response response = get();
        final Representation entity = response.getEntity();
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(new MediaType("text/crazy-person"), entity);
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><firstname>Angela</firstname><lastname>Merkel</lastname></person>Angela Merkel is crazy.\nHeader value for name h1 is h1v",
                entity.getText());
    }
}