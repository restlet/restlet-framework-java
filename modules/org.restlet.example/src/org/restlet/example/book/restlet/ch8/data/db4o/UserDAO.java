package org.restlet.example.book.restlet.ch8.data.db4o;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.example.book.restlet.ch8.objects.User;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.db4o.query.QueryComparator;

/**
 * DAO that manages the persistence of User objects.
 * 
 */
public class UserDAO extends Db4oDAO {

    public UserDAO(ObjectContainer objectContainer) {
        super(objectContainer);
    }

    /**
     * Check that at least one administrator is declared in the database.
     * Otherwise add a new one.
     * 
     */
    public void initAdmin() {
        User prototype = new User();
        prototype.setAdministrator(true);
        ObjectSet<User> result = objectContainer.queryByExample(prototype);

        if (result.isEmpty()) {
            prototype.setFirstName("admin");
            prototype.setLastName("admin");
            prototype.setLogin("admin");
            prototype.setPassword("admin");
            prototype.setAdministrator(true);

            createUser(prototype);
        }
    }

    /**
     * Add a new User object in the database.
     * 
     * @param user
     *                new User object to be added.
     * @return the user object completed with its identfiant.
     */
    public User createUser(User user) {
        user.setId(Long.toString(new Date().getTime()));
        objectContainer.store(user);
        objectContainer.commit();

        return user;
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
        User prototype = new User();
        prototype.setLogin(login);
        prototype.setPassword(new String(password));

        return getUser(prototype);
    }

    /**
     * Get a user by its id.
     * 
     * @param userId
     *                the user's id.
     * @return a User object or null if no user has been found.
     */
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

    /**
     * Get the list of all users.
     * 
     * @return the list of all users.
     */
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

    /**
     * Delete a user.
     * 
     * @param user
     *                the user to be deleted.
     */
    public void deleteUser(User user) {
        objectContainer.delete(user);
        objectContainer.commit();
    }

    /**
     * Update a user.
     * 
     * @param user
     *                the user to be upated.
     */
    public void updateUser(User user) {
        objectContainer.store(user);
        objectContainer.commit();
    }

    /**
     * Get the list of mailboxes owned by a given user.
     * 
     * @param user
     *                the owner.
     * @return the list of mailboxes owned by this user.
     */
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
