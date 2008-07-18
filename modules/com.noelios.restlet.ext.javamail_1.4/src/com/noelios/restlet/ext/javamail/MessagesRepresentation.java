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

package com.noelios.restlet.ext.javamail;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.restlet.data.MediaType;
import org.restlet.resource.DomRepresentation;
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
