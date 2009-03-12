/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch8.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Mail exchanged between a sender and receivers.
 */
public class Mail extends BaseObject {

    public static final String STATUS_DRAFT = "draft";

    public static final String STATUS_SENDING = "sending";

    public static final String STATUS_SENT = "sent";

    public static final String STATUS_RECEIVING = "receiving";

    public static final String STATUS_RECEIVED = "received";

    /** Textual message of the mail. */
    private String message;

    /** List of recipients of the mail. */
    private List<Contact> recipients;

    /** Sender of the mail. */
    private Contact sender;

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
        this.recipients = new ArrayList<Contact>();
        this.tags = new ArrayList<String>();
    }

    public String getMessage() {
        return this.message;
    }

    public List<Contact> getRecipients() {
        return this.recipients;
    }

    public Contact getSender() {
        return this.sender;
    }

    public Date getSendingDate() {
        return this.sendingDate;
    }

    public String getStatus() {
        return this.status;
    }

    public String getSubject() {
        return this.subject;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecipients(List<Contact> recipients) {
        this.recipients = recipients;
    }

    public void setSender(Contact sender) {
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
