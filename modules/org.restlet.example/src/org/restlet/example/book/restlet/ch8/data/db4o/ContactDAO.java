package org.restlet.example.book.restlet.ch8.data.db4o;

import java.util.Date;

import org.restlet.example.book.restlet.ch8.objects.Contact;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * DAO that manages the persistence of Contact objects.
 * 
 */
public class ContactDAO extends Db4oFacade {

    public ContactDAO(ObjectContainer objectContainer) {
        super(objectContainer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.example.book.restlet.ch8.data.db4o.T#createContact(org.restlet.example.book.restlet.ch8.objects.Contact)
     */
    public Contact createContact(Contact contact) {
        contact.setId(Long.toString(new Date().getTime()));
        objectContainer.store(contact);
        objectContainer.commit();

        return contact;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.example.book.restlet.ch8.data.db4o.T#getContactById(java.lang.String)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.example.book.restlet.ch8.data.db4o.T#updateContact(org.restlet.example.book.restlet.ch8.objects.Contact)
     */
    public void updateContact(Contact contact) {
        objectContainer.store(contact);
        objectContainer.commit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.example.book.restlet.ch8.data.db4o.T#deleteContact(org.restlet.example.book.restlet.ch8.objects.Contact)
     */
    public void deleteContact(Contact contact) {
        objectContainer.delete(contact);
        objectContainer.commit();
    }

}
