package org.restlet.example.book.restlet.ch9.tests;

import junit.framework.TestCase;

import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.example.book.restlet.ch9.objects.MailRoot;

public class DomainTestCase extends TestCase {

    /** Domain objects. */
    private DomainObjects domainObjects;

    /** Mail root. */
    private MailRoot mailRoot;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        domainObjects = new DomainObjects();
        mailRoot = domainObjects.getMailRoot();
    }

    public void testMailboxes() {
        assertEquals(mailRoot.getMailboxes().size(), 3);
        assertEquals(mailRoot.getMailboxes().get(0).getOwner(), mailRoot
                .getUsers().get(0));
        assertTrue(mailRoot.getMailboxes().get(2).getOwner().isAdministrator());

    }

    public void testMailbox1() {
        // Test first Mailbox
        Mailbox mailbox = mailRoot.getMailboxes().get(0);
        assertEquals(mailbox.getContacts().size(), 2);
        assertEquals(mailbox.getContacts().get(1).getName(), "contact-2");
        assertEquals(mailbox.getMails().size(), 2);

        assertEquals(mailbox.getMails().get(0).getSender(), mailRoot.getUsers()
                .get(0));
        assertEquals(mailbox.getMails().get(1).getSender(), mailRoot.getUsers()
                .get(0));

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
        Mailbox mailbox = mailRoot.getMailboxes().get(1);
        assertEquals(mailbox.getContacts().size(), 3);
        assertEquals(mailbox.getContacts().get(1).getName(), "contact-4");
        assertEquals(mailbox.getMails().size(), 3);

        assertEquals(mailbox.getMails().get(0).getSender(), mailRoot.getUsers()
                .get(0));
        assertEquals(mailbox.getMails().get(1).getSender(), mailRoot.getUsers()
                .get(0));

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
        Mailbox mailbox = mailRoot.getMailboxes().get(2);

        assertEquals(mailbox.getContacts().size(), 4);
        assertEquals(mailbox.getContacts().get(1).getName(), "contact-7");
        assertEquals(mailbox.getMails().size(), 4);

        assertEquals(mailbox.getMails().get(0).getSender(), mailRoot.getUsers()
                .get(1));
        assertEquals(mailbox.getMails().get(1).getSender(), mailRoot.getUsers()
                .get(1));
        assertEquals(mailbox.getMails().get(3).getSender(), mailRoot.getUsers()
                .get(1));

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

}
