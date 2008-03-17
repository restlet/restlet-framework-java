package org.restlet.example.book.restlet.ch9.objects;

import java.util.List;

/**
 * Mail application that manages a set of mailboxes
 */
public class MailRoot {
    /** Mail boxes managed by the application. */
    private List<MailBox> mailBoxes;

    public List<MailBox> getMailBoxes() {
        return mailBoxes;
    }

    public void setMailBoxes(List<MailBox> mailBoxes) {
        this.mailBoxes = mailBoxes;
    }

}
