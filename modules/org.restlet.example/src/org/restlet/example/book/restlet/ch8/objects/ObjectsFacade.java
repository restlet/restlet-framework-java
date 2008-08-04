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

package org.restlet.example.book.restlet.ch8.objects;

import java.util.ArrayList;
import java.util.List;

import org.restlet.example.book.restlet.ch8.data.DataFacade;

/**
 *
 */
public class ObjectsFacade {

    /** Data facade. */
    protected DataFacade dataFacade;

    public ObjectsFacade(DataFacade dataFacade) {
        super();
        this.dataFacade = dataFacade;
    }

    /**
     * Add a new Contact object in the database.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param contact
     *            new Contact object to be added.
     * @return the contact object completed with its identfiant.
     */
    public Contact createContact(Mailbox mailbox, Contact contact) {
        contact = this.dataFacade.createContact(contact);
        mailbox.getContacts().add(contact);
        this.dataFacade.updateMailbox(mailbox);

        return contact;
    }

    /**
     * Add a new Feed object in the database.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param feed
     *            new Feed object to be added.
     * @return the feed object completed with its identfiant.
     */
    public Feed createFeed(Mailbox mailbox, Feed feed) {
        feed = this.dataFacade.createFeed(feed);
        mailbox.getFeeds().add(feed);
        this.dataFacade.updateMailbox(mailbox);

        return feed;
    }

    /**
     * Add a new Mail object in the database.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param mail
     *            new Mail object to be added.
     * @return the mail object completed with its identfiant.
     */
    public Mail createMail(Mailbox mailbox, Mail mail) {
        mail = this.dataFacade.createMail(mail);
        mailbox.getMails().add(mail);
        this.dataFacade.updateMailbox(mailbox);

        return mail;
    }

    /**
     * Add a new Mailbox object in the database.
     * 
     * @param mailbox
     *            new Mailbox object to be added.
     * @return the mailbox object completed with its identfiant.
     * @throws ObjectsException
     */
    public Mailbox createMailbox(Mailbox mailbox) throws ObjectsException {
        mailbox.setId(mailbox.getNickname());

        if (this.dataFacade.getMailboxById(mailbox.getId()) != null) {
            throw new ObjectsException("An other mailbox has the same name.");
        }
        this.dataFacade.createMailbox(mailbox);

        return mailbox;
    }

    /**
     * Add a new User object in the database.
     * 
     * @param user
     *            new User object to be added.
     * @return the user object completed with its identfiant.
     * @throws ObjectsException
     */
    public User createUser(User user) throws ObjectsException {
        user.setId(user.getLogin());

        if (this.dataFacade.getUserById(user.getId()) != null) {
            throw new ObjectsException("An other user has the same login.");
        }

        this.dataFacade.createUser(user);
        return user;
    }

    /**
     * Delete a contact.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param contact
     *            the contact to be deleted.
     */
    public void deleteContact(Mailbox mailbox, Contact contact) {
        // Remove the contact from the mailbox's list of contacts.
        boolean found = false;
        for (int i = 0; (i < mailbox.getContacts().size()) && !found; i++) {
            final Contact contact2 = mailbox.getContacts().get(i);
            if (contact2.getId().equals(contact.getId())) {
                mailbox.getContacts().remove(i);
                found = true;
            }
        }

        this.dataFacade.deleteContact(contact);
        this.dataFacade.updateMailbox(mailbox);
    }

    /**
     * Delete a feed.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param feed
     *            the feed to be deleted.
     */
    public void deleteFeed(Mailbox mailbox, Feed feed) {
        // Remove the feed from the mailbox's list of feeds.
        boolean found = false;
        for (int i = 0; (i < mailbox.getFeeds().size()) && !found; i++) {
            final Feed feed2 = mailbox.getFeeds().get(i);
            if (feed2.getId().equals(feed.getId())) {
                mailbox.getFeeds().remove(i);
                found = true;
            }
        }

        this.dataFacade.deleteFeed(feed);
        this.dataFacade.updateMailbox(mailbox);
    }

    /**
     * Delete a mail from a mailbox.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param mail
     *            the mail to be deleted.
     */
    public void deleteMail(Mailbox mailbox, Mail mail) {
        // Remove the mail from the mailbox's list of mails.
        boolean found = false;
        for (int i = 0; (i < mailbox.getMails().size()) && !found; i++) {
            final Mail mail2 = mailbox.getMails().get(i);
            if (mail2.getId().equals(mail.getId())) {
                mailbox.getMails().remove(i);
                found = true;
            }
        }

        // Delete its list of unregistered recipients
        for (final Contact contact : mail.getRecipients()) {
            if (contact.getId() == null) {
                this.dataFacade.deleteContact(contact);
            }
        }
        // Delete its list of sender if not registered.
        if (mail.getSender().getId() == null) {
            this.dataFacade.deleteContact(mail.getSender());
        }

        this.dataFacade.deleteMail(mail);
        this.dataFacade.updateMailbox(mailbox);
    }

    /**
     * Delete a mailbox.
     * 
     * @param maibox
     *            the mailbox to be deleted.
     */
    public void deleteMailbox(Mailbox mailbox) {
        for (final Feed feed : mailbox.getFeeds()) {
            this.dataFacade.deleteFeed(feed);
        }

        for (final Mail mail : mailbox.getMails()) {
            this.dataFacade.deleteMail(mail);
        }

        for (final Contact contact : mailbox.getContacts()) {
            this.dataFacade.deleteContact(contact);
        }

        this.dataFacade.deleteMailbox(mailbox);
    }

    /**
     * Delete a user.
     * 
     * @param user
     *            the user to be deleted.
     */
    public void deleteUser(User user) {
        // Delete the user and its mailboxes.
        final List<Mailbox> mailboxes = getMailboxes(user);
        for (final Mailbox mailbox : mailboxes) {
            deleteMailbox(mailbox);
        }
        this.dataFacade.deleteUser(user);
    }

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *            the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public Contact getContactById(String contactId) {
        return this.dataFacade.getContactById(contactId);
    }

    /**
     * Get a feed by its id.
     * 
     * @param feedId
     *            the feed's id.
     * @return a Feed object or null if no feed has been found.
     */
    public Feed getFeedById(String feedId) {
        return this.dataFacade.getFeedById(feedId);
    }

    /**
     * Get a mailbox by its id.
     * 
     * @param mailboxId
     *            the mailbox's id.
     * @return a Mailbox object or null if no mailbox has been found.
     */
    public Mailbox getMailboxById(String mailboxId) {
        return this.dataFacade.getMailboxById(mailboxId);
    }

    /**
     * Get the list of all mailboxes.
     * 
     * @return the list of all mailboxes.
     */
    public List<Mailbox> getMailboxes() {
        return this.dataFacade.getMailboxes();
    }

    /**
     * Get the list of mailboxes owned by a given user.
     * 
     * @param user
     *            the owner.
     * @return the list of mailboxes owned by this user.
     */
    public List<Mailbox> getMailboxes(User user) {
        return this.dataFacade.getMailboxes(user);
    }

    /**
     * Get a mail by its id.
     * 
     * @param mailId
     *            the mail's id.
     * @return a Mail object or null if no mail has been found.
     */
    public Mail getMailById(String mailId) {
        return this.dataFacade.getMailById(mailId);
    }

    /**
     * Get a user by its id.
     * 
     * @param userId
     *            the user's id.
     * @return a User object or null if no user has been found.
     */
    public User getUserById(String userId) {
        return this.dataFacade.getUserById(userId);
    }

    /**
     * Get a user by its login and password.
     * 
     * @param login
     *            the user's id.
     * @param password
     *            the user's password.
     * @return a User object or null if no user has been found.
     */
    public User getUserByLoginPwd(String login, char[] password) {
        return this.dataFacade.getUserByLoginPwd(login, password);
    }

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
    public List<User> getUsers() {
        return this.dataFacade.getUsers();
    }

    /**
     * Check that at least one administrator is declared in the database.
     * Otherwise add a new one.
     * 
     */
    public void initAdmin() {
        this.dataFacade.initAdmin();
    }

    /**
     * Returns the contact from a mailbox's list of contacts according to a
     * given mail address.
     * 
     * @param mailboxAddress
     *            The mail address to check.
     * @param mailbox
     *            The mailbox that contains the list of contacts
     * @return null if the contact is not found, the contact otherwise.
     */
    public Contact lookForContact(String mailboxAddress, Mailbox mailbox) {
        Contact contact = null;
        if (mailbox.getContacts() != null) {
            for (final Contact item : mailbox.getContacts()) {
                if (item.getMailAddress().equals(mailboxAddress)) {
                    contact = item;
                    break;
                }
            }
        }
        return contact;
    }

    /**
     * Update a contact.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param contact
     *            the contact to be update.
     */
    public void updateContact(Mailbox mailbox, Contact contact) {
        this.dataFacade.updateContact(contact);
    }

    /**
     * Update a feed.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param feed
     *            the feed to be updated.
     */
    public void updateFeed(Mailbox mailbox, Feed feed) {
        this.dataFacade.updateFeed(feed);
    }

    /**
     * Update a mail.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param mail
     *            the mail to be updated.
     */
    public void updateMail(Mailbox mailbox, Mail mail) {
        this.dataFacade.updateMail(mail);
    }

    /**
     * Update a mail.
     * 
     * @param mailbox
     *            the parent mailbox.
     * @param mail
     *            the mail to be updated.
     * @param status
     *            the new status.
     * @param subject
     *            the new subject.
     * @param message
     *            the new message.
     * @param mailAddresses
     *            the new list af mail addresses.
     * @param tags
     *            the nex list of tags.
     * @return the updated mail object.
     */
    public Mail updateMail(Mailbox mailbox, Mail mail, String status,
            String subject, String message, List<String> mailAddresses,
            List<String> tags) {
        mail.setSubject(subject);
        mail.setMessage(message);
        mail.setTags(tags);

        if (Mail.STATUS_DRAFT.equalsIgnoreCase(mail.getStatus())) {
            final List<Contact> mailRecipients = new ArrayList<Contact>();
            if (mailAddresses != null) {
                // First, remove the unused mail's recipients
                if (mail.getRecipients() != null) {
                    for (final Contact recipient : mail.getRecipients()) {
                        if (!mailAddresses.contains(recipient.getMailAddress())) {
                            this.dataFacade.deleteContact(recipient);
                        }
                    }
                }

                // Synchronize the list of mail's list of recipients
                for (final String mailAddress : mailAddresses) {
                    // First, check in the mailbox's list of contacts
                    final Contact contact = lookForContact(mailAddress, mailbox);
                    if (contact != null) {
                        mailRecipients.add(contact);
                    } else {
                        // Look for the contact from the mail's list of
                        // recipients.
                        for (final Contact recipient : mail.getRecipients()) {
                            if (recipient.getMailAddress().equals(mailAddress)) {
                                mailRecipients.add(recipient);
                                break;
                            }
                        }
                    }
                }
                mail.setRecipients(mailRecipients);
            } else {
                // First, remove the unused mail's recipients
                if (mail.getRecipients() != null) {
                    for (final Contact recipient : mail.getRecipients()) {
                        this.dataFacade.deleteContact(recipient);
                    }
                }
                mail.setRecipients(null);
            }
        }

        mail.setStatus(status);

        updateMail(mailbox, mail);
        return mail;
    }

    /**
     * Update a mailbox.
     * 
     * @param maibox
     *            the mailbox to be updated.
     */
    public void updateMailbox(Mailbox mailbox) {
        this.dataFacade.updateMailbox(mailbox);
    }

    /**
     * Update a user.
     * 
     * @param user
     *            the user to be upated.
     */
    public void updateUser(User user) {
        this.dataFacade.updateUser(user);
    }

}
