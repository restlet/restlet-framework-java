/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.example.book.restlet.ch9.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Mailbox.
 * 
 */
public class MailBox extends BaseObject {
    /** List of contacts of the mailbox. */
    private List<Contact> contacts;

    /** List of feed of the mailbox. */
    private List<Feed> feeds;

    /** List of mails of the mailbox. */
    private List<Mail> mails;

    public MailBox() {
        super();
        this.contacts = new ArrayList<Contact>();
        this.feeds = new ArrayList<Feed>();
        this.mails = new ArrayList<Mail>();
    }

    /** Owner of the mailbox. */
    private User owner;

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Feed> feeds) {
        this.feeds = feeds;
    }

    public List<Mail> getMails() {
        return mails;
    }

    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

}
