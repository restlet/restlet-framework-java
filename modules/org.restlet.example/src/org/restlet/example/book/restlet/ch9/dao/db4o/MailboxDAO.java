package org.restlet.example.book.restlet.ch9.dao.db4o;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch9.objects.Mailbox;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

/**
 * DAO that manages the persistence of Mailbox objects.
 * 
 */
public class MailboxDAO {

    /** Db4o object container. */
    private ObjectContainer objectContainer;

    public MailboxDAO(ObjectContainer objectContainer) {
        super();
        this.objectContainer = objectContainer;
    }

    /**
     * Add a new Mailbox object in the database.
     * 
     * @param mailbox
     *                new Mailbox object to be added.
     * @return the mailbox object completed with its identfiant.
     */
    public Mailbox createMailbox(Mailbox mailbox) {
        mailbox.setId(Long.toString(new Date().getTime()));
        objectContainer.store(mailbox);
        objectContainer.commit();

        return mailbox;
    }

    /**
     * Get a mailbox by its identifiant.
     * 
     * @param mailboxId
     *                the mailbox's identifiant.
     * @return a Mailbox object or null if no mailbox has been found.
     */
    public Mailbox getMailboxById(String mailboxId) {
        Mailbox prototype = new Mailbox();
        prototype.setId(mailboxId);

        return getMailbox(prototype);
    }

    /**
     * Get a mailbox according to a prototype.
     * 
     * @param prototype
     *                the prototype.
     * @return a Mailbox object or null if no mailbox has been found.
     */
    private Mailbox getMailbox(Mailbox prototype) {
        Mailbox mailbox = null;
        ObjectSet<Mailbox> result = objectContainer.queryByExample(prototype);

        if (!result.isEmpty()) {
            mailbox = result.get(0);
        }

        return mailbox;
    }

    /**
     * Get the list of all mailboxes.
     * 
     * @return the list of all mailboxes.
     */
    public List<Mailbox> getMailboxes() {
        List<Mailbox> result = new ArrayList<Mailbox>();
        ObjectSet<Mailbox> list = objectContainer.queryByExample(new Mailbox());
        result.addAll(list);
        return result;
    }

    /**
     * Delete a mailbox.
     * 
     * @param maibox
     *                the mailbox to be deleted.
     */
    public void deleteMailbox(Mailbox mailbox) {
        objectContainer.delete(mailbox);
        objectContainer.commit();
    }

    /**
     * Update a mailbox.
     * 
     * @param maibox
     *                the mailbox to be updated.
     */
    public void updateMailbox(Mailbox mailbox) {
        objectContainer.store(mailbox);
        objectContainer.commit();
    }
}
