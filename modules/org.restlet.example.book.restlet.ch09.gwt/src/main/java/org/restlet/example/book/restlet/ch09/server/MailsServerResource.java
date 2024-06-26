/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.example.book.restlet.ch09.server;

import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch09.common.MailRepresentation;
import org.restlet.example.book.restlet.ch09.common.MailsRepresentation;
import org.restlet.example.book.restlet.ch09.common.MailsResource;
import org.restlet.ext.wadl.WadlServerResource;

/**
 * Mails server resource implementing the {@link MailsResource} interface.
 */
public class MailsServerResource extends WadlServerResource implements
        MailsResource {

    public MailsRepresentation retrieve() {
        MailsRepresentation mails = new MailsRepresentation();
        MailRepresentation mail = new MailRepresentation();
        mail.setStatus("received");
        mail.setSubject("Message to self");
        mail.setContent("Doh!");
        mail.setAccountRef(new Reference(getReference(), "..").getTargetRef()
                .toString());
        mails.getEmails().add(mail);
        return mails;
    }

    public void add(MailRepresentation mail) {
        System.out.println("Status: " + mail.getStatus());
        System.out.println("Subject: " + mail.getSubject());
        System.out.println("Content: " + mail.getContent());
        System.out.println("Account URI: " + mail.getAccountRef());
        System.out.println();
    }

}
