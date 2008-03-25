package org.restlet.example.book.restlet.ch8.data.db4o;

import org.restlet.example.book.restlet.ch8.data.DataFacade;
import org.restlet.example.book.restlet.ch8.objects.Contact;

import com.db4o.ObjectContainer;

/**
 * DAO that manages the persistence for the DB4O database.
 * 
 */
public class Db4oFacade extends DataFacade {

    /** Db4o object container. */
    protected ObjectContainer objectContainer;

    public Db4oFacade(ObjectContainer objectContainer) {
        super();
        this.objectContainer = objectContainer;
    }

    @Override
    public Contact createContact(Contact contact) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteContact(Contact contact) {
        // TODO Auto-generated method stub

    }

    @Override
    public Contact getContactById(String contactId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateContact(Contact contact) {
        // TODO Auto-generated method stub

    }

}
