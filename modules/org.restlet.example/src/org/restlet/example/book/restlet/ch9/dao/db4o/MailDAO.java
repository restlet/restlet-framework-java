package org.restlet.example.book.restlet.ch9.dao.db4o;

import java.util.Date;

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
     * Add a new Mail object in the database.
     * 
     * @param mail
     *                new Mail object to be added.
     * @return the mail object completed with its identfiant.
     */
    public Mail createMail(Mail mail) {
        mail.setId(Long.toString(new Date().getTime()));
        objectContainer.store(mail);
        objectContainer.commit();

        return mail;
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

    /**
     * Delete a mail.
     * 
     * @param mail
     *                the mail to be deleted.
     */
    public void deleteMail(Mail mail) {
        objectContainer.delete(mail);
        objectContainer.commit();
    }

    /**
     * Update a mail.
     * 
     * @param mail
     *                the mail to be updated.
     */
    public void updateMail(Mail mail) {
        objectContainer.store(mail);
        objectContainer.commit();
    }

}
