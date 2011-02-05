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

package org.restlet.ext.javamail;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.restlet.data.MediaType;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.mail.pop3.POP3Folder;

/**
 * XML representation of a list of JavaMail messages.
 * 
 * @author Jerome Louvel
 */
public class MessagesRepresentation extends DomRepresentation {

    /**
     * Constructor.
     * 
     * @param messages
     *            The list of JavaMail messages to format.
     * @throws IOException
     * @throws MessagingException
     */
    public MessagesRepresentation(Message[] messages, POP3Folder inbox)
            throws IOException, MessagingException {
        super(MediaType.APPLICATION_XML);

        // Format the list
        final Document dom = getDocument();
        final Element emails = dom.createElement("emails");
        dom.appendChild(emails);

        // Retrieve the list of messages
        Element email;
        for (final Message message : messages) {
            final String uid = inbox.getUID(message);

            email = dom.createElement("email");
            email.setAttribute("href", "/" + uid);
            emails.appendChild(email);
        }
    }

}
