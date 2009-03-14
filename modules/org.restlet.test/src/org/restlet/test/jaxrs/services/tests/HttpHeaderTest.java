/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Application;

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
import org.restlet.representation.StringRepresentation;
import org.restlet.test.jaxrs.services.resources.HttpHeaderTestService;

/**
 * @author Stephan Koops
 * @see HttpHeaderTestService
 */
public class HttpHeaderTest extends JaxRsTestCase {

    public static void main(String[] args) throws Exception {
        new HttpHeaderTest().runServerUntilKeyPressed();
    }

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(HttpHeaderTestService.class);
            }
        };
    }

    public void testAccMediaType() throws IOException {
        Response response = get("accMediaTypes", MediaType.TEXT_PLAIN);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[" + MediaType.TEXT_PLAIN.toString() + "]", response
                .getEntity().getText());

        ClientInfo clientInfo = new ClientInfo();
        clientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_PLAIN, 0.5f));
        clientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_HTML, 0.8f));
        clientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_XML, 0.2f));
        response = get("accMediaTypes", clientInfo);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[" + MediaType.TEXT_HTML.toString() + ", "
                + MediaType.TEXT_PLAIN.toString() + ", "
                + MediaType.TEXT_XML.toString() + "]", response.getEntity()
                .getText());
    }

    public void testCookies() throws IOException {
        final Request request = createGetRequest("cookies/cookieName");
        request.getCookies().add(new Cookie("cookieName", "cookie-value"));
        final Response response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("cookieName=cookie-value", response.getEntity().getText());
    }

    public void testHeaderParam() throws IOException {
        Request request = createGetRequest("HeaderParam");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Response response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("HeaderParam");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toLowerCase(), "abc");
        response = accessServer(request);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("HeaderParam");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toUpperCase(), "abc");
        response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());
    }

    public void testHttpHeaders() throws IOException {
        Request request = createGetRequest("header/"
                + HttpHeaderTestService.TEST_HEADER_NAME);
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Response response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("header/"
                + HttpHeaderTestService.TEST_HEADER_NAME);
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toLowerCase(), "abc");
        response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("header/"
                + HttpHeaderTestService.TEST_HEADER_NAME);
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME.toUpperCase(), "abc");
        response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());
    }

    public void testHttpHeadersCaseInsensitive() {
        final Response response = get("header2");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    /**
     * @see HttpHeaderTestService#getLanguage(javax.ws.rs.core.HttpHeaders)
     */
    public void testLanguage() throws IOException {
        final List<Preference<Language>> acceptedLanguages = new ArrayList<Preference<Language>>();
        acceptedLanguages.add(new Preference<Language>(Language.ENGLISH));
        final ClientInfo clientInfo = new ClientInfo();
        clientInfo.setAcceptedLanguages(acceptedLanguages);

        final Request request = new Request(Method.POST, createReference(
                HttpHeaderTestService.class, "language"));
        request.setClientInfo(clientInfo);
        request.setEntity(new StringRepresentation("entity", Language.ENGLISH));
        final Response response = accessServer(request);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("en", response.getEntity().getText());
    }

    public void testWithDefault() throws Exception {
        Request request = createGetRequest("headerWithDefault");
        Util.getHttpHeaders(request).add(
                HttpHeaderTestService.TEST_HEADER_NAME, "abc");
        Response response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", response.getEntity().getText());

        request = createGetRequest("headerWithDefault");
        response = accessServer(request);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("default", response.getEntity().getText());
    }
}