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

package org.restlet.example.ext.rdf.foaf.data;

import java.util.List;

import org.restlet.example.ext.rdf.foaf.objects.Contact;
import org.restlet.example.ext.rdf.foaf.objects.User;

/**
 * Simple factory that generates Data Access Objects.
 */
public abstract class DataFacade {

    /**
     * Add a new Contact object in the database.
     * 
     * @param user
     *            parent user object.
     * @param contact
     *            new Contact object to be added.
     * @return the contact object completed with its identifiant.
     */
    public abstract Contact createContact(User user, Contact contact);

    /**
     * 
     * 
     * @param user
     * 
     */
    /**
     * Add a new User object in the database.
     * 
     * @param user
     *            new User object to be added.
     * @return the user object completed with its identifiant.
     */
    public abstract User createUser(User user);

    /**
     * Delete a contact.
     * 
     * @param contact
     *            the contact to be deleted.
     */
    public abstract void deleteContact(Contact contact);

    /**
     * Delete a user.
     * 
     * @param user
     *            the user to be deleted.
     */
    public abstract void deleteUser(User user);

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *            the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public abstract Contact getContactById(String contactId);

    /**
     * Get the list of contacts owned by a given user.
     * 
     * @param user
     *            the owner.
     * @return the list of contacts owned by this user.
     */
    public abstract List<Contact> getContacts(User user);

    /**
     * Get a user by its id.
     * 
     * @param userId
     *            the user's id.
     * @return a User object or null if no user has been found.
     */
    public abstract User getUserById(String userId);

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
    public abstract List<User> getUsers();

    /**
     * Update a contact.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param contact
     *            the contact to be update.
     */
    public abstract void updateContact(Contact contact);

    /**
     * Update a user.
     * 
     * @param user
     *            the user to be upated.
     */
    public abstract void updateUser(User user);

}
