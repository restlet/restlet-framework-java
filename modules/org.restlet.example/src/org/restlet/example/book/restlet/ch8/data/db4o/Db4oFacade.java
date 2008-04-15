package org.restlet.example.book.restlet.ch8.data.db4o;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch8.data.DataFacade;
import org.restlet.example.book.restlet.ch8.objects.Contact;
import org.restlet.example.book.restlet.ch8.objects.Feed;
import org.restlet.example.book.restlet.ch8.objects.Mail;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.example.book.restlet.ch8.objects.User;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.query.Predicate;
import com.db4o.query.QueryComparator;

/**
 * Facade that manages the persistence for the DB4O database.
 * 
 */
public class Db4oFacade extends DataFacade {

    /** Db4o object container. */
    protected ObjectContainer objectContainer;

    /**
     * Constructor with the path to the db file.
     * 
     * @param db4oFilePath
     *                the path to the db file.
     */
    public Db4oFacade(String db4oFilePath) {
        super();

        Configuration config = Db4o.configure();
        config.updateDepth(2);
        config.activationDepth(10);

        this.objectContainer = Db4o.openFile(db4oFilePath);
    }

    @Override
    public Contact createContact(Contact contact) {
        contact.setId(Long.toString(new Date().getTime()));
        objectContainer.store(contact);
        objectContainer.commit();

        return contact;
    }

    @Override
    public void deleteContact(Contact contact) {
        objectContainer.delete(contact);
        objectContainer.commit();
    }

    @Override
    public Contact getContactById(String contactId) {
        Contact prototype = new Contact();
        prototype.setId(contactId);

        return getContact(prototype);
    }

    /**
     * Get a contact according to a prototype.
     * 
     * @param prototype
     *                the prototype.
     * @return a Contact object or null if no contact has been found.
     */
    private Contact getContact(Contact prototype) {
        Contact contact = null;
        ObjectSet<Contact> result = objectContainer.queryByExample(prototype);

        if (!result.isEmpty()) {
            contact = result.get(0);
        }

        return contact;
    }

    @Override
    public void updateContact(Contact contact) {
        objectContainer.store(contact);
        objectContainer.commit();
    }

    @Override
    public Feed createFeed(Feed feed) {
        feed.setId(Long.toString(new Date().getTime()));
        objectContainer.store(feed);
        objectContainer.commit();

        return feed;
    }

    @Override
    public Feed getFeedById(String feedId) {
        Feed prototype = new Feed();
        prototype.setId(feedId);

        return getFeed(prototype);
    }

    /**
     * Get a feed according to a prototype.
     * 
     * @param prototype
     *                the prototype.
     * @return a Feed object or null if no feed has been found.
     */
    private Feed getFeed(Feed prototype) {
        Feed feed = null;
        ObjectSet<Feed> result = objectContainer.queryByExample(prototype);

        if (!result.isEmpty()) {
            feed = result.get(0);
        }

        return feed;
    }

    @Override
    public void updateFeed(Feed feed) {
        objectContainer.store(feed);
        objectContainer.commit();
    }

    @Override
    public void deleteFeed(Feed feed) {
        objectContainer.delete(feed);
        objectContainer.commit();
    }

    @Override
    public void createMailbox(Mailbox mailbox) {
        objectContainer.store(mailbox);
        objectContainer.commit();
    }

    @Override
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

    @Override
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

    @Override
    public void updateMailbox(Mailbox mailbox) {
        objectContainer.store(mailbox);
        objectContainer.commit();
    }

    @Override
    public void deleteMailbox(Mailbox mailbox) {
        objectContainer.delete(mailbox);
        objectContainer.commit();
    }

    @Override
    public Mail createMail(Mail mail) {
        mail.setId(Long.toString(new Date().getTime()));
        objectContainer.store(mail);
        objectContainer.commit();

        return mail;
    }

    @Override
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

    @Override
    public void updateMail(Mail mail) {
        objectContainer.store(mail);
        objectContainer.commit();
    }

    @Override
    public void deleteMail(Mail mail) {
        objectContainer.delete(mail);
        objectContainer.commit();
    }

    @Override
    public void initAdmin() {
        User prototype = new User();
        prototype.setAdministrator(true);
        ObjectSet<User> result = objectContainer.queryByExample(prototype);

        if (result.isEmpty()) {
            prototype.setId("admin");
            prototype.setFirstName("admin");
            prototype.setLastName("admin");
            prototype.setLogin("admin");
            prototype.setPassword("admin");
            prototype.setAdministrator(true);

            createUser(prototype);
        }
    }

    @Override
    public void createUser(User user) {
        objectContainer.store(user);
        objectContainer.commit();
    }

    @Override
    public User getUserByLoginPwd(String login, char[] password) {
        User prototype = new User();
        prototype.setLogin(login);
        prototype.setPassword(new String(password));

        return getUser(prototype);
    }

    @Override
    public User getUserById(String userId) {
        User prototype = new User();
        prototype.setId(userId);

        return getUser(prototype);
    }

    /**
     * Get a user according to a prototype.
     * 
     * @param prototype
     *                the prototype.
     * @return a User object or null if no user has been found.
     */
    private User getUser(User prototype) {
        User user = null;
        ObjectSet<User> result = objectContainer.queryByExample(prototype);

        if (!result.isEmpty()) {
            user = result.get(0);
        }

        return user;
    }

    @Override
    public List<User> getUsers() {
        // Get all users
        Predicate<User> predicate = new Predicate<User>() {
            static final long serialVersionUID = 1l;

            @Override
            public boolean match(User arg0) {
                return true;
            }
        };
        // Sort by last name
        QueryComparator<User> comparator = new QueryComparator<User>() {
            static final long serialVersionUID = 1l;

            public int compare(User arg0, User arg1) {
                return arg0.getLastName().compareToIgnoreCase(
                        arg1.getLastName());
            }

        };
        List<User> result = new ArrayList<User>();
        ObjectSet<User> list = objectContainer.query(predicate, comparator);
        result.addAll(list);
        return result;
    }

    @Override
    public void deleteUser(User user) {
        objectContainer.delete(user);
        objectContainer.commit();
    }

    @Override
    public void updateUser(User user) {
        objectContainer.store(user);
        objectContainer.commit();
    }

    @Override
    public List<Mailbox> getMailboxes(User user) {
        final String userId = user.getId();
        Predicate<Mailbox> predicate = new Predicate<Mailbox>() {
            static final long serialVersionUID = 1l;

            @Override
            public boolean match(Mailbox mailbox) {
                return mailbox.getOwner().getId().equals(userId);
            }
        };
        return new ArrayList<Mailbox>(objectContainer.query(predicate));
    }

}
