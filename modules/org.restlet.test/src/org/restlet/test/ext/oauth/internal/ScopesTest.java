/**
 * Copyright 2005-2013 Restlet S.A.S.
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
package org.restlet.test.ext.oauth.internal;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.security.Role;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class ScopesTest {

    /**
     * Test of toScope method, of class Scopes.
     */
    @Test
    public void testToScope_List() {
        List<Role> roles = new ArrayList<Role>();
        roles.add(new Role(null, "foo", null));
        roles.add(new Role(null, "bar", null));
        roles.add(new Role(null, "baz", null));
        String result = Scopes.toScope(roles);
        assertThat(
                result,
                anyOf(equalTo("foo bar baz"), equalTo("foo baz bar"),
                        equalTo("bar foo baz"), equalTo("bar baz foo"),
                        equalTo("baz foo bar"), equalTo("baz bar foo")));
    }

    /**
     * Test of toString method, of class Scopes.
     */
    @Test
    public void testToString() {
        String[] scopes = new String[] { "foo", "bar", "baz" };
        String expResult = "foo bar baz";
        String result = Scopes.toString(scopes);
        assertEquals(expResult, result);
    }

    /**
     * Test of toScope method, of class Scopes.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToScope_Role() {
        assertEquals("foo", Scopes.toScope(new Role(null, "foo", null)));

        Scopes.toScope(new Role(null, "foo bar", null));
    }

    /**
     * Test of toRole method, of class Scopes.
     */
    @Test
    public void testToRole() {
        String scope = "foo";
        Role result = Scopes.toRole(scope);
        assertTrue(result.getName().equals("foo"));
    }

    /**
     * Test of toRoles method, of class Scopes.
     */
    @Test
    public void testToRoles() {
        String scopes = "foo bar baz";
        String[] expResult = new String[] { "foo", "bar", "baz" };
        List<Role> result = Scopes.toRoles(scopes);
        for (int i = 0; i < 3; i++) {
            assertEquals(expResult[i], result.get(i).getName());
        }
    }

    /**
     * Test of parseScope method, of class Scopes.
     */
    @Test
    public void testParseScope_String() {
        String scopes = "foo bar baz";
        String[] expResult = new String[] { "foo", "bar", "baz" };
        String[] result = Scopes.parseScope(scopes);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of parseScope method, of class Scopes.
     */
    @Test
    public void testParseScope_List() {
        List<Role> roles = new ArrayList<Role>();
        roles.add(new Role(null, "foo", null));
        roles.add(new Role(null, "bar", null));
        roles.add(new Role(null, "baz", null));
        String[] expResult = new String[] { "foo", "bar", "baz" };
        String[] result = Scopes.parseScope(roles);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of isIdentical method, of class Scopes.
     */
    @Test
    public void testIsIdentical() {
        String[] a = new String[] { "foo", "bar", "baz" };
        String[] b = new String[] { "bar", "baz", "foo" };
        assertTrue(Scopes.isIdentical(a, b));
    }
}
