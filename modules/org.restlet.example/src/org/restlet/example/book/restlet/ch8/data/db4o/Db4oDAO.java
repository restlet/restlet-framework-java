package org.restlet.example.book.restlet.ch8.data.db4o;

import com.db4o.ObjectContainer;

/**
 * DAO that manages the persistence for the DB4O database.
 * 
 */
public class Db4oDAO {

    /** Db4o object container. */
    protected ObjectContainer objectContainer;

    public Db4oDAO(ObjectContainer objectContainer) {
        super();
        this.objectContainer = objectContainer;
    }

}
