/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

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
