package org.restlet.example.book.restlet.ch8.data;

import org.restlet.example.book.restlet.ch8.objects.Contact;

/**
 * Simple factory that generates Data Access Objects dedicated to the Db4o
 * database.
 * 
 */
public abstract class DataFacade {

    /**
     * Add a new Contact object in the database.
     * 
     * @param contact
     *                new Contact object to be added.
     * @return the contact object completed with its identfiant.
     */
    public abstract Contact createContact(Contact contact);

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *                the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public abstract Contact getContactById(String contactId);

    /**
     * Update a contact.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param contact
     *                the contact to be update.
     */
    public abstract void updateContact(Contact contact);

    /**
     * Delete a contact.
     * 
     * @param contact
     *                the contact to be deleted.
     */
    public abstract void deleteContact(Contact contact);

}
