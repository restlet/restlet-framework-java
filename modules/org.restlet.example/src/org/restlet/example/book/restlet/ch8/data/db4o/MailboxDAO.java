package org.restlet.example.book.restlet.ch8.data.db4o;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch8.objects.Mailbox;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.db4o.query.QueryComparator;

/**
 * DAO that manages the persistence of Mailbox objects.
 * 
 */
public class MailboxDAO extends Db4oDAO {

    public MailboxDAO(ObjectContainer objectContainer) {
        super(objectContainer);
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
     * Get a mailbox by its id.
     * 
     * @param mailboxId
     *                the mailbox's id.
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
        // Get all mailboxes
        Predicate<Mailbox> predicate = new Predicate<Mailbox>() {
            static final long serialVersionUID = 1l;

            @Override
            public boolean match(Mailbox arg0) {
                return true;
            }
        };
        // Sort by owner name
        QueryComparator<Mailbox> comparator = new QueryComparator<Mailbox>() {
            static final long serialVersionUID = 1l;

            public int compare(Mailbox arg0, Mailbox arg1) {
                int result = arg0.getOwner().getLastName().compareToIgnoreCase(
                        arg1.getOwner().getLastName());
                if (result == 0) {
                    result = arg0.getNickname().compareToIgnoreCase(
                            arg1.getNickname());
                }
                return result;
            }

        };

        List<Mailbox> result = new ArrayList<Mailbox>();
        ObjectSet<Mailbox> list = objectContainer.query(predicate, comparator);
        result.addAll(list);
        return result;
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

}
