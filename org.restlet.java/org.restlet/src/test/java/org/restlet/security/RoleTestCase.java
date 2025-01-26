/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import org.junit.jupiter.api.Test;
import org.restlet.Application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Suite of unit tests for the {@link Role} class.
 *
 * @author Thierry Boileau
 * @author Jerome Louvel
 */
public class RoleTestCase {

    @Test
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
        assertNotEquals(role1, role4);

        Role role10 = new Role(app1, "role10", "");
        Role role11 = new Role(app1, "role11", "");

        role1.getChildRoles().add(role10);
        role1.getChildRoles().add(role11);
        assertNotEquals(role1, role2);

        role2.getChildRoles().add(role11);
        role2.getChildRoles().add(role10);
        assertNotEquals(role1, role2);

        role2.getChildRoles().clear();
        role2.getChildRoles().add(role10);
        role2.getChildRoles().add(role11);
        assertEquals(role1, role2);
    }
}
