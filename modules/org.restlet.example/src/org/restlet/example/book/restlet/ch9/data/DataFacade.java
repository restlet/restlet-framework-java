package org.restlet.example.book.restlet.ch9.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.example.book.restlet.ch9.dao.DAOFactory;
import org.restlet.example.book.restlet.ch9.dao.db4o.ContactDAO;
import org.restlet.example.book.restlet.ch9.dao.db4o.FeedDAO;
import org.restlet.example.book.restlet.ch9.dao.db4o.MailDAO;
import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Feed;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.example.book.restlet.ch9.objects.User;

public class DataFacade {
    /** DAO objects factory. */
    protected DAOFactory daoFactory;

    public DataFacade(DAOFactory daoFactory) {
        super();
        this.daoFactory = daoFactory;
    }

    /**
     * Check that at least one administrator is declared in the database.
     * Otherwise add a new one.
     * 
     */
    public void initAdmin() {
        daoFactory.getUserDAO().initAdmin();
    }

    /**
     * Get a contact by its id.
     * 
     * @param contactId
     *                the contact's id.
     * @return a Contact object or null if no contact has been found.
     */
    public Contact getContactById(String contactId) {
        return daoFactory.getContactDAO().getContactById(contactId);
    }

    /**
     * Get a feed by its id.
     * 
     * @param feedId
     *                the feed's id.
     * @return a Feed object or null if no feed has been found.
     */
    public Feed getFeedById(String feedId) {
        return daoFactory.getFeedDAO().getFeedById(feedId);
    }

    /**
     * Get a mail by its id.
     * 
     * @param mailId
     *                the mail's id.
     * @return a Mail object or null if no mail has been found.
     */
    public Mail getMailById(String mailId) {
        return daoFactory.getMailDAO().getMailById(mailId);
    }

    /**
     * Add a new User object in the database.
     * 
     * @param user
     *                new User object to be added.
     * @return the user object completed with its identfiant.
     */
    public User createUser(User user) {
        return daoFactory.getUserDAO().createUser(user);
    }

    /**
     * Get a user by its id.
     * 
     * @param userId
     *                the user's id.
     * @return a User object or null if no user has been found.
     */
    public User getUserById(String userId) {
        return daoFactory.getUserDAO().getUserById(userId);
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
        return daoFactory.getUserDAO().getUserByLoginPwd(login, password);
    }

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
    public List<User> getUsers() {
        return daoFactory.getUserDAO().getUsers();
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
        daoFactory.getUserDAO().deleteUser(user);
    }

    /**
     * Update a user.
     * 
     * @param user
     *                the user to be upated.
     */
    public void updateUser(User user) {
        daoFactory.getUserDAO().updateUser(user);
    }

    /**
     * Get the list of mailboxes owned by a given user.
     * 
     * @param user
     *                the owner.
     * @return the list of mailboxes owned by this user.
     */
    public List<Mailbox> getMailboxes(User user) {
        return daoFactory.getUserDAO().getMailboxes(user);
    }

    /**
     * Add a new Mailbox object in the database.
     * 
     * @param mailbox
     *                new Mailbox object to be added.
     * @return the mailbox object completed with its identfiant.
     */
    public Mailbox createMailbox(Mailbox mailbox) {
        return daoFactory.getMailboxDAO().createMailbox(mailbox);
    }

    /**
     * Get a mailbox by its id.
     * 
     * @param mailboxId
     *                the mailbox's id.
     * @return a Mailbox object or null if no mailbox has been found.
     */
    public Mailbox getMailboxById(String mailboxId) {
        return daoFactory.getMailboxDAO().getMailboxById(mailboxId);
    }

    /**
     * Get the list of all mailboxes.
     * 
     * @return the list of all mailboxes.
     */
    public List<Mailbox> getMailboxes() {
        return daoFactory.getMailboxDAO().getMailboxes();
    }

    /**
     * Delete a mailbox.
     * 
     * @param maibox
     *                the mailbox to be deleted.
     */
    public void deleteMailbox(Mailbox mailbox) {
        FeedDAO feedDAO = daoFactory.getFeedDAO();
        for (Feed feed : mailbox.getFeeds()) {
            feedDAO.deleteFeed(feed);
        }
        MailDAO mailDAO = daoFactory.getMailDAO();
        for (Mail mail : mailbox.getMails()) {
            mailDAO.deleteMail(mail);
        }
        ContactDAO contactDAO = daoFactory.getContactDAO();
        for (Contact contact : mailbox.getContacts()) {
            contactDAO.deleteContact(contact);
        }

        daoFactory.getMailboxDAO().deleteMailbox(mailbox);
    }

    /**
     * Update a mailbox.
     * 
     * @param maibox
     *                the mailbox to be updated.
     */
    public void updateMailbox(Mailbox mailbox) {
        daoFactory.getMailboxDAO().updateMailbox(mailbox);
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
        contact = daoFactory.getContactDAO().createContact(contact);
        mailbox.getContacts().add(contact);
        daoFactory.getMailboxDAO().updateMailbox(mailbox);

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

        daoFactory.getContactDAO().deleteContact(contact);
        daoFactory.getMailboxDAO().updateMailbox(mailbox);
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
        daoFactory.getContactDAO().updateContact(contact);
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
        feed = daoFactory.getFeedDAO().createFeed(feed);
        mailbox.getFeeds().add(feed);
        daoFactory.getMailboxDAO().updateMailbox(mailbox);

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

        daoFactory.getFeedDAO().deleteFeed(feed);
        daoFactory.getMailboxDAO().updateMailbox(mailbox);
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
        daoFactory.getFeedDAO().updateFeed(feed);
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
        mail = daoFactory.getMailDAO().createMail(mail);
        mailbox.getMails().add(mail);
        daoFactory.getMailboxDAO().updateMailbox(mailbox);

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
        ContactDAO contactDAO = daoFactory.getContactDAO();
        // Delete its list of unregistered recipients
        for (Contact contact : mail.getRecipients()) {
            if (contact.getId() == null) {
                contactDAO.deleteContact(contact);
            }
        }
        // Delete its list of sender if not registered.
        if (mail.getSender().getId() == null) {
            contactDAO.deleteContact(mail.getSender());
        }

        daoFactory.getMailDAO().deleteMail(mail);
        daoFactory.getMailboxDAO().updateMailbox(mailbox);
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
        daoFactory.getMailDAO().updateMail(mail);
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
                ContactDAO contactDAO = daoFactory.getContactDAO();
                if (mail.getRecipients() != null) {
                    for (Contact recipient : mail.getRecipients()) {
                        if (!mailAddresses.contains(recipient.getMailAddress())) {
                            contactDAO.deleteContact(recipient);
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
                ContactDAO contactDAO = daoFactory.getContactDAO();
                if (mail.getRecipients() != null) {
                    for (Contact recipient : mail.getRecipients()) {
                        contactDAO.deleteContact(recipient);
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
