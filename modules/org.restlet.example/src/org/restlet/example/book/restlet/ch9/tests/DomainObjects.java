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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Feed;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.example.book.restlet.ch9.objects.MailRoot;
import org.restlet.example.book.restlet.ch9.objects.User;

public class DomainObjects {
    public static void main(String[] args) {
        DomainObjects dom = new DomainObjects();
    }

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

    /** List of all available statuses. */
    private String[] mailStatuses = { Mail.STATUS_DRAFT, Mail.STATUS_SENDING,
            Mail.STATUS_SENT, Mail.STATUS_RECEIVING, Mail.STATUS_RECEIVED };

    /** List of tags. */
    private List<String> mailTags = Arrays.asList("tag1", "tag2", "tag3");

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
        user.setFirstName("Agathe Zeblues");
        user.setAdministrator(false);
        mailRoot.getUsers().add(user);

        User admin = new User();
        admin.setId("admin");
        admin.setFirstName("admin");
        admin.setAdministrator(true);
        mailRoot.getUsers().add(admin);

        // Add three mailboxes
        mailRoot.getMailboxes().add(createMailbox(user));
        mailRoot.getMailboxes().add(createMailbox(user));
        mailRoot.getMailboxes().add(createMailbox(admin));
    }

    /**
     * Create a new mail box and set its owner.
     * 
     * @param owner
     *                user that owns this mail box.
     * @return A new mailbox.
     */
    private Mailbox createMailbox(User owner) {
        Mailbox mailbox = new Mailbox();
        mailbox.setId(Integer.toString(mailboxSequence++));
        mailbox.setOwner(owner);

        // Create one feed
        int index = Integer.parseInt(mailbox.getId()) % (mailTags.size());
        mailbox.getFeeds().add(createFeed(Arrays.asList(mailTags.get(index))));

        // Create several contacts
        for (int i = 0; i < mailboxSequence; i++) {
            mailbox.getContacts().add(createContact());
        }

        for (int i = 0; i < mailboxSequence; i++) {
            mailbox.getMails()
                    .add(
                            createMail(owner, mailbox.getContacts(), mailbox
                                    .getFeeds()));
        }

        return mailbox;
    }

    /**
     * Create a new contact.
     * 
     * @return A new contact.
     */
    private Contact createContact() {
        Contact contact = new Contact();
        contact.setId(Integer.toString(contactSequence++));
        contact.setName("contact-" + contact.getId());
        contact.setMailAddress("http://rmep.com/contacts/" + contact.getName());
        return contact;
    }

    /**
     * Create a new feed.
     * 
     * @param tags
     *                List of tags supported by this feed.
     * @return A new feed.
     */
    private Feed createFeed(List<String> tags) {
        Feed feed = new Feed();
        feed.setId(Integer.toString(feedSequence++));
        feed.setTags(tags);
        return feed;
    }

    /**
     * Create a new mail given it's sender and recipients.
     * 
     * @param sender
     *                sender of the mail
     * @param recipients
     *                primary recipients of the mail.
     * @param feeds
     *                List of potential matching feeds.
     * @return A new mail.
     */
    private Mail createMail(User sender, List<Contact> recipients,
            List<Feed> feeds) {
        Mail mail = new Mail();
        mail.setId(Integer.toString(mailSequence++));
        mail.setSender(sender);
        mail.setRecipients(new ArrayList<Contact>(recipients.subList(0,
                (Integer.parseInt(mail.getId())) % recipients.size() + 1)));
        mail.setMessage("Cheers -" + sender.getFirstName());
        mail.setSendingDate(new Date());
        mail.setSubject("Hello!");

        // Set the status according to the mail identifiant.
        mail.setStatus(mailStatuses[(Integer.parseInt(mail.getId()))
                % mailStatuses.length]);
        // Set the list of tags according to the mail identifiant.
        mail.setTags(new ArrayList<String>(mailTags.subList(0, (Integer
                .parseInt(mail.getId()))
                % (mailTags.size() + 1))));

        // Add the mail to the appropriate feed.
        if (!mail.getTags().isEmpty()) {
            for (Feed feed : feeds) {
                if (mail.getTags().containsAll(feed.getTags())) {
                    feed.getMails().add(mail);
                }
            }
        }
        return mail;
    }

    public MailRoot getMailRoot() {
        return mailRoot;
    }

}
