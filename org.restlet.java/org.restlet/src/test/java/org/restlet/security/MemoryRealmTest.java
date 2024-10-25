/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MemoryRealmTest {

	@Test
	public void whenUmappingAGroupAndRoleFromAMemoryRealmThenMappingIsDropped() {
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
	public void whenUmappingAUserAndRoleFromAMemoryRealmThenMappingIsDropped() {
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
