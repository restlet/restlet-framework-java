/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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
