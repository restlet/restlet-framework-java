/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jaas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Principal;
import javax.security.auth.Subject;
import org.junit.jupiter.api.Test;
import org.restlet.data.ClientInfo;
import org.restlet.security.Role;
import org.restlet.security.User;

/** Unit tests for {@link JaasUtils}. */
class JaasUtilsTestCase {

    @Test
    void testCreateSubjectWithNullClientInfo() {
        Subject subject = JaasUtils.createSubject(null);

        assertNotNull(subject);
        assertTrue(subject.getPrincipals().isEmpty());
    }

    @Test
    void testCreateSubjectWithEmptyClientInfo() {
        ClientInfo clientInfo = new ClientInfo();
        Subject subject = JaasUtils.createSubject(clientInfo);

        assertNotNull(subject);
        assertTrue(subject.getPrincipals().isEmpty());
    }

    @Test
    void testCreateSubjectWithUser() {
        ClientInfo clientInfo = new ClientInfo();
        User user = new User("alice");
        clientInfo.setUser(user);

        Subject subject = JaasUtils.createSubject(clientInfo);

        assertEquals(1, subject.getPrincipals().size());
        assertTrue(subject.getPrincipals().contains(user));
    }

    @Test
    void testCreateSubjectWithRole() {
        ClientInfo clientInfo = new ClientInfo();
        Role role = new Role(null, "admin");
        clientInfo.getRoles().add(role);

        Subject subject = JaasUtils.createSubject(clientInfo);

        assertEquals(1, subject.getPrincipals().size());
        assertTrue(subject.getPrincipals().contains(role));
    }

    @Test
    void testCreateSubjectWithPrincipal() {
        ClientInfo clientInfo = new ClientInfo();
        Principal principal = () -> "custom-principal";
        clientInfo.getPrincipals().add(principal);

        Subject subject = JaasUtils.createSubject(clientInfo);

        assertEquals(1, subject.getPrincipals().size());
        assertTrue(subject.getPrincipals().contains(principal));
    }

    @Test
    void testCreateSubjectWithAllTypes() {
        ClientInfo clientInfo = new ClientInfo();
        User user = new User("bob");
        Role role = new Role(null, "editor");
        Principal principal = () -> "extra";

        clientInfo.setUser(user);
        clientInfo.getRoles().add(role);
        clientInfo.getPrincipals().add(principal);

        Subject subject = JaasUtils.createSubject(clientInfo);

        assertEquals(3, subject.getPrincipals().size());
        assertTrue(subject.getPrincipals().contains(user));
        assertTrue(subject.getPrincipals().contains(role));
        assertTrue(subject.getPrincipals().contains(principal));
    }

    @Test
    void testDoAsPrivileged() {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setUser(new User("eve"));

        String result = JaasUtils.doAsPriviledged(clientInfo, () -> "executed");

        assertEquals("executed", result);
    }
}
