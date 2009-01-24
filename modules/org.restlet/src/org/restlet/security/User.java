/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.security;

/**
 * User part of an organization. Note the same user can be member of any number
 * of groups.
 * 
 * @author Jerome Louvel
 */
public class User {

    /** The email. */
    private volatile String email;

    /** The first name. */
    private volatile String firstName;

    /** The last name. */
    private volatile String lastName;

    /** The title. */
    private volatile String title;

    /**
     * Default constructor.
     */
    public User() {
        this(null, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param firstName
     *            The first name.
     * @param lastName
     *            The last name.
     * @param title
     *            The title.
     * @param email
     *            The email.
     */
    public User(String firstName, String lastName, String title, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.email = email;
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
     * Returns the last name.
     * 
     * @return The last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the title.
     * 
     * @return The title.
     */
    public String getTitle() {
        return title;
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
     * Sets the last name.
     * 
     * @param lastName
     *            The last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the title.
     * 
     * @param title
     *            The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
