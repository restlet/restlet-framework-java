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

package org.restlet.example.book.restlet.ch9.tests;

import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Feed;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.MailBox;
import org.restlet.example.book.restlet.ch9.objects.MailRoot;
import org.restlet.example.book.restlet.ch9.objects.User;

public class DomainObjects {

    /** Root object at the top of the hierarchy. */
    private MailRoot mailRoot;

    /** Used to identify contact objects. */
    private int contactSequence;

    /** Used to identify feed objects. */
    private int feedSequence;

    /** Used to identify mail objects. */
    private int mailSequence;

    /** Used to identify mailbox objects. */
    private int mailboxSequence;

    public DomainObjects() {
        super();

        mailRoot = new MailRoot();

        // Initialize sequences.
        contactSequence = 1;
        feedSequence = 1;
        mailSequence = 1;
        mailboxSequence = 1;

        // Add two users.
        User user = new User();
        user.setId("agathe_zeblues");
        user.setName("Agathe Zeblues");
        user.setAdministrator(false);
        mailRoot.getUsers().add(user);

        User admin = new User();
        admin.setId("admin");
        admin.setName("admin");
        admin.setAdministrator(true);
        mailRoot.getUsers().add(admin);

        // Add three mailboxes
        mailRoot.getMailBoxes().add(createMailBox(user));
        mailRoot.getMailBoxes().add(createMailBox(user));
        mailRoot.getMailBoxes().add(createMailBox(admin));
    }

    /**
     * Create a new mail box and set its owner.
     * 
     * @param owner
     *                user that owns this mail box.
     * @return A new mailbox.
     */
    private MailBox createMailBox(User owner) {
        MailBox mailBox = new MailBox();
        mailBox.setId(Integer.toString(mailboxSequence++));
        mailBox.setOwner(owner);

        // Create one feed
        mailBox.getFeeds().add(createFeed());

        // Create several contacts
        for (int i = 0; i < mailboxSequence; i++) {
            mailBox.getContacts().add(createContact());
        }

        for (int i = 0; i < mailboxSequence; i++) {
            mailBox.getMails().add(createMail(owner, mailBox.getContacts()));
        }

        return mailBox;
    }

    /**
     * Create a new contact.
     * 
     * @return A new contact.
     */
    private Contact createContact() {
        Contact contact = new Contact();
        contact.setId(Integer.toString(contactSequence++));
        return contact;
    }

    /**
     * Create a new feed.
     * 
     * @return A new feed.
     */
    private Feed createFeed() {
        Feed feed = new Feed();
        feed.setId(Integer.toString(feedSequence++));
        return feed;
    }

    /**
     * Create a new mail given it's sender and recipients.
     * 
     * @param sender
     *                sender of the mail
     * @param recipients
     *                primary recipients of the mail.
     * @return A new mail.
     */
    private Mail createMail(User sender, List<Contact> recipients) {
        Mail mail = new Mail();
        mail.setId(Integer.toString(mailSequence++));
        mail.setSender(sender);
        mail.setPrimaryRecipients(recipients);
        mail.setMessage("Cheers -" + sender.getName());
        mail.setSendingDate(new Date());
        mail.setSubject("Hello!");
        mail.setStatus("status");
        return mail;
    }

    public MailRoot getMailRoot() {
        return mailRoot;
    }

}
