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
import java.util.List;

import org.restlet.Client;
import org.restlet.data.ClientInfo;
import org.restlet.data.Cookie;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.util.Util;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.HttpHeaderTestService;

/**
 * This test class checks if the Request.evaluatePreconditions methods works
 * fine.
 * 
 * @author Stephan Koops
 */
public class HttpHeaderTest extends JaxRsTestCase {

    private static final Class<HttpHeaderTestService> SERVICE_CLASS = HttpHeaderTestService.class;

    @Override
    protected Class<?> getRootResourceClass() {
        return SERVICE_CLASS;
    }

    public void testHeaderParam() throws IOException {
        Request request = new Request(Method.GET, createReference(
                SERVICE_CLASS, "HeaderParam"));
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Client client = createClient();
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = new Request(Method.GET, createReference(SERVICE_CLASS,
                "HeaderParam"));
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toLowerCase(), "abc");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = new Request(Method.GET, createReference(SERVICE_CLASS,
                "HeaderParam"));
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toUpperCase(), "abc");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());
    }

    public void testAccMediaType() throws IOException {
        Response response = accessServer(Method.GET, SERVICE_CLASS,
                "accMediaTypes", MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("["+MediaType.TEXT_PLAIN.toString()+"]", response.getEntity()
                .getText());
    }

    public void testCookies() throws IOException {
        Request request = new Request(Method.GET, createReference(
                SERVICE_CLASS, "cookies/cookieName"));
        request.getCookies().add(new Cookie("cookieName", "cookie-value"));
        Client client = createClient();
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("cookieName=cookie-value", response.getEntity().getText());
    }

    public void testLanguage() throws IOException {
        List<Preference<Language>> acceptedLanguages = new ArrayList<Preference<Language>>();
        acceptedLanguages.add(new Preference<Language>(Language.ENGLISH));
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setAcceptedLanguages(acceptedLanguages);

        Request request = new Request(Method.POST, createReference(
                SERVICE_CLASS, "language"));
        request.setClientInfo(clientInfo);
        request.setEntity(new StringRepresentation("entity", Language.ENGLISH));
        Client client = createClient();
        Response response = client.handle(request);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("en", response.getEntity().getText());
    }

    public void testHttpHeaders() throws IOException {
        Request request = new Request(Method.GET, createReference(
                SERVICE_CLASS, "header/"
                        + HttpHeaderTestService.TEST_HEADER_NAME));
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Client client = createClient();
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = new Request(Method.GET, createReference(SERVICE_CLASS,
                "header/" + HttpHeaderTestService.TEST_HEADER_NAME));
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toLowerCase(), "abc");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = new Request(Method.GET, createReference(SERVICE_CLASS,
                "header/" + HttpHeaderTestService.TEST_HEADER_NAME));
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toUpperCase(), "abc");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());
    }
}