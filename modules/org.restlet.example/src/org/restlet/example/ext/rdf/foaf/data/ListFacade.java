package org.restlet.example.ext.rdf.foaf.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.example.ext.rdf.foaf.objects.Contact;
import org.restlet.example.ext.rdf.foaf.objects.User;

public class ListFacade extends DataFacade {

    private Map<String, User> users;

    public ListFacade() {
        super();
        this.users = new HashMap<String, User>();
    }

    @Override
    public Contact createContact(Contact contact) {
        contact.setId(Long.toString(new Date().getTime()));
        return contact;
    }

    @Override
    public User createUser(User user) {
        user.setId(Long.toString(new Date().getTime()));
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
                    result= contact;
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
