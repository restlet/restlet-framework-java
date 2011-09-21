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

package org.restlet.example.book.restlet.ch10.sec3.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Account {

    private String login;

    private String firstName;

    private String lastName;

    private String nickName;

    private String senderName;

    private String emailAddress;

    private List<Contact> contacts;

    private List<Mail> mails;

    private List<Feed> feeds;

    public Account() {
        this.contacts = new CopyOnWriteArrayList<Contact>();
        this.mails = new CopyOnWriteArrayList<Mail>();
        this.feeds = new CopyOnWriteArrayList<Feed>();
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public List<Feed> getFeeds() {
        return feeds;
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

    public List<Mail> getMails() {
        return mails;
    }

    public String getNickName() {
        return nickName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setContacts(List<Contact> contact) {
        this.contacts = contact;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setFeeds(List<Feed> feed) {
        this.feeds = feed;
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

    public void setMails(List<Mail> mail) {
        this.mails = mail;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

}
