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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Group}. */
class GroupTestCase {

    @Test
    void defaultConstructor_hasNullNameAndInheritsRoles() {
        Group group = new Group();

        assertNull(group.getName());
        assertNull(group.getDescription());
        assertTrue(group.isInheritingRoles());
        assertTrue(group.getMemberGroups().isEmpty());
        assertTrue(group.getMemberUsers().isEmpty());
    }

    @Test
    void constructorWithNameAndDescription_setsFieldsAndInheritsRoles() {
        Group group = new Group("admins", "Administrators group");

        assertEquals("admins", group.getName());
        assertEquals("Administrators group", group.getDescription());
        assertTrue(group.isInheritingRoles());
    }

    @Test
    void constructorWithInheritingRolesFalse_setsFieldFalse() {
        Group group = new Group("admins", "Administrators group", false);
        assertFalse(group.isInheritingRoles());
    }

    @Test
    void getNameSetName_roundTrip() {
        Group group = new Group();
        group.setName("users");
        assertEquals("users", group.getName());
    }

    @Test
    void getDescriptionSetDescription_roundTrip() {
        Group group = new Group();
        group.setDescription("desc");
        assertEquals("desc", group.getDescription());
    }

    @Test
    void isInheritingRolesSetInheritingRoles_roundTrip() {
        Group group = new Group();
        group.setInheritingRoles(false);
        assertFalse(group.isInheritingRoles());
    }

    @Test
    void getMemberGroups_isModifiableAndTracksMembership() {
        Group group = new Group();
        Group child = new Group("child", null);

        group.getMemberGroups().add(child);

        assertEquals(1, group.getMemberGroups().size());
        assertSame(child, group.getMemberGroups().getFirst());
    }

    @Test
    void getMemberUsers_isModifiableAndTracksMembership() {
        Group group = new Group();
        User user = new User("bob");

        group.getMemberUsers().add(user);

        assertEquals(1, group.getMemberUsers().size());
        assertSame(user, group.getMemberUsers().getFirst());
    }

    @Test
    void setMemberGroups_withNull_clearsList() {
        Group group = new Group();
        group.getMemberGroups().add(new Group("child", null));

        group.setMemberGroups(null);

        assertTrue(group.getMemberGroups().isEmpty());
    }

    @Test
    void setMemberGroups_withNewList_replacesContents() {
        Group group = new Group();
        group.getMemberGroups().add(new Group("old", null));
        Group newChild = new Group("new", null);

        group.setMemberGroups(List.of(newChild));

        assertEquals(1, group.getMemberGroups().size());
        assertSame(newChild, group.getMemberGroups().getFirst());
    }

    @Test
    void setMemberGroups_withSameListInstance_isNoOp() {
        Group group = new Group();
        group.getMemberGroups().add(new Group("child", null));

        group.setMemberGroups(group.getMemberGroups());

        assertEquals(1, group.getMemberGroups().size());
    }

    @Test
    void setMemberUsers_withNull_clearsList() {
        Group group = new Group();
        group.getMemberUsers().add(new User("bob"));

        group.setMemberUsers(null);

        assertTrue(group.getMemberUsers().isEmpty());
    }

    @Test
    void setMemberUsers_withNewList_replacesContents() {
        Group group = new Group();
        group.getMemberUsers().add(new User("old"));
        User newUser = new User("new");

        group.setMemberUsers(List.of(newUser));

        assertEquals(1, group.getMemberUsers().size());
        assertSame(newUser, group.getMemberUsers().getFirst());
    }

    @Test
    void setMemberUsers_withSameListInstance_isNoOp() {
        Group group = new Group();
        group.getMemberUsers().add(new User("bob"));

        group.setMemberUsers(group.getMemberUsers());

        assertEquals(1, group.getMemberUsers().size());
    }

    @Test
    void toString_returnsName() {
        Group group = new Group("admins", null);
        assertEquals("admins", group.toString());
    }
}
