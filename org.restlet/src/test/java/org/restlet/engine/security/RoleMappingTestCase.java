/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.security.Role;
import org.restlet.security.User;

class RoleMappingTestCase {

    @Test
    void defaultConstructor_hasNullSourceAndTarget() {
        RoleMapping mapping = new RoleMapping();
        assertNull(mapping.getSource());
        assertNull(mapping.getTarget());
    }

    @Test
    void constructor_setsSourceAndTarget() {
        User user = new User("alice");
        Role role = new Role(null, "admin", "Administrator role");
        RoleMapping mapping = new RoleMapping(user, role);
        assertEquals(user, mapping.getSource());
        assertEquals(role, mapping.getTarget());
    }

    @Test
    void settersUpdateState() {
        RoleMapping mapping = new RoleMapping();
        User user = new User("bob");
        Role role = new Role(null, "editor", "Editor role");
        mapping.setSource(user);
        mapping.setTarget(role);
        assertEquals(user, mapping.getSource());
        assertEquals(role, mapping.getTarget());
    }
}
