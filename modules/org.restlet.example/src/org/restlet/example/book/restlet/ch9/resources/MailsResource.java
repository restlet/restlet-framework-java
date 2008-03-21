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

package org.restlet.example.book.restlet.ch9.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch9.objects.Contact;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a list of mails.
 * 
 */
public class MailsResource extends BaseResource {

    /** The parent mailbox. */
    private Mailbox mailbox;

    /** The list of mails. */
    private List<Mail> mails;

    public MailsResource(Context context, Request request, Response response) {
        super(context, request, response);
        String mailboxId = (String) request.getAttributes().get("mailboxId");
        mailbox = getDAOFactory().getMailboxDAO().getMailboxById(mailboxId);

        if (mailbox != null) {
            getVariants().add(new Variant(MediaType.TEXT_HTML));
            mails = mailbox.getMails();
        }
    }

    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);

        Mail mail = new Mail();
        mail.setSender(getCurrentUser());
        mail.setStatus(Mail.STATUS_DRAFT);
        mail.setSubject(form.getFirstValue("subject"));
        mail.setMessage(form.getFirstValue("message"));

        String[] recipientsArray = form.getFirstValue("recipients").split(" ");
        List<Contact> recipients = new ArrayList<Contact>();
        for (int i = 0; i < recipientsArray.length; i++) {
            String string = recipientsArray[i];
            Contact contact = new Contact();
            contact.setName(string);
            recipients.add(contact);
        }
        mail.setRecipients(recipients);
        mail
                .setTags(Arrays.asList(form.getFirstValue("tags").split(
                        " ")));
        mail = getDAOFactory().getMailboxDAO().createMail(mailbox, mail);
        Reference mailRef = new Reference(getRequest().getResourceRef(), mail
                .getId());
        getResponse().redirectSeeOther(mailRef);
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", mailbox);
        dataModel.put("mails", mails);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        TemplateRepresentation representation = new TemplateRepresentation(
                "mails.html", getFmcConfiguration(), dataModel, variant
                        .getMediaType());

        return representation;
    }
}
