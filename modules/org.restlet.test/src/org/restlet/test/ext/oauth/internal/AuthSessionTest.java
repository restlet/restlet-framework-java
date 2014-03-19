/**
 * Copyright 2005-2014 Restlet
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
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.test.ext.oauth.internal;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.ext.oauth.ResponseType;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.AuthSessionTimeoutException;
import org.restlet.ext.oauth.internal.RedirectionURI;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class AuthSessionTest {

    private AuthSession session;

    public AuthSessionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        session = AuthSession.newAuthSession();
        session.setClientId("foobar");
        session.setAuthFlow(ResponseType.code);
        session.setGrantedScope(new String[] { "baz" });
        session.setRequestedScope(new String[] { "foo", "baz" });
        session.setRedirectionURI(new RedirectionURI("http://example.com/cb"));
        session.setScopeOwner("me");
        session.setSessionTimeout(600);
        session.setState("xyz");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of toMap method, of class AuthSession.
     */
    @Test
    public void testToMap() {
        Map<String, Object> map = session.toMap();
        isNormalized(map);
    }

    @Test
    public void testToAuthSession() {
        Map<String, Object> map = session.toMap();
        AuthSession s = AuthSession.toAuthSession(map);
        assertEquals(session, s);
    }

    /**
     * Test of updateActivity method, of class AuthSession.
     */
    @Test(expected = AuthSessionTimeoutException.class)
    public void testUpdateActivity() throws Exception {
        session.setSessionTimeout(1);
        Thread.sleep(1000);
        session.updateActivity();
    }

    @SuppressWarnings("unchecked")
    private static void isNormalized(Object val) {
        assertThat(
                val,
                anyOf(instanceOf(String.class), instanceOf(Number.class),
                        instanceOf(Boolean.class), instanceOf(Date.class),
                        instanceOf(Map.class), instanceOf(List.class),
                        instanceOf(byte[].class)));

        if (val instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) val;

            for (Object key : map.keySet()) {
                isNormalized(map.get(key));
            }
        } else if (val instanceof List) {
            List<?> list = (List<?>) val;

            for (Object elem : list) {
                isNormalized(elem);
            }
        }
    }
}
