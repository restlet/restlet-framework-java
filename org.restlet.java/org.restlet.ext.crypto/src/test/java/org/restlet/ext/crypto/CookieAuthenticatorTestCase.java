/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.crypto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.restlet.*;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.security.MapVerifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the {@link CookieAuthenticator} class.
 * 
 * @author Jerome Louvel
 */
public class CookieAuthenticatorTestCase {

    public static class CookieGuardedApplication extends Application {

        @Override
        public Restlet createInboundRoot() {
            CookieAuthenticator co = new CookieAuthenticator(getContext(),
                    false, "My cookie realm", "MyExtraSecretKey".getBytes());

            MapVerifier mapVerifier = new MapVerifier();
            mapVerifier.getLocalSecrets().put("scott", "tiger".toCharArray());
            co.setVerifier(mapVerifier);

            Restlet hr = new Restlet() {

                @Override
                public void handle(Request request, Response response) {
                    response.setEntity("Hello, world!", MediaType.TEXT_PLAIN);
                }

            };

            co.setNext(hr);
            return co;
        }
    }

    @Test
    public void testCookieAuth1() {
        CookieGuardedApplication cga = new CookieGuardedApplication();
        Component c = new Component();
        c.getDefaultHost().attachDefault(cga);
        ClientResource cr = new ClientResource("http://toto.com/");
        cr.setNext(c);

        // 1) Attempt to connect without credentials
        try {
            cr.get();
            Assertions.fail("A resource exception should have been thrown");
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, re.getStatus());
        }

        // 2) Attempt to log in with wrong credentials
        ClientResource loginCr = cr.getChild("/login");
        Form loginForm = new Form();
        loginForm.add("login", "scott");
        loginForm.add("password", "titi");

        ResourceException exception = assertThrows(ResourceException.class,
                () -> loginCr.post(loginForm));
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, exception.getStatus());

        // 3) Login with right credentials
        loginForm.set("password", "tiger");
        loginCr.post(loginForm);
        assertEquals(Status.SUCCESS_OK, loginCr.getStatus());

        CookieSetting cs = loginCr.getCookieSettings().getFirst("Credentials");
        assertNotNull(cs, "No cookie credentials found");

        // 4) Retry connect with the right credentials
        cr.getCookies().add(cs.getName(), cs.getValue());
        assertEquals("Hello, world!", cr.get(String.class));

        // 5) Logout
        ClientResource logoutCr = cr.getChild("/logout");
        logoutCr.get();
        assertEquals(Status.SUCCESS_OK, logoutCr.getStatus());
        cs = logoutCr.getCookieSettings().getFirst("Credentials");
        assertEquals(0, cs.getMaxAge());
    }
}
