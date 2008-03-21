package org.restlet.example.book.restlet.ch9.dao.db4o;

import org.restlet.example.book.restlet.ch9.objects.Mail;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * DAO that manages the persistence of Mail objects.
 * 
 */
public class MailDAO {

    /** Db4o object container. */
    private ObjectContainer objectContainer;

    public MailDAO(ObjectContainer objectContainer) {
        super();
        this.objectContainer = objectContainer;
    }

    /**
     * Get a mail by its identifiant.
     * 
     * @param mailId
     *                the mail's identifiant.
     * @return a Mail object or null if no mail has been found.
     */
    public Mail getMailById(String mailId) {
        Mail prototype = new Mail();
        prototype.setId(mailId);

        return getMail(prototype);
    }

    /**
     * Get a mail according to a prototype.
     * 
     * @param prototype
     *                the prototype.
     * @return a Mail object or null if no mail has been found.
     */
    private Mail getMail(Mail prototype) {
        Mail mail = null;
        ObjectSet<Mail> result = objectContainer.queryByExample(prototype);

        if (!result.isEmpty()) {
            mail = result.get(0);
        }

        return mail;
    }
}
