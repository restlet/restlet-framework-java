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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.restlet.data.MediaType;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.DateUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * XML representation of a JavaMail message.
 * 
 * @author Jerome Louvel
 */
public class MessageRepresentation extends DomRepresentation {

    /**
     * Constructor.
     * 
     * @param message
     *                The JavaMail message to format.
     * @throws IOException
     * @throws MessagingException
     * @throws DOMException
     */
    public MessageRepresentation(Message message) throws IOException,
            DOMException, MessagingException {
        super(MediaType.APPLICATION_XML);

        Document dom = getDocument();
        Element email = dom.createElement("email");
        dom.appendChild(email);

        // Add the email header
        Element head = dom.createElement("head");
        email.appendChild(head);

        if (message.getSubject() != null) {
            Element subject = dom.createElement("subject");
            subject.setTextContent(message.getSubject());
            head.appendChild(subject);
        }

        Address[] froms = message.getFrom();
        if (froms != null) {
            for (Address fromAddress : froms) {
                Element from = dom.createElement("from");
                from.setTextContent(fromAddress.toString());
                head.appendChild(from);
            }
        }

        Address[] tos = message.getRecipients(Message.RecipientType.TO);
        if (tos != null) {
            for (Address toAddress : tos) {
                Element to = dom.createElement("to");
                to.setTextContent(toAddress.toString());
                head.appendChild(to);
            }
        }

        Address[] ccs = message.getRecipients(Message.RecipientType.CC);
        if (ccs != null) {
            for (Address ccAddress : ccs) {
                Element cc = dom.createElement("cc");
                cc.setTextContent(ccAddress.toString());
                head.appendChild(cc);
            }
        }

        Address[] bccs = message.getRecipients(Message.RecipientType.BCC);
        if (bccs != null) {
            for (Address bccAddress : bccs) {
                Element bcc = dom.createElement("bcc");
                bcc.setTextContent(bccAddress.toString());
                head.appendChild(bcc);
            }
        }

        if (message.getReceivedDate() != null) {
            Element received = dom.createElement("received");
            received.setTextContent(DateUtils.format(message.getReceivedDate(),
                    DateUtils.FORMAT_RFC_1123.get(0)));
            head.appendChild(received);
        }

        if (message.getSentDate() != null) {
            Element sent = dom.createElement("sent");
            sent.setTextContent(DateUtils.format(message.getSentDate(),
                    DateUtils.FORMAT_RFC_1123.get(0)));
            head.appendChild(sent);
        }
        email.appendChild(head);

        // Complete the XML representation with the text part of the message
        Representation content = null;
        if (message.getContent() instanceof Multipart) {
            // Look for the text part of the mail.
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0, n = multipart.getCount(); i < n; i++) {
                Part part = multipart.getBodyPart(i);

                String disposition = part.getDisposition();
                if (disposition != null) {
                    if (disposition.equals(Part.ATTACHMENT)
                            || disposition.equals(Part.INLINE)) {
                        // create a representation from part.getInputStream()
                    }
                } else {
                    // Check if plain text
                    MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                    // Take care of getting the name of the media type, not its
                    // parameters.
                    // TODO to be enhanced in MediaType#valueOf?
                    String[] tab = mimeBodyPart.getContentType().split(";");
                    String mediaType = tab[0];
                    MediaType contentType = MediaType.valueOf(mediaType);
                    if (MediaType.TEXT_PLAIN.equals(contentType, true)) {
                        content = new InputRepresentation(mimeBodyPart
                                .getInputStream(), MediaType.TEXT_PLAIN);
                        break;
                    } else {
                        // Special non-attachment cases here of
                        // image/gif, text/html, ...
                    }
                }
            }
        } else {
            // Add the email body
            if (message.getContentType() != null) {
                // Take care of getting the name of the media type, not its
                // parameters.
                // TODO to be enhanced in MediaType#valueOf?
                String[] tab = message.getContentType().split(";");
                String mediaType = tab[0];
                MediaType contentType = MediaType.valueOf(mediaType);
                if (MediaType.TEXT_PLAIN.equals(contentType, true)) {
                    content = new InputRepresentation(message.getInputStream(),
                            MediaType.TEXT_PLAIN);
                }
            }
        }
        if (content != null) {
            Element body = dom.createElement("body");

            CDATASection bodyContent = dom
                    .createCDATASection(content.getText());
            body.appendChild(bodyContent);
            email.appendChild(body);
        }
    }
}
