/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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
