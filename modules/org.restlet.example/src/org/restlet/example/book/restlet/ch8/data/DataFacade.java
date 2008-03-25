package org.restlet.example.book.restlet.ch8.data;

import org.restlet.example.book.restlet.ch8.data.db4o.ContactDAO;
import org.restlet.example.book.restlet.ch8.data.db4o.FeedDAO;
import org.restlet.example.book.restlet.ch8.data.db4o.MailDAO;
import org.restlet.example.book.restlet.ch8.data.db4o.MailboxDAO;
import org.restlet.example.book.restlet.ch8.data.db4o.UserDAO;

import com.db4o.ObjectContainer;

/**
 * Simple factory that generates Data Access Objects dedicated to the Db4o
 * database.
 * 
 */
public class DataFacade {

    /** Db4o object container. */
    private ObjectContainer objectContainer;

    public DataFacade(ObjectContainer objectContainer) {
        super();
        this.objectContainer = objectContainer;
    }

    /**
     * Return a new DAO instance for managing contact data.
     * 
     * @return a new DAO instance for managing contact data.
     */
    public ContactDAO getContactDAO() {
        return new ContactDAO(objectContainer);
    }

    /**
     * Return a new DAO instance for managing feed data.
     * 
     * @return a new DAO instance for managing feed data.
     */
    public FeedDAO getFeedDAO() {
        return new FeedDAO(objectContainer);
    }

    /**
     * Return a new DAO instance for managing mailbox data.
     * 
     * @return a new DAO instance for managing mailbox data.
     */
    public MailboxDAO getMailboxDAO() {
        return new MailboxDAO(objectContainer);
    }

    /**
     * Return a new DAO instance for managing mail data.
     * 
     * @return a new DAO instance for managing mail data.
     */
    public MailDAO getMailDAO() {
        return new MailDAO(objectContainer);
    }

    /**
     * Return a new DAO instance for managing user data.
     * 
     * @return a new DAO instance for managing user data.
     */
    public UserDAO getUserDAO() {
        return new UserDAO(objectContainer);
    }

}
