package org.restlet.example.book.restlet.ch08.sec5.webapi.common;

import java.util.List;

public class AccountRepresentation {

    private List<String> contactRefs;

    private List<String> mailRefs;

    private List<String> feedRefs;

    public AccountRepresentation() {
    }

    public List<String> getContactRefs() {
        return contactRefs;
    }

    public List<String> getFeedRefs() {
        return feedRefs;
    }

    public List<String> getMailRefs() {
        return mailRefs;
    }

    public void setContactRefs(List<String> contactRefs) {
        this.contactRefs = contactRefs;
    }

    public void setFeedRefs(List<String> feedRefs) {
        this.feedRefs = feedRefs;
    }

    public void setMailRefs(List<String> mailRefs) {
        this.mailRefs = mailRefs;
    }

}
