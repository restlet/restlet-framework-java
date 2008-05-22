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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.restlet.Restlet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Cookie;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.providers.CrazyTypeProvider;
import org.restlet.test.jaxrs.services.providers.MessageBodyWorkersTestProvider;
import org.restlet.test.jaxrs.services.resources.HttpHeaderTestService;
import org.restlet.test.jaxrs.services.resources.MessageBodyWorkersTestResource;
import org.restlet.test.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see MessageBodyWorkersTestProvider
 * @see MessageBodyWorkersTestResource
 */
@SuppressWarnings("all")
public class MessageBodyWorkersTest extends JaxRsTestCase {

    @Override
    protected Class<?> getRootResourceClass() {
        return MessageBodyWorkersTestResource.class;
    }

    @Override
    protected Set<Class<?>> getProvClasses() {
        return (Set)TestUtils.createSet(MessageBodyWorkersTestProvider.class,
                CrazyTypeProvider.class);
    }

    public void test1() throws IOException {
        Response response = get();
        Representation entity = response.getEntity();
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(new MediaType("text/crazy-person"), entity);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><firstname>Angela</firstname><lastname>Merkel</lastname></person>Angela Merkel is crazy.\nHeader value for name h1 is h1v", entity.getText());
    }
}