package org.restlet.example.book.restlet.ch9.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.example.book.restlet.ch9.objects.MailRoot;
import org.restlet.example.book.restlet.ch9.objects.User;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.query.Predicate;
import com.db4o.ta.TransparentPersistenceSupport;

public class PersistenceTestCase extends TestCase {

    /** Domain objects. */
    private DomainObjects domainObjects;

    /** Mail root. */
    private MailRoot mailRoot;

    /** Object container. */
    ObjectContainer objectContainer;

    File db4oFile = new File(System.getProperty("java.io.tmpdir"),
            "testdb4o.dbo");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db4oFile.delete();
        Configuration configuration = Db4o.newConfiguration();
        configuration.activationDepth(10);
        configuration.add(new TransparentPersistenceSupport());
        objectContainer = Db4o.openFile(configuration, db4oFile
                .getAbsolutePath());

        domainObjects = new DomainObjects();
        mailRoot = domainObjects.getMailRoot();

        objectContainer.store(mailRoot);
        mailRoot = null;

        ObjectSet<MailRoot> list = objectContainer
                .queryByExample(new MailRoot());
        if (list.isEmpty()) {
            throw new Exception("The database has not been properly mounted.");
        } else {
            mailRoot = list.get(0);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        objectContainer.close();
        super.tearDown();
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

    public void testDB1() {
        Mailbox mailbox = mailRoot.getMailboxes().get(2);

        // Get the unique administrator of the db.
        User proto = new User();
        proto.setAdministrator(true);

        ObjectSet<User> result = objectContainer.queryByExample(proto);
        assertEquals(result.size(), 1);

        mailbox.getOwner().setAdministrator(false);

        objectContainer.store(mailbox.getOwner());
        objectContainer.commit();
        result = objectContainer.queryByExample(proto);

        assertEquals(result.size(), 0);
    }

    public void testDB2() {
        // Set a contact prototype for future research.
        Contact contactProto = new Contact();
        // Set a Mail predicate for future research.
        Predicate<Mail> mailByContactNamePredicate = new Predicate<Mail>() {
            @Override
            public boolean match(Mail mail) {
                boolean found = false;
                for (Contact contact : mail.getRecipients()) {
                    if ("contact-5".equals(contact.getName())) {
                        found = true;
                        break;
                    }
                }

                return found;
            }
        };
        // Set a Mailbox predicate for future research.
        Predicate<Mailbox> mailboxByContactNamePredicate = new Predicate<Mailbox>() {
            @Override
            public boolean match(Mailbox mailbox) {
                boolean found = false;
                for (Contact contact : mailbox.getContacts()) {
                    if ("contact-5".equals(contact.getName())) {
                        found = true;
                        break;
                    }
                }

                return found;
            }
        };

        // Set of contacts resulting from a request
        ObjectSet<Contact> contactList;
        // Set of mails resulting from a request
        ObjectSet<Mail> mailList;
        // Set of mailboxess resulting from a request
        ObjectSet<Mailbox> mailboxList;

        // Search contact by name.
        contactProto.setName("contact-4");
        contactList = objectContainer.queryByExample(contactProto);
        assertEquals(contactList.size(), 1);

        // Search mail by name of contact.
        mailList = objectContainer.query(mailByContactNamePredicate);
        assertEquals(mailList.size(), 1);

        // Update this contact
        Contact contact = contactList.get(0);
        contact.setName("contact-5");
        objectContainer.store(contact);
        objectContainer.commit();

        // Check that the mail sent to contact-4 now points to contact-5
        mailList = objectContainer.query(mailByContactNamePredicate);
        assertEquals(mailList.size(), 2);

        mailboxList = objectContainer.query(mailboxByContactNamePredicate);
        assertEquals(mailboxList.size(), 1);

        // Look for "contact-5" (2 contacts) and delete them.
        contactProto.setName("contact-5");
        contactList = objectContainer.queryByExample(contactProto);
        for (Contact contact2 : contactList) {
            for (Mailbox mailbox : mailboxList) {
                boolean found = false;
                for (int i = 0; i < mailbox.getContacts().size() && !found; i++) {
                    Contact contact3 = mailbox.getContacts().get(i);
                    if (contact2.getId().equals(contact3.getId())) {
                        mailbox.getContacts().remove(i);
                        found = true;
                    }
                }
                objectContainer.store(mailbox);
            }
            for (Mail mail : mailList) {
                List<Contact> list = new ArrayList<Contact>();
                for (int i = 0; i < mail.getRecipients().size(); i++) {
                    Contact contact3 = mail.getRecipients().get(i);
                    if (!contact2.getId().equals(contact3.getId())) {
                        list.add(contact3);
                    }
                }
                mail.setRecipients(list);
                objectContainer.store(mail);
            }

            objectContainer.delete(contact2);
        }
        objectContainer.commit();

        // Check the list is empty
        contactList = objectContainer.queryByExample(contactProto);
        assertTrue(contactList.isEmpty());

        mailboxList = objectContainer.query(mailboxByContactNamePredicate);
        assertTrue(mailboxList.isEmpty());

        mailList = objectContainer.query(mailByContactNamePredicate);
        assertTrue(mailList.isEmpty());
    }
}
