package org.restlet.example.book.restlet.ch9.dao.db4o;

import java.util.Date;

import org.restlet.example.book.restlet.ch9.objects.Contact;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * DAO that manages the persistence of Contact objects.
 * 
 */
public class ContactDAO {

    /** Db4o object container. */
    private ObjectContainer objectContainer;

    public ContactDAO(ObjectContainer objectContainer) {
        super();
        this.objectContainer = objectContainer;
    }

    /**
     * Add a new Contact object in the database.
     * 
     * @param contact
     *                new Contact object to be added.
     * @return the contact object completed with its identfiant.
     */
    public Contact createContact(Contact contact) {
        contact.setId(Long.toString(new Date().getTime()));
        objectContainer.store(contact);
        objectContainer.commit();

        return contact;
    }

    /**
     * Get a contact by its identifiant.
     * 
     * @param contactId
     *                the contact's identifiant.
     * @return a Contact object or null if no contact has been found.
     */
    public Contact getContactById(String contactId) {
        Contact prototype = new Contact();
        prototype.setId(contactId);

        return getContact(prototype);
    }

    /**
     * Get a contact according to a prototype.
     * 
     * @param prototype
     *                the prototype.
     * @return a Contact object or null if no contact has been found.
     */
    private Contact getContact(Contact prototype) {
        Contact contact = null;
        ObjectSet<Contact> result = objectContainer.queryByExample(prototype);

        if (!result.isEmpty()) {
            contact = result.get(0);
        }

        return contact;
    }

    /**
     * Delete a contact.
     * 
     * @param contact
     *                the contact to be deleted.
     */
    public void deleteContact(Contact contact) {
        objectContainer.delete(contact);
        objectContainer.commit();
    }

    /**
     * Update a contact.
     * 
     * @param contact
     *                the contact to be update.
     */
    public void updateContact(Contact contact) {
        objectContainer.store(contact);
        objectContainer.commit();
    }

}
