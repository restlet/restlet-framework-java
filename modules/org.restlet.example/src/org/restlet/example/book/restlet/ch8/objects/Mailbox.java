/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.example.book.restlet.ch8.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Mailbox seen as a list of mails, contacts and feeds.
 */
public class Mailbox extends BaseObject {

    /** List of contacts of the mailbox. */
    private List<Contact> contacts;

    /** List of feed of the mailbox. */
    private List<Feed> feeds;

    /** List of mails of the mailbox. */
    private List<Mail> mails;

    /** Nickname of the mailbox. */
    private String nickname;

    /** Owner of the mailbox. */
    private User owner;

    /** Identity of the sender. */
    private String senderName;

    public Mailbox() {
        super();
        this.contacts = new ArrayList<Contact>();
        this.feeds = new ArrayList<Feed>();
        this.mails = new ArrayList<Mail>();
    }

    public List<Contact> getContacts() {
        return this.contacts;
    }

    public List<Feed> getFeeds() {
        return this.feeds;
    }

    public List<Mail> getMails() {
        return this.mails;
    }

    public String getNickname() {
        return this.nickname;
    }

    public User getOwner() {
        return this.owner;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

}
