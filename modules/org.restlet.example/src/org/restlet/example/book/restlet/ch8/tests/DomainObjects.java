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

package org.restlet.example.book.restlet.ch8.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.restlet.example.book.restlet.ch8.objects.Contact;
import org.restlet.example.book.restlet.ch8.objects.Feed;
import org.restlet.example.book.restlet.ch8.objects.Mail;
import org.restlet.example.book.restlet.ch8.objects.MailRoot;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.example.book.restlet.ch8.objects.User;

/**
 *
 */
public class DomainObjects {

    /** Root object at the top of the hierarchy. */
    private final MailRoot mailRoot;

    /** Used to identify contact objects. */
    private int contactSequence;

    /** Used to identify feed objects. */
    private int feedSequence;

    /** Used to identify mail objects. */
    private int mailSequence;

    /** Used to identify mailbox objects. */
    private int mailboxSequence;

    /** List of all available statuses. */
    private final String[] mailStatuses = { Mail.STATUS_DRAFT,
            Mail.STATUS_SENDING, Mail.STATUS_SENT, Mail.STATUS_RECEIVING,
            Mail.STATUS_RECEIVED };

    /** List of tags. */
    private final List<String> mailTags = Arrays.asList("tag1", "tag2", "tag3");

    public DomainObjects() {
        super();

        this.mailRoot = new MailRoot();

        // Initialize sequences.
        this.contactSequence = 1;
        this.feedSequence = 1;
        this.mailSequence = 1;
        this.mailboxSequence = 1;

        // Add two users.
        final User user = new User();
        user.setId("agathe_zeblues");
        user.setFirstName("Agathe Zeblues");
        user.setAdministrator(false);
        this.mailRoot.getUsers().add(user);

        final User admin = new User();
        admin.setId("admin");
        admin.setFirstName("admin");
        admin.setAdministrator(true);
        this.mailRoot.getUsers().add(admin);

        // Add three mailboxes
        this.mailRoot.getMailboxes().add(createMailbox(user));
        this.mailRoot.getMailboxes().add(createMailbox(user));
        this.mailRoot.getMailboxes().add(createMailbox(admin));
    }

    /**
     * Create a new contact.
     * 
     * @return A new contact.
     */
    private Contact createContact() {
        final Contact contact = new Contact();
        contact.setId(Integer.toString(this.contactSequence++));
        contact.setName("contact-" + contact.getId());
        contact.setMailAddress("http://rmep.com/contacts/" + contact.getName());
        return contact;
    }

    /**
     * Create a new feed.
     * 
     * @param tags
     *            List of tags supported by this feed.
     * @return A new feed.
     */
    private Feed createFeed(List<String> tags) {
        final Feed feed = new Feed();
        feed.setId(Integer.toString(this.feedSequence++));
        feed.setTags(tags);
        return feed;
    }

    /**
     * Create a new mail given it's sender and recipients.
     * 
     * @param sender
     *            sender of the mail
     * @param recipients
     *            primary recipients of the mail.
     * @param feeds
     *            List of potential matching feeds.
     * @return A new mail.
     */
    private Mail createMail(User sender, List<Contact> recipients,
            List<Feed> feeds) {
        final Mail mail = new Mail();
        mail.setId(Integer.toString(this.mailSequence++));
        mail.setRecipients(new ArrayList<Contact>(recipients.subList(0,
                (Integer.parseInt(mail.getId())) % recipients.size() + 1)));
        mail.setMessage("Cheers -" + sender.getFirstName());
        mail.setSendingDate(new Date());
        mail.setSubject("Hello!");

        // Set the status according to the mail id.
        mail.setStatus(this.mailStatuses[(Integer.parseInt(mail.getId()))
                % this.mailStatuses.length]);
        // Set the list of tags according to the mail id.
        mail.setTags(new ArrayList<String>(this.mailTags.subList(0, (Integer
                .parseInt(mail.getId()))
                % (this.mailTags.size() + 1))));

        // Add the mail to the appropriate feed.
        if (!mail.getTags().isEmpty()) {
            for (final Feed feed : feeds) {
                if (mail.getTags().containsAll(feed.getTags())) {
                    feed.getMails().add(mail);
                }
            }
        }
        return mail;
    }

    /**
     * Create a new mail box and set its owner.
     * 
     * @param owner
     *            user that owns this mail box.
     * @return A new mailbox.
     */
    private Mailbox createMailbox(User owner) {
        final Mailbox mailbox = new Mailbox();
        mailbox.setId(Integer.toString(this.mailboxSequence++));
        mailbox.setOwner(owner);

        // Create one feed
        final int index = Integer.parseInt(mailbox.getId())
                % (this.mailTags.size());
        mailbox.getFeeds().add(
                createFeed(Arrays.asList(this.mailTags.get(index))));

        // Create several contacts
        for (int i = 0; i < this.mailboxSequence; i++) {
            mailbox.getContacts().add(createContact());
        }

        for (int i = 0; i < this.mailboxSequence; i++) {
            mailbox.getMails()
                    .add(
                            createMail(owner, mailbox.getContacts(), mailbox
                                    .getFeeds()));
        }

        return mailbox;
    }

    public MailRoot getMailRoot() {
        return this.mailRoot;
    }

}
