package org.restlet.example.book.restlet.ch8.objects;

import java.util.ArrayList;
import java.util.List;

import org.restlet.example.book.restlet.ch8.data.DataFacade;

public class ObjectsFacade {

    /** Data facade. */
    protected DataFacade dataFacade;

    public ObjectsFacade(DataFacade dataFacade) {
        super();
        this.dataFacade = dataFacade;
    }

    /**
     * Check that at least one administrator is declared in the database.
     * Otherwise add a new one.
     * 
     */
    public void initAdmin() {
        dataFacade.initAdmin();
    }

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *                the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public Contact getContactById(String contactId) {
        return dataFacade.getContactById(contactId);
    }

    /**
     * Get a feed by its id.
     * 
     * @param feedId
     *                the feed's id.
     * @return a Feed object or null if no feed has been found.
     */
    public Feed getFeedById(String feedId) {
        return dataFacade.getFeedById(feedId);
    }

    /**
     * Get a mail by its id.
     * 
     * @param mailId
     *                the mail's id.
     * @return a Mail object or null if no mail has been found.
     */
    public Mail getMailById(String mailId) {
        return dataFacade.getMailById(mailId);
    }

    /**
     * Add a new User object in the database.
     * 
     * @param user
     *                new User object to be added.
     * @return the user object completed with its identfiant.
     */
    public User createUser(User user) {
        return dataFacade.createUser(user);
    }

    /**
     * Get a user by its id.
     * 
     * @param userId
     *                the user's id.
     * @return a User object or null if no user has been found.
     */
    public User getUserById(String userId) {
        return dataFacade.getUserById(userId);
    }

    /**
     * Get a user by its login and password.
     * 
     * @param login
     *                the user's id.
     * @param password
     *                the user's password.
     * @return a User object or null if no user has been found.
     */
    public User getUserByLoginPwd(String login, char[] password) {
        return dataFacade.getUserByLoginPwd(login, password);
    }

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
    public List<User> getUsers() {
        return dataFacade.getUsers();
    }

    /**
     * Delete a user.
     * 
     * @param user
     *                the user to be deleted.
     */
    public void deleteUser(User user) {
        // Delete the user and its mailboxes.
        List<Mailbox> mailboxes = getMailboxes(user);
        for (Mailbox mailbox : mailboxes) {
            deleteMailbox(mailbox);
        }
        dataFacade.deleteUser(user);
    }

    /**
     * Update a user.
     * 
     * @param user
     *                the user to be upated.
     */
    public void updateUser(User user) {
        dataFacade.updateUser(user);
    }

    /**
     * Get the list of mailboxes owned by a given user.
     * 
     * @param user
     *                the owner.
     * @return the list of mailboxes owned by this user.
     */
    public List<Mailbox> getMailboxes(User user) {
        return dataFacade.getMailboxes(user);
    }

    /**
     * Add a new Mailbox object in the database.
     * 
     * @param mailbox
     *                new Mailbox object to be added.
     * @return the mailbox object completed with its identfiant.
     */
    public Mailbox createMailbox(Mailbox mailbox) {
        return dataFacade.createMailbox(mailbox);
    }

    /**
     * Get a mailbox by its id.
     * 
     * @param mailboxId
     *                the mailbox's id.
     * @return a Mailbox object or null if no mailbox has been found.
     */
    public Mailbox getMailboxById(String mailboxId) {
        return dataFacade.getMailboxById(mailboxId);
    }

    /**
     * Get the list of all mailboxes.
     * 
     * @return the list of all mailboxes.
     */
    public List<Mailbox> getMailboxes() {
        return dataFacade.getMailboxes();
    }

    /**
     * Delete a mailbox.
     * 
     * @param maibox
     *                the mailbox to be deleted.
     */
    public void deleteMailbox(Mailbox mailbox) {
        for (Feed feed : mailbox.getFeeds()) {
            dataFacade.deleteFeed(feed);
        }

        for (Mail mail : mailbox.getMails()) {
            dataFacade.deleteMail(mail);
        }

        for (Contact contact : mailbox.getContacts()) {
            dataFacade.deleteContact(contact);
        }

        dataFacade.deleteMailbox(mailbox);
    }

    /**
     * Update a mailbox.
     * 
     * @param maibox
     *                the mailbox to be updated.
     */
    public void updateMailbox(Mailbox mailbox) {
        dataFacade.updateMailbox(mailbox);
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
        contact = dataFacade.createContact(contact);
        mailbox.getContacts().add(contact);
        dataFacade.updateMailbox(mailbox);

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

        dataFacade.deleteContact(contact);
        dataFacade.updateMailbox(mailbox);
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
        dataFacade.updateContact(contact);
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
        feed = dataFacade.createFeed(feed);
        mailbox.getFeeds().add(feed);
        dataFacade.updateMailbox(mailbox);

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
        // Remove the feed from the mailbox's list of feeds.
        boolean found = false;
        for (int i = 0; i < mailbox.getFeeds().size() && !found; i++) {
            Feed feed2 = mailbox.getFeeds().get(i);
            if (feed2.getId().equals(feed.getId())) {
                mailbox.getFeeds().remove(i);
                found = true;
            }
        }

        dataFacade.deleteFeed(feed);
        dataFacade.updateMailbox(mailbox);
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
        dataFacade.updateFeed(feed);
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
        mail = dataFacade.createMail(mail);
        mailbox.getMails().add(mail);
        dataFacade.updateMailbox(mailbox);

        return mail;
    }

    /**
     * Delete a mail from a mailbox.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param mail
     *                the mail to be deleted.
     */
    public void deleteMail(Mailbox mailbox, Mail mail) {
        // Remove the mail from the mailbox's list of mails.
        boolean found = false;
        for (int i = 0; i < mailbox.getMails().size() && !found; i++) {
            Mail mail2 = mailbox.getMails().get(i);
            if (mail2.getId().equals(mail.getId())) {
                mailbox.getMails().remove(i);
                found = true;
            }
        }

        // Delete its list of unregistered recipients
        for (Contact contact : mail.getRecipients()) {
            if (contact.getId() == null) {
                dataFacade.deleteContact(contact);
            }
        }
        // Delete its list of sender if not registered.
        if (mail.getSender().getId() == null) {
            dataFacade.deleteContact(mail.getSender());
        }

        dataFacade.deleteMail(mail);
        dataFacade.updateMailbox(mailbox);
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
        dataFacade.updateMail(mail);
    }

    /**
     * Update a mail.
     * 
     * @param mailbox
     *                the parent mailbox.
     * @param mail
     *                the mail to be updated.
     * @param status
     *                the new status.
     * @param subject
     *                the new subject.
     * @param message
     *                the new message.
     * @param mailAddresses
     *                the new list af mail addresses.
     * @param tags
     *                the nex list of tags.
     * @return the updated mail object.
     */
    public Mail updateMail(Mailbox mailbox, Mail mail, String status,
            String subject, String message, List<String> mailAddresses,
            List<String> tags) {
        mail.setSubject(subject);
        mail.setMessage(message);
        mail.setTags(tags);

        if (Mail.STATUS_DRAFT.equalsIgnoreCase(mail.getStatus())) {
            List<Contact> mailRecipients = new ArrayList<Contact>();
            if (mailAddresses != null) {
                // First, remove the unused mail's recipients
                if (mail.getRecipients() != null) {
                    for (Contact recipient : mail.getRecipients()) {
                        if (!mailAddresses.contains(recipient.getMailAddress())) {
                            dataFacade.deleteContact(recipient);
                        }
                    }
                }

                // Synchronize the list of mail's list of recipients
                for (String mailAddress : mailAddresses) {
                    // First, check in the mailbox's list of contacts
                    Contact contact = lookForContact(mailAddress, mailbox);
                    if (contact != null) {
                        mailRecipients.add(contact);
                    } else {
                        // Look for the contact from the mail's list of
                        // recipients.
                        for (Contact recipient : mail.getRecipients()) {
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
                    for (Contact recipient : mail.getRecipients()) {
                        dataFacade.deleteContact(recipient);
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
     * Returns the contact from a mailbox's list of contacts according to a
     * given mail address.
     * 
     * @param mailboxAddress
     *                The mail address to check.
     * @param mailbox
     *                The mailbox that contains the list of contacts
     * @return null if the contact is not found, the contact otherwise.
     */
    public Contact lookForContact(String mailboxAddress, Mailbox mailbox) {
        Contact contact = null;
        if (mailbox.getContacts() != null) {
            for (Contact item : mailbox.getContacts()) {
                if (item.getMailAddress().equals(mailboxAddress)) {
                    contact = item;
                    break;
                }
            }
        }
        return contact;
    }

}
