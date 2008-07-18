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

package org.restlet.example.book.restlet.ch8.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.objects.Contact;
import org.restlet.example.book.restlet.ch8.objects.Mail;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
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

        if (getCurrentUser() != null) {
            // Authenticated access.
            setModifiable(true);
            // Get the parent mailbox thanks to its ID taken from the resource's
            // URI.
            final String mailboxId = Reference.decode((String) request
                    .getAttributes().get("mailboxId"));
            this.mailbox = getObjectsFacade().getMailboxById(mailboxId);

            if (this.mailbox != null) {
                this.mails = this.mailbox.getMails();
                getVariants().add(new Variant(MediaType.TEXT_HTML));
            }
        } else {
            // Anonymous access
            setModifiable(false);
        }
    }

    /**
     * Accept the representation of a new mail, and create it.
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);

        Mail mail = new Mail();
        mail.setStatus(Mail.STATUS_DRAFT);
        final Contact sender = new Contact();
        sender.setName(getCurrentUser().getFirstName() + " "
                + getCurrentUser().getLastName());
        sender.setMailAddress(getRequest().getRootRef().getIdentifier()
                + "/mailboxes/" + this.mailbox.getId());
        mail.setSender(sender);
        mail.setSubject(form.getFirstValue("subject"));
        mail.setMessage(form.getFirstValue("message"));

        if (form.getFirstValue("recipients") != null) {
            final List<Contact> recipients = new ArrayList<Contact>();
            for (final Parameter parameter : form.subList("recipients")) {
                for (final Contact contact : this.mailbox.getContacts()) {
                    if (contact.getId().equals(parameter.getValue())) {
                        recipients.add(contact);
                    }
                }
            }
            mail.setRecipients(recipients);
        } else {
            mail.setRecipients(null);
        }

        if (form.getFirstValue("tags") != null) {
            mail.setTags(new ArrayList<String>(Arrays.asList(form
                    .getFirstValue("tags").split(" "))));
        } else {
            mail.setTags(null);
        }

        mail = getObjectsFacade().createMail(this.mailbox, mail);

        getResponse().redirectSeeOther(
                getChildReference(getRequest().getResourceRef(), mail.getId()));
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", this.mailbox);
        dataModel.put("mails", this.mails);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getHTMLTemplateRepresentation("mails.html", dataModel);
    }
}
