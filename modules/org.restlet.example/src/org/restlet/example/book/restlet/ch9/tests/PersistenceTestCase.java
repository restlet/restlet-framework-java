package org.restlet.example.book.restlet.ch9.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.MailBox;
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

    public void testDB1() {
        MailBox mailBox = mailRoot.getMailBoxes().get(2);

        // Get the unique administrator of the db.
        User proto = new User();
        proto.setAdministrator(true);

        ObjectSet<User> result = objectContainer.queryByExample(proto);
        assertEquals(result.size(), 1);

        mailBox.getOwner().setAdministrator(false);

        objectContainer.store(mailBox.getOwner());
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
                for (Contact contact : mail.getPrimaryRecipients()) {
                    if ("contact-5".equals(contact.getName())) {
                        found = true;
                        break;
                    }
                }

                return found;
            }
        };
        // Set a MailBox predicate for future research.
        Predicate<MailBox> mailboxByContactNamePredicate = new Predicate<MailBox>() {
            @Override
            public boolean match(MailBox mailBox) {
                boolean found = false;
                for (Contact contact : mailBox.getContacts()) {
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
        ObjectSet<MailBox> mailBoxList;

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

        mailBoxList = objectContainer.query(mailboxByContactNamePredicate);
        assertEquals(mailBoxList.size(), 1);

        // Look for "contact-5" (2 contacts) and delete them.
        contactProto.setName("contact-5");
        contactList = objectContainer.queryByExample(contactProto);
        for (Contact contact2 : contactList) {
            for (MailBox mailBox : mailBoxList) {
                boolean found = false;
                for (int i = 0; i < mailBox.getContacts().size() && !found; i++) {
                    Contact contact3 = mailBox.getContacts().get(i);
                    if (contact2.getId().equals(contact3.getId())) {
                        mailBox.getContacts().remove(i);
                        found = true;
                    }
                }
                objectContainer.store(mailBox);
            }
            for (Mail mail : mailList) {
                List<Contact> list = new ArrayList<Contact>();
                for (int i = 0; i < mail.getPrimaryRecipients().size(); i++) {
                    Contact contact3 = mail.getPrimaryRecipients().get(i);
                    if (!contact2.getId().equals(contact3.getId())) {
                        list.add(contact3);
                    }
                }
                mail.setPrimaryRecipients(list);
                objectContainer.store(mail);
            }

            objectContainer.delete(contact2);
        }
        objectContainer.commit();

        // Check the list is empty
        contactList = objectContainer.queryByExample(contactProto);
        assertTrue(contactList.isEmpty());

        mailBoxList = objectContainer.query(mailboxByContactNamePredicate);
        assertTrue(mailBoxList.isEmpty());

        mailList = objectContainer.query(mailByContactNamePredicate);
        assertTrue(mailList.isEmpty());
    }
}
