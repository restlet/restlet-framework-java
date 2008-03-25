package org.restlet.example.book.restlet.ch8.data.db4o;

import java.util.Date;

import org.restlet.example.book.restlet.ch8.objects.Mail;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * DAO that manages the persistence of Mail objects.
 * 
 */
public class MailDAO extends Db4oFacade {

    public MailDAO(ObjectContainer objectContainer) {
        super(objectContainer);
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
     * Get a mail by its id.
     * 
     * @param mailId
     *                the mail's id.
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
     * Update a mail.
     * 
     * @param mail
     *                the mail to be updated.
     */
    public void updateMail(Mail mail) {
        objectContainer.store(mail);
        objectContainer.commit();
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

}
