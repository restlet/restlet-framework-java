package org.restlet.example.book.restlet.ch9.dao.db4o;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Feed;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

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

    /**
     * Add a new Contact object in the database.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param contact
     *                new Contact object to be added.
     * @return the contact object completed with its identfiant.
     */
    public Contact createContact(Mailbox mailbox, Contact contact) {
        contact.setId(Long.toString(new Date().getTime()));
        objectContainer.store(contact);
        mailbox.getContacts().add(contact);
        objectContainer.store(mailbox);
        objectContainer.commit();

        return contact;
    }

    /**
     * Delete a contact.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param contact
     *                the contact to be deleted.
     */
    public void deleteContact(Mailbox mailbox, Contact contact) {
        // Remove the contact from the mailbox's list of contacts.
        boolean found = false;
        for (int i = 0; i < mailbox.getContacts().size() && !found; i++) {
            Contact contact2 = mailbox.getContacts().get(i);
            if (contact2.getId().equals(contact.getId())) {
                mailbox.getContacts().remove(i);
                found = true;
            }
        }

        objectContainer.store(mailbox);
        objectContainer.delete(contact);
        objectContainer.commit();
    }

    /**
     * Update a contact.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param contact
     *                the contact to be update.
     */
    public void updateContact(Mailbox mailbox, Contact contact) {
        objectContainer.store(mailbox);
        objectContainer.store(contact);
        objectContainer.commit();
    }

    /**
     * Add a new Feed object in the database.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param feed
     *                new Feed object to be added.
     * @return the feed object completed with its identfiant.
     */
    public Feed createFeed(Mailbox mailbox, Feed feed) {
        feed.setId(Long.toString(new Date().getTime()));
        objectContainer.store(feed);
        mailbox.getFeeds().add(feed);
        objectContainer.store(mailbox);
        objectContainer.commit();

        return feed;
    }

    /**
     * Delete a feed.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param feed
     *                the feed to be deleted.
     */
    public void deleteFeed(Mailbox mailbox, Feed feed) {
        // Remove the feed from the mailbox's list of contacts.
        boolean found = false;
        for (int i = 0; i < mailbox.getFeeds().size() && !found; i++) {
            Feed feed2 = mailbox.getFeeds().get(i);
            if (feed2.getId().equals(feed.getId())) {
                mailbox.getFeeds().remove(i);
                found = true;
            }
        }

        objectContainer.store(mailbox);
        objectContainer.delete(feed);
        objectContainer.commit();
    }

    /**
     * Update a feed.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param feed
     *                the feed to be updated.
     */
    public void updateFeed(Mailbox mailbox, Feed feed) {
        objectContainer.store(mailbox);
        objectContainer.store(feed);
        objectContainer.commit();
    }

    /**
     * Add a new Mail object in the database.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param mail
     *                new Mail object to be added.
     * @return the mail object completed with its identfiant.
     */
    public Mail createMail(Mailbox mailbox, Mail mail) {
        mail.setId(Long.toString(new Date().getTime()));
        objectContainer.store(mail);
        mailbox.getMails().add(mail);
        objectContainer.store(mailbox);
        objectContainer.commit();

        return mail;
    }

    /**
     * Delete a mail.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param mail
     *                the mail to be deleted.
     */
    public void deleteMail(Mailbox mailbox, Mail mail) {
        // Remove the mail from the mailbox's list of contacts.
        boolean found = false;
        for (int i = 0; i < mailbox.getMails().size() && !found; i++) {
            Mail mail2 = mailbox.getMails().get(i);
            if (mail2.getId().equals(mail.getId())) {
                mailbox.getMails().remove(i);
                found = true;
            }
        }

        objectContainer.store(mailbox);
        objectContainer.delete(mail);
        objectContainer.commit();
    }

    /**
     * Update a mail.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param mail
     *                the mail to be updated.
     */
    public void updateMail(Mailbox mailbox, Mail mail) {
        objectContainer.store(mailbox);
        objectContainer.store(mail);
        objectContainer.commit();
    }

}
