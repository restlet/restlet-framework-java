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
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * JavaMail MIME message based on an XML representation. For the XML format, see
 * the {@link JavaMailClientHelper} class.
 * 
 * @author Jerome Louvel
 */
public class RepresentationMessage extends MimeMessage {

    /**
     * Creates a JavaMail message by parsing an XML representation.
     * 
     * @param xmlMessage
     *            The XML message to parse.
     * @param session
     *            The current JavaMail session.
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     */
    public RepresentationMessage(Representation xmlMessage, Session session)
            throws IOException, AddressException, MessagingException {
        super(session);
        DomRepresentation dom = new DomRepresentation(xmlMessage);
        Document email = dom.getDocument();
        Element root = (Element) email.getElementsByTagName("email").item(0);
        Element header = (Element) root.getElementsByTagName("head").item(0);
        String subject = header.getElementsByTagName("subject").item(0)
                .getTextContent();
        String from = header.getElementsByTagName("from").item(0)
                .getTextContent();

        NodeList toList = header.getElementsByTagName("to");
        String[] to = new String[toList.getLength()];

        for (int i = 0; i < toList.getLength(); i++) {
            to[i] = toList.item(i).getTextContent();
        }

        NodeList ccList = header.getElementsByTagName("cc");
        String[] cc = new String[ccList.getLength()];

        for (int i = 0; i < ccList.getLength(); i++) {
            cc[i] = ccList.item(i).getTextContent();
        }

        NodeList bccList = header.getElementsByTagName("bcc");
        String[] bcc = new String[bccList.getLength()];

        for (int i = 0; i < bccList.getLength(); i++) {
            bcc[i] = bccList.item(i).getTextContent();
        }

        String text = root.getElementsByTagName("body").item(0)
                .getTextContent();

        // Set the FROM and TO fields
        setFrom(new InternetAddress(from));

        for (String element : to) {
            addRecipient(Message.RecipientType.TO, new InternetAddress(element));
        }

        for (String element : cc) {
            addRecipient(Message.RecipientType.CC, new InternetAddress(element));
        }

        for (final String element : bcc) {
            addRecipient(Message.RecipientType.BCC,
                    new InternetAddress(element));
        }

        // Set the subject and content text
        setSubject(subject);
        setText(text);
        setSentDate(new Date());
        saveChanges();
    }

    /**
     * Constructor.
     * 
     * @param session
     *            The current JavaMail session.
     */
    public RepresentationMessage(Session session) {
        super(session);
    }

}
