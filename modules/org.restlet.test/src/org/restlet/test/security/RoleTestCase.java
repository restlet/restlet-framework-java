/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.security;

import org.restlet.Application;
import org.restlet.security.Role;
import org.restlet.test.RestletTestCase;

/**
 * Suite of unit tests for the {@link Role} class.
 * 
 * @author Thierry Boileau
 * @author Jerome Louvel
 */
public class RoleTestCase extends RestletTestCase {

    public void testRoleEquality() {
        Application app1 = new Application();
        Application app2 = new Application();

        Role role1 = new Role(app1, "role", "one description");
        Role role2 = new Role(app1, "role", "another description");
        Role role3 = new Role(app1, "role", null);

        assertEquals(role1, role2);
        assertEquals(role1, role3);
        assertEquals(role2, role3);

        Role role4 = new Role(app2, "role", "one description");
        assertFalse(role1.equals(role4));

        Role role10 = new Role(app1, "role10", "");
        Role role11 = new Role(app1, "role11", "");

        role1.getChildRoles().add(role10);
        role1.getChildRoles().add(role11);
        assertFalse(role1.equals(role2));

        role2.getChildRoles().add(role11);
        role2.getChildRoles().add(role10);
        assertFalse(role1.equals(role2));

        role2.getChildRoles().clear();
        role2.getChildRoles().add(role10);
        role2.getChildRoles().add(role11);
        assertEquals(role1, role2);
    }
}
