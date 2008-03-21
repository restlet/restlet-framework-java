package org.restlet.example.book.restlet.ch9.dao.db4o;

import org.restlet.example.book.restlet.ch9.objects.Contact;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * DAO that manages the persistence of Contact objects.
 * 
 */
public class ContactDAO extends Db4oDAO {

    public ContactDAO(ObjectContainer objectContainer) {
        super(objectContainer);
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

}
