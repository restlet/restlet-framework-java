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

package org.restlet.example.book.restlet.ch8.tests;

import junit.framework.TestCase;

import org.restlet.example.book.restlet.ch8.objects.MailRoot;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;

/**
 *
 */
public class DomainTestCase extends TestCase {

    /** Domain objects. */
    private DomainObjects domainObjects;

    /** Mail root. */
    private MailRoot mailRoot;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.domainObjects = new DomainObjects();
        this.mailRoot = this.domainObjects.getMailRoot();
    }

    public void testMailbox1() {
        // Test first Mailbox
        final Mailbox mailbox = this.mailRoot.getMailboxes().get(0);
        assertEquals(mailbox.getContacts().size(), 2);
        assertEquals(mailbox.getContacts().get(1).getName(), "contact-2");
        assertEquals(mailbox.getMails().size(), 2);

        assertEquals(mailbox.getMails().get(0).getTags().get(0), "tag1");
        assertEquals(mailbox.getMails().get(1).getTags().size(), 2);

        assertEquals(mailbox.getFeeds().get(0).getTags().get(0), "tag2");
        assertEquals(mailbox.getMails().get(1).getTags().get(1), "tag2");
        // The unique feed of the first mail box does not contain the first
        // mail.
        assertFalse(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(0)));
        assertTrue(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(1)));

        assertEquals(mailbox.getMails().get(0).getRecipients().get(0), mailbox
                .getContacts().get(0));
        assertEquals(mailbox.getMails().get(0).getRecipients().get(1), mailbox
                .getContacts().get(1));
    }

    public void testMailbox2() {
        // Test second Mailbox
        final Mailbox mailbox = this.mailRoot.getMailboxes().get(1);
        assertEquals(mailbox.getContacts().size(), 3);
        assertEquals(mailbox.getContacts().get(1).getName(), "contact-4");
        assertEquals(mailbox.getMails().size(), 3);

        assertEquals(mailbox.getMails().get(0).getTags().get(0), "tag1");
        assertTrue(mailbox.getMails().get(1).getTags().isEmpty());

        assertEquals(mailbox.getFeeds().get(0).getTags().get(0), "tag3");
        assertEquals(mailbox.getFeeds().get(0).getMails().size(), 1);
        assertTrue(mailbox.getMails().get(1).getTags().isEmpty());
        // The unique feed of the second mail box contains the first
        // mail.
        assertTrue(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(0)));
        assertFalse(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(1)));

        assertEquals(mailbox.getMails().get(0).getRecipients().size(), 1);
        assertEquals(mailbox.getMails().get(1).getRecipients().size(), 2);
        assertEquals(mailbox.getMails().get(0).getRecipients().get(0), mailbox
                .getContacts().get(0));
        assertEquals(mailbox.getMails().get(1).getRecipients().get(1), mailbox
                .getContacts().get(1));
    }

    public void testMailbox3() {
        // Test third Mailbox
        final Mailbox mailbox = this.mailRoot.getMailboxes().get(2);

        assertEquals(mailbox.getContacts().size(), 4);
        assertEquals(mailbox.getContacts().get(1).getName(), "contact-7");
        assertEquals(mailbox.getMails().size(), 4);

        assertEquals(mailbox.getMails().get(0).getTags().get(0), "tag1");
        assertEquals(mailbox.getMails().get(0).getTags().get(1), "tag2");
        assertEquals(mailbox.getMails().get(1).getTags().get(2), "tag3");
        assertTrue(mailbox.getMails().get(2).getTags().isEmpty());

        assertEquals(mailbox.getFeeds().get(0).getTags().get(0), "tag1");
        assertEquals(mailbox.getFeeds().get(0).getMails().size(), 3);
        // The unique feed of the third mail box does not contain the second
        // mail.
        assertTrue(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(0)));
        assertTrue(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(1)));
        assertFalse(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(2)));
        assertTrue(mailbox.getFeeds().get(0).getMails().contains(
                mailbox.getMails().get(3)));

        assertEquals(mailbox.getMails().get(1).getRecipients().size(), 4);
        assertEquals(mailbox.getMails().get(1).getRecipients().get(3), mailbox
                .getContacts().get(3));
    }

    public void testMailboxes() {
        assertEquals(this.mailRoot.getMailboxes().size(), 3);
        assertEquals(this.mailRoot.getMailboxes().get(0).getOwner(),
                this.mailRoot.getUsers().get(0));
        assertTrue(this.mailRoot.getMailboxes().get(2).getOwner()
                .isAdministrator());

    }

}
