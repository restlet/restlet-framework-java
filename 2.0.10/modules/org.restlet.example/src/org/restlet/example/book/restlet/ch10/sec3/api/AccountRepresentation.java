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

package org.restlet.example.book.restlet.ch10.sec3.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("account")
public class AccountRepresentation {

    private volatile String login;

    private volatile String firstName;

    private volatile String lastName;

    private volatile String nickName;

    private volatile String senderName;

    private volatile String emailAddress;

    @XStreamImplicit
    private volatile List<String> contactRefs;

    public AccountRepresentation() {
        this.contactRefs = new CopyOnWriteArrayList<String>();
    }

    public List<String> getContactRefs() {
        return contactRefs;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLogin() {
        return login;
    }

    public String getNickName() {
        return nickName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setContactRefs(List<String> contactRefs) {
        this.contactRefs = contactRefs;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

}
