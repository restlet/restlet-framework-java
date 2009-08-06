package org.restlet.example.ext.rdf.foaf.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.example.ext.rdf.foaf.objects.Contact;
import org.restlet.example.ext.rdf.foaf.objects.User;

public class ListFacade extends DataFacade {

    private ConcurrentMap<String, User> users;

    public ListFacade() {
        super();
        this.users = new ConcurrentHashMap<String, User>();
    }

    @Override
    public Contact createContact(User user, Contact contact) {
        contact.setId(Integer.toString(user.getContacts().size() + 1));
        return contact;
    }

    @Override
    public User createUser(User user) {
        user.setId(Integer.toString(users.size() + 1));
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteContact(Contact contact) {
    }

    @Override
    public void deleteUser(User user) {
        users.remove(user.getId());
    }

    @Override
    public Contact getContactById(String contactId) {
        Contact result = null;
        for (User user : users.values()) {
            for (Contact contact : user.getContacts()) {
                if (contactId.equals(contact.getId())) {
                    result = contact;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public List<Contact> getContacts(User user) {
        return user.getContacts();
    }

    @Override
    public User getUserById(String userId) {
        return users.get(userId);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<User>(users.values());
    }

    @Override
    public void updateContact(Contact contact) {
    }

    @Override
    public void updateUser(User user) {
    }

}
