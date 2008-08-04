/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

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
 */
public abstract class DataFacade {

    /**
     * Add a new Contact object in the database.
     * 
     * @param contact
     *            new Contact object to be added.
     * @return the contact object completed with its identfiant.
     */
    public abstract Contact createContact(Contact contact);

    /**
     * Add a new Feed object in the database.
     * 
     * @param feed
     *            new Feed object to be added.
     * @return the feed object completed with its identfiant.
     */
    public abstract Feed createFeed(Feed feed);

    /**
     * Add a new Mail object in the database.
     * 
     * @param mail
     *            new Mail object to be added.
     * @return the mail object completed with its identfiant.
     */
    public abstract Mail createMail(Mail mail);

    /**
     * Add a new Mailbox object in the database.
     * 
     * @param mailbox
     *            new Mailbox object to be added.
     */
    public abstract void createMailbox(Mailbox mailbox);

    /**
     * Add a new User object in the database.
     * 
     * @param user
     *            new User object to be added.
     */
    public abstract void createUser(User user);

    /**
     * Delete a contact.
     * 
     * @param contact
     *            the contact to be deleted.
     */
    public abstract void deleteContact(Contact contact);

    /**
     * Delete a feed.
     * 
     * @param feed
     *            the feed to be deleted.
     */
    public abstract void deleteFeed(Feed feed);

    /**
     * Delete a mail.
     * 
     * @param mail
     *            the mail to be deleted.
     */
    public abstract void deleteMail(Mail mail);

    /**
     * Delete a mailbox.
     * 
     * @param maibox
     *            the mailbox to be deleted.
     */
    public abstract void deleteMailbox(Mailbox mailbox);

    /**
     * Delete a user.
     * 
     * @param user
     *            the user to be deleted.
     */
    public abstract void deleteUser(User user);

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *            the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public abstract Contact getContactById(String contactId);

    /**
     * Get a feed by its id.
     * 
     * @param feedId
     *            the feed's id.
     * @return a Feed object or null if no feed has been found.
     */
    public abstract Feed getFeedById(String feedId);

    /**
     * Get a mailbox by its id.
     * 
     * @param mailboxId
     *            the mailbox's id.
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
     * Get the list of mailboxes owned by a given user.
     * 
     * @param user
     *            the owner.
     * @return the list of mailboxes owned by this user.
     */
    public abstract List<Mailbox> getMailboxes(User user);

    /**
     * Get a mail by its id.
     * 
     * @param mailId
     *            the mail's id.
     * @return a Mail object or null if no mail has been found.
     */
    public abstract Mail getMailById(String mailId);

    /**
     * Get a user by its id.
     * 
     * @param userId
     *            the user's id.
     * @return a User object or null if no user has been found.
     */
    public abstract User getUserById(String userId);

    /**
     * Get a user by its login and password.
     * 
     * @param login
     *            the user's id.
     * @param password
     *            the user's password.
     * @return a User object or null if no user has been found.
     */
    public abstract User getUserByLoginPwd(String login, char[] password);

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
    public abstract List<User> getUsers();

    /**
     * Check that at least one administrator is declared in the database.
     * Otherwise add a new one.
     * 
     */
    public abstract void initAdmin();

    /**
     * Update a contact.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param contact
     *            the contact to be update.
     */
    public abstract void updateContact(Contact contact);

    /**
     * Update a feed.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param feed
     *            the feed to be updated.
     */
    public abstract void updateFeed(Feed feed);

    /**
     * Update a mail.
     * 
     * @param mail
     *            the mail to be updated.
     */
    public abstract void updateMail(Mail mail);

    /**
     * Update a mailbox.
     * 
     * @param maibox
     *            the mailbox to be updated.
     */
    public abstract void updateMailbox(Mailbox mailbox);

    /**
     * Update a user.
     * 
     * @param user
     *            the user to be upated.
     */
    public abstract void updateUser(User user);

}
