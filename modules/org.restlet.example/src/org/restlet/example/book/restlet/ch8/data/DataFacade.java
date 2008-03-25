package org.restlet.example.book.restlet.ch8.data;

import java.util.List;

import org.restlet.example.book.restlet.ch8.objects.Contact;
import org.restlet.example.book.restlet.ch8.objects.Feed;
import org.restlet.example.book.restlet.ch8.objects.Mail;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.example.book.restlet.ch8.objects.User;

/**
 * Simple factory that generates Data Access Objects dedicated to the Db4o
 * database.
 * 
 */
public abstract class DataFacade {

    /**
     * Add a new Contact object in the database.
     * 
     * @param contact
     *                new Contact object to be added.
     * @return the contact object completed with its identfiant.
     */
    public abstract Contact createContact(Contact contact);

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *                the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public abstract Contact getContactById(String contactId);

    /**
     * Update a contact.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param contact
     *                the contact to be update.
     */
    public abstract void updateContact(Contact contact);

    /**
     * Delete a contact.
     * 
     * @param contact
     *                the contact to be deleted.
     */
    public abstract void deleteContact(Contact contact);

    /**
     * Add a new Feed object in the database.
     * 
     * @param feed
     *                new Feed object to be added.
     * @return the feed object completed with its identfiant.
     */
    public abstract Feed createFeed(Feed feed);

    /**
     * Get a feed by its id.
     * 
     * @param feedId
     *                the feed's id.
     * @return a Feed object or null if no feed has been found.
     */
    public abstract Feed getFeedById(String feedId);

    /**
     * Update a feed.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param feed
     *                the feed to be updated.
     */
    public abstract void updateFeed(Feed feed);

    /**
     * Delete a feed.
     * 
     * @param feed
     *                the feed to be deleted.
     */
    public abstract void deleteFeed(Feed feed);

    /**
     * Add a new Mailbox object in the database.
     * 
     * @param mailbox
     *                new Mailbox object to be added.
     * @return the mailbox object completed with its identfiant.
     */
    public abstract Mailbox createMailbox(Mailbox mailbox);

    /**
     * Get a mailbox by its id.
     * 
     * @param mailboxId
     *                the mailbox's id.
     * @return a Mailbox object or null if no mailbox has been found.
     */
    public abstract Mailbox getMailboxById(String mailboxId);

    /**
     * Get the list of all mailboxes.
     * 
     * @return the list of all mailboxes.
     */
    public abstract List<Mailbox> getMailboxes();

    /**
     * Update a mailbox.
     * 
     * @param maibox
     *                the mailbox to be updated.
     */
    public abstract void updateMailbox(Mailbox mailbox);

    /**
     * Delete a mailbox.
     * 
     * @param maibox
     *                the mailbox to be deleted.
     */
    public abstract void deleteMailbox(Mailbox mailbox);

    /**
     * Add a new Mail object in the database.
     * 
     * @param mail
     *                new Mail object to be added.
     * @return the mail object completed with its identfiant.
     */
    public abstract Mail createMail(Mail mail);

    /**
     * Get a mail by its id.
     * 
     * @param mailId
     *                the mail's id.
     * @return a Mail object or null if no mail has been found.
     */
    public abstract Mail getMailById(String mailId);

    /**
     * Update a mail.
     * 
     * @param mail
     *                the mail to be updated.
     */
    public abstract void updateMail(Mail mail);

    /**
     * Delete a mail.
     * 
     * @param mail
     *                the mail to be deleted.
     */
    public abstract void deleteMail(Mail mail);

    /**
     * Check that at least one administrator is declared in the database.
     * Otherwise add a new one.
     * 
     */
    public abstract void initAdmin();

    /**
     * Add a new User object in the database.
     * 
     * @param user
     *                new User object to be added.
     * @return the user object completed with its identfiant.
     */
    public abstract User createUser(User user);

    /**
     * Get a user by its login and password.
     * 
     * @param login
     *                the user's id.
     * @param password
     *                the user's password.
     * @return a User object or null if no user has been found.
     */
    public abstract User getUserByLoginPwd(String login, char[] password);

    /**
     * Get a user by its id.
     * 
     * @param userId
     *                the user's id.
     * @return a User object or null if no user has been found.
     */
    public abstract User getUserById(String userId);

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
    public abstract List<User> getUsers();

    /**
     * Delete a user.
     * 
     * @param user
     *                the user to be deleted.
     */
    public abstract void deleteUser(User user);

    /**
     * Update a user.
     * 
     * @param user
     *                the user to be upated.
     */
    public abstract void updateUser(User user);

    /**
     * Get the list of mailboxes owned by a given user.
     * 
     * @param user
     *                the owner.
     * @return the list of mailboxes owned by this user.
     */
    public abstract List<Mailbox> getMailboxes(User user);

}
