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

package org.restlet.example.book.restlet.ch08.gae.common;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * List of contacts.
 */
public class ContactsRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<ContactRepresentation> contacts;

    public ContactsRepresentation() {
        this.contacts = new ArrayList<ContactRepresentation>();
    }

    public ArrayList<ContactRepresentation> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<ContactRepresentation> contacts) {
        this.contacts = contacts;
    }

}
