package org.restlet.example.book.restlet.ch9.tests;

import junit.framework.TestCase;

import org.restlet.example.book.restlet.ch9.objects.MailBox;
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

    public void testMailBoxes() {
        assertEquals(mailRoot.getMailBoxes().size(), 3);
        assertEquals(mailRoot.getMailBoxes().get(0).getOwner(), mailRoot
                .getUsers().get(0));
        assertTrue(mailRoot.getMailBoxes().get(2).getOwner().isAdministrator());

    }

    public void testMailBox1() {
        // Test first MailBox
        MailBox mailBox = mailRoot.getMailBoxes().get(0);
        assertEquals(mailBox.getContacts().size(), 2);
        assertEquals(mailBox.getContacts().get(1).getName(), "contact-2");
        assertEquals(mailBox.getMails().size(), 2);

        assertEquals(mailBox.getMails().get(0).getSender(), mailRoot.getUsers()
                .get(0));
        assertEquals(mailBox.getMails().get(1).getSender(), mailRoot.getUsers()
                .get(0));

        assertEquals(mailBox.getMails().get(0).getTags().get(0), "tag1");
        assertEquals(mailBox.getMails().get(1).getTags().size(), 2);

        assertEquals(mailBox.getFeeds().get(0).getTags().get(0), "tag2");
        assertEquals(mailBox.getMails().get(1).getTags().get(1), "tag2");
        // The unique feed of the first mail box does not contain the first
        // mail.
        assertFalse(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(0)));
        assertTrue(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(1)));

        assertEquals(mailBox.getMails().get(0).getPrimaryRecipients().get(0),
                mailBox.getContacts().get(0));
        assertEquals(mailBox.getMails().get(0).getPrimaryRecipients().get(1),
                mailBox.getContacts().get(1));
    }

    public void testMailBox2() {
        // Test second MailBox
        MailBox mailBox = mailRoot.getMailBoxes().get(1);
        assertEquals(mailBox.getContacts().size(), 3);
        assertEquals(mailBox.getContacts().get(1).getName(), "contact-4");
        assertEquals(mailBox.getMails().size(), 3);

        assertEquals(mailBox.getMails().get(0).getSender(), mailRoot.getUsers()
                .get(0));
        assertEquals(mailBox.getMails().get(1).getSender(), mailRoot.getUsers()
                .get(0));

        assertEquals(mailBox.getMails().get(0).getTags().get(0), "tag1");
        assertTrue(mailBox.getMails().get(1).getTags().isEmpty());

        assertEquals(mailBox.getFeeds().get(0).getTags().get(0), "tag3");
        assertEquals(mailBox.getFeeds().get(0).getMails().size(), 1);
        assertTrue(mailBox.getMails().get(1).getTags().isEmpty());
        // The unique feed of the second mail box contains the first
        // mail.
        assertTrue(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(0)));
        assertFalse(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(1)));

        assertEquals(mailBox.getMails().get(0).getPrimaryRecipients().size(), 1);
        assertEquals(mailBox.getMails().get(1).getPrimaryRecipients().size(), 2);
        assertEquals(mailBox.getMails().get(0).getPrimaryRecipients().get(0),
                mailBox.getContacts().get(0));
        assertEquals(mailBox.getMails().get(1).getPrimaryRecipients().get(1),
                mailBox.getContacts().get(1));
    }

    public void testMailBox3() {
        // Test third MailBox
        MailBox mailBox = mailRoot.getMailBoxes().get(2);

        assertEquals(mailBox.getContacts().size(), 4);
        assertEquals(mailBox.getContacts().get(1).getName(), "contact-7");
        assertEquals(mailBox.getMails().size(), 4);

        assertEquals(mailBox.getMails().get(0).getSender(), mailRoot.getUsers()
                .get(1));
        assertEquals(mailBox.getMails().get(1).getSender(), mailRoot.getUsers()
                .get(1));
        assertEquals(mailBox.getMails().get(3).getSender(), mailRoot.getUsers()
                .get(1));

        assertEquals(mailBox.getMails().get(0).getTags().get(0), "tag1");
        assertEquals(mailBox.getMails().get(0).getTags().get(1), "tag2");
        assertEquals(mailBox.getMails().get(1).getTags().get(2), "tag3");
        assertTrue(mailBox.getMails().get(2).getTags().isEmpty());

        assertEquals(mailBox.getFeeds().get(0).getTags().get(0), "tag1");
        assertEquals(mailBox.getFeeds().get(0).getMails().size(), 3);
        // The unique feed of the third mail box does not contain the second
        // mail.
        assertTrue(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(0)));
        assertTrue(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(1)));
        assertFalse(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(2)));
        assertTrue(mailBox.getFeeds().get(0).getMails().contains(
                mailBox.getMails().get(3)));

        assertEquals(mailBox.getMails().get(1).getPrimaryRecipients().size(), 4);
        assertEquals(mailBox.getMails().get(1).getPrimaryRecipients().get(3),
                mailBox.getContacts().get(3));
    }

}
