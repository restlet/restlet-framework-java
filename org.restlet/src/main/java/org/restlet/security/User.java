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

package org.restlet.security;

import java.security.Principal;

/**
 * User part of a security realm. Note the same user can be member of several
 * groups.
 * 
 * @see Realm
 * @see Group
 * @author Jerome Louvel
 */
public class User implements Principal {

    /** The email. */
    private volatile String email;

    /** The first name. */
    private volatile String firstName;

    /** The identifier. */
    private volatile String identifier;

    /** The last name. */
    private volatile String lastName;

    /** The secret. */
    private volatile char[] secret;

    /**
     * Default constructor.
     */
    public User() {
        this(null, (char[]) null, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier (login).
     */
    public User(String identifier) {
        this(identifier, (char[]) null, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier (login).
     * @param secret
     *            The identification secret.
     */
    public User(String identifier, char[] secret) {
        this(identifier, secret, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier (login).
     * @param secret
     *            The identification secret.
     * @param firstName
     *            The first name.
     * @param lastName
     *            The last name.
     * @param email
     *            The email.
     */
    public User(String identifier, char[] secret, String firstName,
            String lastName, String email) {
        this.identifier = identifier;
        this.secret = secret;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier (login).
     * @param secret
     *            The identification secret.
     */
    public User(String identifier, String secret) {
        this(identifier, secret.toCharArray());
    }

    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier (login).
     * @param secret
     *            The identification secret.
     * @param firstName
     *            The first name.
     * @param lastName
     *            The last name.
     * @param email
     *            The email.
     */
    public User(String identifier, String secret, String firstName,
            String lastName, String email) {
        this(identifier, secret.toCharArray(), firstName, lastName, email);
    }

    /**
     * Returns the email.
     * 
     * @return The email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the first name.
     * 
     * @return The first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the identifier.
     * 
     * @return The identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the last name.
     * 
     * @return The last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the user identifier.
     * 
     * @see #getIdentifier()
     */
    public String getName() {
        return getIdentifier();
    }

    /**
     * Returns the secret.
     * 
     * @return The secret.
     */
    public char[] getSecret() {
        return secret;
    }

    /**
     * Sets the email.
     * 
     * @param email
     *            The email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the first name.
     * 
     * @param firstName
     *            The first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the identifier.
     * 
     * @param identifier
     *            The identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the last name.
     * 
     * @param lastName
     *            The last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the secret.
     * 
     * @param secret
     *            The secret.
     */
    public void setSecret(char[] secret) {
        this.secret = secret;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }
}
