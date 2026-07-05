/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link User}. */
class UserTestCase {

    @Test
    void defaultConstructor_hasNullFields() {
        User user = new User();

        assertNull(user.getIdentifier());
        assertNull(user.getSecret());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
    }

    @Test
    void constructorWithIdentifier_setsIdentifierOnly() {
        User user = new User("alice");

        assertEquals("alice", user.getIdentifier());
        assertNull(user.getSecret());
    }

    @Test
    void constructorWithIdentifierAndCharSecret_setsBoth() {
        char[] secret = "secret".toCharArray();
        User user = new User("alice", secret);

        assertEquals("alice", user.getIdentifier());
        assertArrayEquals(secret, user.getSecret());
    }

    @Test
    void constructorWithIdentifierAndStringSecret_convertsToCharArray() {
        User user = new User("alice", "secret");

        assertEquals("alice", user.getIdentifier());
        assertArrayEquals("secret".toCharArray(), user.getSecret());
    }

    @Test
    void constructorWithAllFieldsAndCharSecret_setsAllFields() {
        char[] secret = "secret".toCharArray();
        User user = new User("alice", secret, "Alice", "Wonderland", "alice@example.com");

        assertEquals("alice", user.getIdentifier());
        assertArrayEquals(secret, user.getSecret());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Wonderland", user.getLastName());
        assertEquals("alice@example.com", user.getEmail());
    }

    @Test
    void constructorWithAllFieldsAndStringSecret_setsAllFields() {
        User user = new User("alice", "secret", "Alice", "Wonderland", "alice@example.com");

        assertEquals("alice", user.getIdentifier());
        assertArrayEquals("secret".toCharArray(), user.getSecret());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Wonderland", user.getLastName());
        assertEquals("alice@example.com", user.getEmail());
    }

    @Test
    void getIdentifierSetIdentifier_roundTrip() {
        User user = new User();
        user.setIdentifier("bob");
        assertEquals("bob", user.getIdentifier());
    }

    @Test
    void getSecretSetSecret_roundTrip() {
        User user = new User();
        char[] secret = "secret".toCharArray();
        user.setSecret(secret);
        assertArrayEquals(secret, user.getSecret());
    }

    @Test
    void getFirstNameSetFirstName_roundTrip() {
        User user = new User();
        user.setFirstName("Bob");
        assertEquals("Bob", user.getFirstName());
    }

    @Test
    void getLastNameSetLastName_roundTrip() {
        User user = new User();
        user.setLastName("Builder");
        assertEquals("Builder", user.getLastName());
    }

    @Test
    void getEmailSetEmail_roundTrip() {
        User user = new User();
        user.setEmail("bob@example.com");
        assertEquals("bob@example.com", user.getEmail());
    }

    @Test
    void getName_returnsIdentifier() {
        User user = new User("bob");
        assertEquals("bob", user.getName());
    }

    @Test
    void toString_returnsIdentifier() {
        User user = new User("bob");
        assertEquals("bob", user.toString());
    }
}
