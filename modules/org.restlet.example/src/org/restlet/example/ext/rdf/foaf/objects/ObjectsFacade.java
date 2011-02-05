/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.example.ext.rdf.foaf.objects;

import java.util.List;

import org.restlet.example.ext.rdf.foaf.data.DataFacade;

/**
 *
 */
public class ObjectsFacade {

    /** Data facade. */
    protected DataFacade dataFacade;

    public ObjectsFacade(DataFacade dataFacade) {
        super();
        this.dataFacade = dataFacade;
    }

    /**
     * Add a new Contact object in the database.
     * 
     * @param User
     *            the parent user.
     * @param contact
     *            new Contact object to be added.
     * @return the contact object completed with its identifiant.
     */
    public Contact createContact(User user, Contact contact) {
        contact = this.dataFacade.createContact(user, contact);
        user.getContacts().add(contact);
        this.dataFacade.updateUser(user);

        return contact;
    }

    /**
     * Add a new User object in the database.
     * 
     * @param user
     *            new User object to be added.
     * @return the user object completed with its identfiant.
     * @throws ObjectsException
     */
    public User createUser(User user) throws ObjectsException {
        user = this.dataFacade.createUser(user);
        return user;
    }

    /**
     * Delete a contact.
     * 
     * @param user
     *            the parent user.
     * @param contact
     *            the contact to be deleted.
     */
    public void deleteContact(User user, Contact contact) {
        // Remove the contact from the mailbox's list of contacts.
        boolean found = false;
        for (int i = 0; (i < user.getContacts().size()) && !found; i++) {
            final Contact contact2 = user.getContacts().get(i);
            if (contact2.getId().equals(contact.getId())) {
                user.getContacts().remove(i);
                found = true;
            }
        }

        this.dataFacade.deleteContact(contact);
        this.dataFacade.updateUser(user);
    }

    /**
     * Delete a user.
     * 
     * @param user
     *            the user to be deleted.
     */
    public void deleteUser(User user) {
        // Delete the user and its mailboxes.
        for (final Contact contact : user.getContacts()) {
            this.dataFacade.deleteContact(contact);
        }
        this.dataFacade.deleteUser(user);
    }

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *            the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public Contact getContactById(String contactId) {
        return this.dataFacade.getContactById(contactId);
    }

    /**
     * Get a user by its id.
     * 
     * @param userId
     *            the user's id.
     * @return a User object or null if no user has been found.
     */
    public User getUserById(String userId) {
        return this.dataFacade.getUserById(userId);
    }

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
    public List<User> getUsers() {
        return this.dataFacade.getUsers();
    }

    /**
     * Update a contact.
     * 
     * @param user
     *            the parent user.
     * @param contact
     *            the contact to be update.
     */
    public void updateContact(User user, Contact contact) {
        this.dataFacade.updateContact(contact);
    }

    /**
     * Update a user.
     * 
     * @param user
     *            the user to be upated.
     */
    public void updateUser(User user) {
        this.dataFacade.updateUser(user);
    }

}
