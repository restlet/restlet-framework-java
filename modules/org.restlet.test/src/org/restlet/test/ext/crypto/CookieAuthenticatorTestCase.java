/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.test.ext.crypto;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.crypto.CookieAuthenticator;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.security.MapVerifier;

/**
 * Unit test for the {@link CookieAuthenticator} class.
 * 
 * @author Jerome Louvel
 */
public class CookieAuthenticatorTestCase extends TestCase {

    public class CookieGuardedApplication extends Application {

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

    public void testCookieAuth1() {
        CookieGuardedApplication cga = new CookieGuardedApplication();
        Component c = new Component();
        c.getDefaultHost().attachDefault(cga);
        ClientResource cr = new ClientResource("http://toto.com/");
        cr.setNext(c);

        // 1) Attempt to connect without credentials
        try {
            cr.get();
            fail("A resource exception should have been thrown");
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, re.getStatus());
        }

        // 2) Attempt to login with wrong credentials
        ClientResource loginCr = cr.getChild("/login");
        Form loginForm = new Form();
        loginForm.add("login", "scott");
        loginForm.add("password", "titi");

        try {
            loginCr.post(loginForm);
            fail("A resource exception should have been thrown");
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, re.getStatus());
        }

        // 3) Login with right credentials
        loginForm.set("password", "tiger");
        loginCr.post(loginForm);
        assertEquals(Status.SUCCESS_OK, loginCr.getStatus());

        CookieSetting cs = loginCr.getCookieSettings().getFirst("Credentials");
        assertNotNull("No cookie credentials found", cs);

        // 4) Retry connect with right credentials
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
