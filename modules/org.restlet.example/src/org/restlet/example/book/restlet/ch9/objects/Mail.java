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

package org.restlet.example.book.restlet.ch9.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Mail exchanged between a sender and receivers.
 * 
 */
public class Mail extends BaseObject {

    public static final String STATUS_DRAFT = "draft";

    public static final String STATUS_SENDING = "sending";

    public static final String STATUS_SENT = "sent";

    public static final String STATUS_RECEIVING = "receiving";

    public static final String STATUS_RECEIVED = "received";

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

    public Mail() {
        super();
        this.primaryRecipients = new ArrayList<Contact>();
        this.secondaryRecipients = new ArrayList<Contact>();
        this.tags = new ArrayList<String>();
    }

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
