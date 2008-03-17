package org.restlet.example.book.restlet.ch9.objects;

import java.util.List;

/**
 * Mail application that manages a set of mailboxes.
 */
public class MailRoot {
    /** Mail boxes managed by the application. */
    private List<MailBox> mailBoxes;

    /** Users managed by the application. */
    private List<User> users;

    public List<MailBox> getMailBoxes() {
        return mailBoxes;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setMailBoxes(List<MailBox> mailBoxes) {
        this.mailBoxes = mailBoxes;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
