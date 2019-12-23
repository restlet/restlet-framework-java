/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.example.book.restlet.ch08.gae.server;

import org.restlet.example.book.restlet.ch08.gae.common.ContactRepresentation;
import org.restlet.example.book.restlet.ch08.gae.common.ContactsRepresentation;
import org.restlet.example.book.restlet.ch08.gae.common.ContactsResource;
import org.restlet.ext.wadl.WadlServerResource;

/**
 * Contacts server resource implementing the {@link ContactsResource} interface.
 */
public class ContactsServerResource extends WadlServerResource implements
        ContactsResource {

    public ContactsRepresentation retrieve() {
        ContactsRepresentation contacts = new ContactsRepresentation();
        ContactRepresentation contact = new ContactRepresentation("Homer",
                "Simpson", "homer@simpson.org", "chunkylover53", null,
                "Homer Simpson");
        contacts.getContacts().add(contact);
        contact = new ContactRepresentation("Bartholomew", "Simpson",
                "bart@simpson.org", "jojo10", null, "Bart Simpson");
        contacts.getContacts().add(contact);
        return contacts;
    }

    public void add(ContactRepresentation contact) {
        System.out.println("Email: " + contact.getEmail());
        System.out.println("First name: " + contact.getFirstName());
        System.out.println("Last name: " + contact.getLastName());
        System.out.println("Login: " + contact.getLogin());
        System.out.println("Nick name: " + contact.getNickName());
        System.out.println("Sender name: " + contact.getSenderName());
        System.out.println();
    }

}
