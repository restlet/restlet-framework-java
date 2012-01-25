/**
 * Copyright 2005-2012 Restlet S.A.S.
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

package org.restlet.test.security;

import org.restlet.security.Role;
import org.restlet.test.RestletTestCase;

/**
 * Suite of unit tests for the {@link Role} class.
 * 
 * @author Thierry Boileau
 */
public class RoleTestCase extends RestletTestCase {

    public void testRoleEquality() {
        Role role1 = new Role("role", "");
        Role role2 = new Role("role", "");
        assertEquals(role1, role2);

        Role role3 = new Role("role3", "");
        Role role4 = new Role("role4", "");

        role1.getChildRoles().add(role3);
        role1.getChildRoles().add(role4);
        assertNotSame(role1, role2);

        role2.getChildRoles().add(role4);
        role2.getChildRoles().add(role3);
        assertNotSame(role1, role2);

        role2.getChildRoles().clear();
        role2.getChildRoles().add(role3);
        role2.getChildRoles().add(role4);
        assertEquals(role1, role2);
    }
}
