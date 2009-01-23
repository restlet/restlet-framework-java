/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

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
 */
public class Db4oFacade extends DataFacade {

    /** Db4o object container. */
    protected ObjectContainer objectContainer;

    /**
     * Constructor with the path to the db file.
     * 
     * @param db4oFilePath
     *            the path to the db file.
     */
    public Db4oFacade(String db4oFilePath) {
        super();

        final Configuration config = Db4o.newConfiguration();
        config.updateDepth(2);
        config.activationDepth(10);

        this.objectContainer = Db4o.openFile(db4oFilePath);
    }

    @Override
    public Contact createContact(Contact contact) {
        contact.setId(Long.toString(new Date().getTime()));
        this.objectContainer.store(contact);
        this.objectContainer.commit();

        return contact;
    }

    @Override
    public Feed createFeed(Feed feed) {
        feed.setId(Long.toString(new Date().getTime()));
        this.objectContainer.store(feed);
        this.objectContainer.commit();

        return feed;
    }

    @Override
    public Mail createMail(Mail mail) {
        mail.setId(Long.toString(new Date().getTime()));
        this.objectContainer.store(mail);
        this.objectContainer.commit();

        return mail;
    }

    @Override
    public void createMailbox(Mailbox mailbox) {
        this.objectContainer.store(mailbox);
        this.objectContainer.commit();
    }

    @Override
    public void createUser(User user) {
        this.objectContainer.store(user);
        this.objectContainer.commit();
    }

    @Override
    public void deleteContact(Contact contact) {
        this.objectContainer.delete(contact);
        this.objectContainer.commit();
    }

    @Override
    public void deleteFeed(Feed feed) {
        this.objectContainer.delete(feed);
        this.objectContainer.commit();
    }

    @Override
    public void deleteMail(Mail mail) {
        this.objectContainer.delete(mail);
        this.objectContainer.commit();
    }

    @Override
    public void deleteMailbox(Mailbox mailbox) {
        this.objectContainer.delete(mailbox);
        this.objectContainer.commit();
    }

    @Override
    public void deleteUser(User user) {
        this.objectContainer.delete(user);
        this.objectContainer.commit();
    }

    /**
     * Get a contact according to a prototype.
     * 
     * @param prototype
     *            the prototype.
     * @return a Contact object or null if no contact has been found.
     */
    private Contact getContact(Contact prototype) {
        Contact contact = null;
        final ObjectSet<Contact> result = this.objectContainer
                .queryByExample(prototype);

        if (!result.isEmpty()) {
            contact = result.get(0);
        }

        return contact;
    }

    @Override
    public Contact getContactById(String contactId) {
        final Contact prototype = new Contact();
        prototype.setId(contactId);

        return getContact(prototype);
    }

    /**
     * Get a feed according to a prototype.
     * 
     * @param prototype
     *            the prototype.
     * @return a Feed object or null if no feed has been found.
     */
    private Feed getFeed(Feed prototype) {
        Feed feed = null;
        final ObjectSet<Feed> result = this.objectContainer
                .queryByExample(prototype);

        if (!result.isEmpty()) {
            feed = result.get(0);
        }

        return feed;
    }

    @Override
    public Feed getFeedById(String feedId) {
        final Feed prototype = new Feed();
        prototype.setId(feedId);

        return getFeed(prototype);
    }

    /**
     * Get a mail according to a prototype.
     * 
     * @param prototype
     *            the prototype.
     * @return a Mail object or null if no mail has been found.
     */
    private Mail getMail(Mail prototype) {
        Mail mail = null;
        final ObjectSet<Mail> result = this.objectContainer
                .queryByExample(prototype);

        if (!result.isEmpty()) {
            mail = result.get(0);
        }

        return mail;
    }

    /**
     * Get a mailbox according to a prototype.
     * 
     * @param prototype
     *            the prototype.
     * @return a Mailbox object or null if no mailbox has been found.
     */
    private Mailbox getMailbox(Mailbox prototype) {
        Mailbox mailbox = null;
        final ObjectSet<Mailbox> result = this.objectContainer
                .queryByExample(prototype);

        if (!result.isEmpty()) {
            mailbox = result.get(0);
        }

        return mailbox;
    }

    @Override
    public Mailbox getMailboxById(String mailboxId) {
        final Mailbox prototype = new Mailbox();
        prototype.setId(mailboxId);

        return getMailbox(prototype);
    }

    @Override
    public List<Mailbox> getMailboxes() {
        // Get all mailboxes
        final Predicate<Mailbox> predicate = new Predicate<Mailbox>() {
            static final long serialVersionUID = 1l;

            @Override
            public boolean match(Mailbox arg0) {
                return true;
            }
        };
        // Sort by owner name
        final QueryComparator<Mailbox> comparator = new QueryComparator<Mailbox>() {
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

        final List<Mailbox> result = new ArrayList<Mailbox>();
        final ObjectSet<Mailbox> list = this.objectContainer.query(predicate,
                comparator);
        result.addAll(list);
        return result;
    }

    @Override
    public List<Mailbox> getMailboxes(User user) {
        final String userId = user.getId();
        final Predicate<Mailbox> predicate = new Predicate<Mailbox>() {
            static final long serialVersionUID = 1l;

            @Override
            public boolean match(Mailbox mailbox) {
                return mailbox.getOwner().getId().equals(userId);
            }
        };
        return new ArrayList<Mailbox>(this.objectContainer.query(predicate));
    }

    @Override
    public Mail getMailById(String mailId) {
        final Mail prototype = new Mail();
        prototype.setId(mailId);

        return getMail(prototype);
    }

    /**
     * Get a user according to a prototype.
     * 
     * @param prototype
     *            the prototype.
     * @return a User object or null if no user has been found.
     */
    private User getUser(User prototype) {
        User user = null;
        final ObjectSet<User> result = this.objectContainer
                .queryByExample(prototype);

        if (!result.isEmpty()) {
            user = result.get(0);
        }

        return user;
    }

    @Override
    public User getUserById(String userId) {
        final User prototype = new User();
        prototype.setId(userId);

        return getUser(prototype);
    }

    @Override
    public User getUserByLoginPwd(String login, char[] password) {
        final User prototype = new User();
        prototype.setLogin(login);
        prototype.setPassword(new String(password));

        return getUser(prototype);
    }

    @Override
    public List<User> getUsers() {
        // Get all users
        final Predicate<User> predicate = new Predicate<User>() {
            static final long serialVersionUID = 1l;

            @Override
            public boolean match(User arg0) {
                return true;
            }
        };
        // Sort by last name
        final QueryComparator<User> comparator = new QueryComparator<User>() {
            static final long serialVersionUID = 1l;

            public int compare(User arg0, User arg1) {
                return arg0.getLastName().compareToIgnoreCase(
                        arg1.getLastName());
            }

        };
        final List<User> result = new ArrayList<User>();
        final ObjectSet<User> list = this.objectContainer.query(predicate,
                comparator);
        result.addAll(list);
        return result;
    }

    @Override
    public void initAdmin() {
        final User prototype = new User();
        prototype.setAdministrator(true);
        final ObjectSet<User> result = this.objectContainer
                .queryByExample(prototype);

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
    public void updateContact(Contact contact) {
        this.objectContainer.store(contact);
        this.objectContainer.commit();
    }

    @Override
    public void updateFeed(Feed feed) {
        this.objectContainer.store(feed);
        this.objectContainer.commit();
    }

    @Override
    public void updateMail(Mail mail) {
        this.objectContainer.store(mail);
        this.objectContainer.commit();
    }

    @Override
    public void updateMailbox(Mailbox mailbox) {
        this.objectContainer.store(mailbox);
        this.objectContainer.commit();
    }

    @Override
    public void updateUser(User user) {
        this.objectContainer.store(user);
        this.objectContainer.commit();
    }

}
