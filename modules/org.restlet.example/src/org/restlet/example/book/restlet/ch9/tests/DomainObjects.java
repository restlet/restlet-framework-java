package org.restlet.example.book.restlet.ch9.tests;

import org.restlet.example.book.restlet.ch9.objects.MailBox;
import org.restlet.example.book.restlet.ch9.objects.MailRoot;
import org.restlet.example.book.restlet.ch9.objects.User;

public class DomainObjects {

    /** Root object at the top of the hierarchy. */
    private MailRoot mailRoot;

    public DomainObjects() {
        super();

        mailRoot = new MailRoot();

        // Add two users.
        User user = new User();
        user.setName("William Mailliw");
        user.setAdministrator(false);
        mailRoot.getUsers().add(user);
        
        User admin = new User();
        admin.setName("admin");
        admin.setAdministrator(true);
        mailRoot.getUsers().add(admin);
        
        //Add three mailboxes
        MailBox mailBox = new MailBox();
        mailBox.setOwner(user);
        
        mailBox = new MailBox();
        mailBox.setOwner(user);

        mailBox = new MailBox();
        mailBox.setOwner(admin);
    }

    public MailRoot getMailRoot() {
        return mailRoot;
    }

}
