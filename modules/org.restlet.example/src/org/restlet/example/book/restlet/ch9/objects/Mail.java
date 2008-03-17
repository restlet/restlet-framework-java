package org.restlet.example.book.restlet.ch9.objects;

import java.util.Date;
import java.util.List;

/**
 * Mail exchanged between a sender and receivers.
 * 
 */
public class Mail {
    /** Textual message of the mail. */
    private String message;

    /** List of primary recipients of the mail. */
    private List<Contact> primaryRecipients;

    /** List of secondary recipients of the mail. */
    private List<Contact> secondaryRecipients;

    /** Sender of the mail. */
    private User sender;

    /** Sending date of the mail. */
    private Date sendingDate;

    /** Status of the mail. */
    private String status;

    /** Subject of the mail. */
    private String subject;

    /** List of tags of the mail. */
    private List<String> tags;

    public String getMessage() {
        return message;
    }

    public List<Contact> getPrimaryRecipients() {
        return primaryRecipients;
    }

    public List<Contact> getSecondaryRecipients() {
        return secondaryRecipients;
    }

    public User getSender() {
        return sender;
    }

    public Date getSendingDate() {
        return sendingDate;
    }

    public String getStatus() {
        return status;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPrimaryRecipients(List<Contact> primaryRecipients) {
        this.primaryRecipients = primaryRecipients;
    }

    public void setSecondaryRecipients(List<Contact> secondaryRecipients) {
        this.secondaryRecipients = secondaryRecipients;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setSendingDate(Date sendingDate) {
        this.sendingDate = sendingDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
