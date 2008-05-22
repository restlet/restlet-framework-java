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

import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

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
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.resources.HttpHeaderTestService;

/**
 * @author Stephan Koops
 * @see HttpHeaderTestService
 */
@SuppressWarnings("all")
public class HttpHeaderTest extends JaxRsTestCase {

    public static void main(String[] args) throws Exception {
        new HttpHeaderTest().runServerUntilKeyPressed();
    }

    /**
     * @return
     */
    protected ApplicationConfig getAppConfig() {
        ApplicationConfig appConfig = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getProviderClasses() {
                return (Set) getProvClasses();
            }

            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getResourceClasses() {
                return (Set) Collections.singleton(getRootResourceClass());
            }

            @Override
            public Map<String, javax.ws.rs.core.MediaType> getMediaTypeMappings() {
                Map<String, javax.ws.rs.core.MediaType> mediaTypeMapping = new HashMap<String, javax.ws.rs.core.MediaType>();
                mediaTypeMapping.put("txt", TEXT_PLAIN_TYPE);
                mediaTypeMapping.put("html", TEXT_HTML_TYPE);
                mediaTypeMapping.put("xml", APPLICATION_XML_TYPE);
                return mediaTypeMapping;
            }
        };
        return appConfig;
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return HttpHeaderTestService.class;
    }

    public void testHeaderParam() throws IOException {
        Request request = createGetRequest("HeaderParam");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Restlet client = getClientConnector();
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("HeaderParam");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toLowerCase(), "abc");
        response = client.handle(request);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("HeaderParam");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toUpperCase(), "abc");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());
    }

    public void testAccMediaType() throws IOException {
        Response response = get("accMediaTypes", MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[" + MediaType.TEXT_PLAIN.toString() + "]", response
                .getEntity().getText());
    }

    public void testCookies() throws IOException {
        Request request = createGetRequest("cookies/cookieName");
        request.getCookies().add(new Cookie("cookieName", "cookie-value"));
        Restlet client = getClientConnector();
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
                HttpHeaderTestService.class, "language"));
        request.setOriginalRef(request.getResourceRef());
        request.setClientInfo(clientInfo);
        request.setEntity(new StringRepresentation("entity", Language.ENGLISH));
        Restlet client = getClientConnector();
        Response response = client.handle(request);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("en", response.getEntity().getText());
    }

    public void testHttpHeaders() throws IOException {
        Request request = createGetRequest("header/"
                + HttpHeaderTestService.TEST_HEADER_NAME);
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Restlet client = getClientConnector();
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("header/"
                + HttpHeaderTestService.TEST_HEADER_NAME);
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toLowerCase(), "abc");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("header/"
                + HttpHeaderTestService.TEST_HEADER_NAME);
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toUpperCase(), "abc");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());
    }

    public void testHttpHeadersCaseInsensitive() {
        Response response = get("header2");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    public void testWithDefault() throws Exception {
        Request request = createGetRequest("headerWithDefault");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Restlet client = getClientConnector();
        Response response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("headerWithDefault");
        response = client.handle(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("default", response.getEntity().getText());
    }
}