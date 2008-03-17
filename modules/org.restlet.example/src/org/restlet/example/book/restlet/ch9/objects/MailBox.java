package org.restlet.example.book.restlet.ch9.objects;

import java.util.List;

/**
 * Mailbox.
 * 
 */
public class MailBox {
    /** List of contacts of the mailbox. */
    private List<Contact> contacts;

    /** List of feed of the mailbox. */
    private List<Feed> feeds;

    /** List of mails of the mailbox. */
    private List<Mail> mails;

    /** Owner of the mailbox. */
    private User owner;

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public List<Mail> getMails() {
        return mails;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

}
