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

package org.restlet.example.book.restlet.ch8.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Root object that manages a set of mailboxes and users.
 */
public class MailRoot extends BaseObject {

    /** Mail boxes managed by the application. */
    private List<Mailbox> mailboxes;

    /** Users managed by the application. */
    private List<User> users;

    public MailRoot() {
        super();
        this.mailboxes = new ArrayList<Mailbox>();
        this.users = new ArrayList<User>();
    }

    public List<Mailbox> getMailboxes() {
        return this.mailboxes;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setMailboxes(List<Mailbox> mailboxes) {
        this.mailboxes = mailboxes;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
