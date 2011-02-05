/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.example.ext.rdf.foaf.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * User account.
 */
public class User extends BaseObject {

    /** List of contacts of the user. */
    private List<Contact> contacts;

    /** First name of the user. */
    private String firstName;

    /** Image representation of the user. */
    private String image;

    /** Last name of the user. */
    private String lastName;
    
    public User() {
        super();
    }

    public List<Contact> getContacts() {
        if(contacts == null){
            contacts = new ArrayList<Contact>();
        }
        return contacts;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getImage() {
        return image;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
