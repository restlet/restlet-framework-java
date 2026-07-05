/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;

/** Unit tests for {@link RoleAuthorizer}. */
class RoleAuthorizerTestCase {

    private Request requestWithRoles(Role... roles) {
        Request request = new Request();
        request.getClientInfo().getRoles().addAll(List.of(roles));
        return request;
    }

    @Test
    void defaultConstructor_hasEmptyRoleListsAndNullIdentifier() {
        RoleAuthorizer authorizer = new RoleAuthorizer();

        assertNull(authorizer.getIdentifier());
        assertTrue(authorizer.getAuthorizedRoles().isEmpty());
        assertTrue(authorizer.getForbiddenRoles().isEmpty());
    }

    @Test
    void constructorWithIdentifier_setsIdentifierAndEmptyLists() {
        RoleAuthorizer authorizer = new RoleAuthorizer("my-id");

        assertEquals("my-id", authorizer.getIdentifier());
        assertTrue(authorizer.getAuthorizedRoles().isEmpty());
        assertTrue(authorizer.getForbiddenRoles().isEmpty());
    }

    @Test
    void authorize_withNoAuthorizedRolesConfigured_returnsTrue() {
        RoleAuthorizer authorizer = new RoleAuthorizer();
        Request request = requestWithRoles();

        assertTrue(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void authorize_withMatchingAuthorizedRole_returnsTrue() {
        Role adminRole = new Role(null, "admin", null);
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getAuthorizedRoles().add(adminRole);

        Request request = requestWithRoles(adminRole);

        assertTrue(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void authorize_withoutMatchingAuthorizedRole_returnsFalse() {
        Role adminRole = new Role(null, "admin", null);
        Role userRole = new Role(null, "user", null);
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getAuthorizedRoles().add(adminRole);

        Request request = requestWithRoles(userRole);

        assertFalse(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void authorize_withMatchingForbiddenRole_returnsFalseEvenWithoutAuthorizedRoles() {
        Role bannedRole = new Role(null, "banned", null);
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getForbiddenRoles().add(bannedRole);

        Request request = requestWithRoles(bannedRole);

        assertFalse(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void authorize_withAuthorizedAndForbiddenRole_returnsFalse() {
        Role adminRole = new Role(null, "admin", null);
        Role bannedRole = new Role(null, "banned", null);
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getAuthorizedRoles().add(adminRole);
        authorizer.getForbiddenRoles().add(bannedRole);

        Request request = requestWithRoles(adminRole, bannedRole);

        assertFalse(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void setAuthorizedRoles_withNull_clearsList() {
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getAuthorizedRoles().add(new Role(null, "admin", null));

        authorizer.setAuthorizedRoles(null);

        assertTrue(authorizer.getAuthorizedRoles().isEmpty());
    }

    @Test
    void setAuthorizedRoles_withSameListInstance_isNoOp() {
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getAuthorizedRoles().add(new Role(null, "admin", null));

        authorizer.setAuthorizedRoles(authorizer.getAuthorizedRoles());

        assertEquals(1, authorizer.getAuthorizedRoles().size());
    }

    @Test
    void setForbiddenRoles_withNull_clearsList() {
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getForbiddenRoles().add(new Role(null, "banned", null));

        authorizer.setForbiddenRoles(null);

        assertTrue(authorizer.getForbiddenRoles().isEmpty());
    }

    @Test
    void setForbiddenRoles_withSameListInstance_isNoOp() {
        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getForbiddenRoles().add(new Role(null, "banned", null));

        authorizer.setForbiddenRoles(authorizer.getForbiddenRoles());

        assertEquals(1, authorizer.getForbiddenRoles().size());
    }
}
