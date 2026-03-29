/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MemoryRealmTest {

    @Test
    void whenUnmappingAGroupAndRoleFromAMemoryRealmThenMappingIsDropped() {
        // given a Memory Realm, a Group and a Role
        MemoryRealm memoryRealm = new MemoryRealm();
        Group group = new Group();
        Role role = new Role();

        // Given the group and role are mapped
        memoryRealm.map(group, role);
        // Then there is a mapping for this group
        assertFalse(memoryRealm.findRoles(group).isEmpty());

        // When I remove this mapping
        memoryRealm.unmap(group, role);

        // Then the memory realm has no more mapping
        assertTrue(memoryRealm.findRoles(group).isEmpty());
    }

    @Test
    void whenUnmappingAUserAndRoleFromAMemoryRealmThenMappingIsDropped() {
        // given a Memory Realm, a Group and a Role
        MemoryRealm memoryRealm = new MemoryRealm();
        User user = new User();
        Role role = new Role();

        // Given the user and role are mapped
        memoryRealm.map(user, role);
        // Then there is a mapping for this user
        assertFalse(memoryRealm.findRoles(user).isEmpty());

        // When I remove this mapping
        memoryRealm.unmap(user, role);

        // Then the memory realm has no more mapping
        assertTrue(memoryRealm.findRoles(user).isEmpty());
    }
}
