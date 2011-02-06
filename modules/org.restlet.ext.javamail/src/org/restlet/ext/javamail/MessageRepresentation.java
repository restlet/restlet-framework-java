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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.restlet.data.MediaType;
import org.restlet.engine.http.header.ContentType;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
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
     *            The JavaMail message to format.
     * @throws IOException
     * @throws MessagingException
     * @throws DOMException
     */
    public MessageRepresentation(Message message) throws IOException,
            DOMException, MessagingException {
        super(MediaType.APPLICATION_XML);

        final Document dom = getDocument();
        final Element email = dom.createElement("email");
        dom.appendChild(email);

        // Add the email header
        final Element head = dom.createElement("head");
        email.appendChild(head);

        if (message.getSubject() != null) {
            final Element subject = dom.createElement("subject");
            subject.setTextContent(message.getSubject());
            head.appendChild(subject);
        }

        final Address[] froms = message.getFrom();
        if (froms != null) {
            for (final Address fromAddress : froms) {
                final Element from = dom.createElement("from");
                from.setTextContent(fromAddress.toString());
                head.appendChild(from);
            }
        }

        final Address[] tos = message.getRecipients(Message.RecipientType.TO);
        if (tos != null) {
            for (final Address toAddress : tos) {
                final Element to = dom.createElement("to");
                to.setTextContent(toAddress.toString());
                head.appendChild(to);
            }
        }

        final Address[] ccs = message.getRecipients(Message.RecipientType.CC);
        if (ccs != null) {
            for (final Address ccAddress : ccs) {
                final Element cc = dom.createElement("cc");
                cc.setTextContent(ccAddress.toString());
                head.appendChild(cc);
            }
        }

        final Address[] bccs = message.getRecipients(Message.RecipientType.BCC);
        if (bccs != null) {
            for (final Address bccAddress : bccs) {
                final Element bcc = dom.createElement("bcc");
                bcc.setTextContent(bccAddress.toString());
                head.appendChild(bcc);
            }
        }

        if (message.getReceivedDate() != null) {
            final Element received = dom.createElement("received");
            received.setTextContent(DateUtils.format(message.getReceivedDate(),
                    DateUtils.FORMAT_RFC_1123.get(0)));
            head.appendChild(received);
        }

        if (message.getSentDate() != null) {
            final Element sent = dom.createElement("sent");
            sent.setTextContent(DateUtils.format(message.getSentDate(),
                    DateUtils.FORMAT_RFC_1123.get(0)));
            head.appendChild(sent);
        }
        email.appendChild(head);

        // Complete the XML representation with the text part of the message
        Representation content = null;
        if (message.getContent() instanceof Multipart) {
            // Look for the text part of the mail.
            final Multipart multipart = (Multipart) message.getContent();

            for (int i = 0, n = multipart.getCount(); i < n; i++) {
                final Part part = multipart.getBodyPart(i);

                final String disposition = part.getDisposition();
                if (disposition != null) {
                    if (disposition.equals(Part.ATTACHMENT)
                            || disposition.equals(Part.INLINE)) {
                        // create a representation from part.getInputStream()
                    }
                } else {
                    // Check if plain text
                    final MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                    final ContentType contentType = new ContentType(
                            mimeBodyPart.getContentType());
                    if (MediaType.TEXT_PLAIN.equals(contentType.getMediaType(),
                            true)) {
                        content = new InputRepresentation(mimeBodyPart
                                .getInputStream(), MediaType.TEXT_PLAIN);
                        break;
                    }

                    // TODO Special non-attachment cases here of
                    // image/gif, text/html, ...
                }
            }
        } else {
            // Add the email body
            if (message.getContentType() != null) {
                final ContentType contentType = new ContentType(message
                        .getContentType());
                if (MediaType.TEXT_PLAIN.equals(contentType.getMediaType(),
                        true)) {
                    content = new InputRepresentation(message.getInputStream(),
                            MediaType.TEXT_PLAIN);
                }
            }
        }
        if (content != null) {
            final Element body = dom.createElement("body");

            final CDATASection bodyContent = dom.createCDATASection(content
                    .getText());
            body.appendChild(bodyContent);
            email.appendChild(body);
        }
    }
}
