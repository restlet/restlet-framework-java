/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
