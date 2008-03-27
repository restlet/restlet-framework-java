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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
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
 * Resource for a mail.
 * 
 */
public class MailResource extends BaseResource {

    /** The mail represented by this resource. */
    private Mail mail;

    /** The parent mailbox. */
    private Mailbox mailbox;

    public MailResource(Context context, Request request, Response response) {
        super(context, request, response);

        if (getCurrentUser() != null) {
            // Authenticated access.
            setModifiable(true);
            String mailboxId = Reference.decode((String) request
                    .getAttributes().get("mailboxId"));
            mailbox = getObjectsFacade().getMailboxById(mailboxId);

            if (mailbox != null) {
                String mailId = (String) request.getAttributes().get("mailId");
                mail = getObjectsFacade().getMailById(mailId);

                if (mail != null) {
                    getVariants().add(new Variant(MediaType.TEXT_HTML));
                }
            }
        } else {
            // Anonymous access
            setModifiable(false);
        }
    }

    /**
     * Remove this resource.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        getObjectsFacade().deleteMail(mailbox, mail);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", mailbox);
        dataModel.put("mail", mail);

        List<Contact> contacts = new ArrayList<Contact>();
        contacts.addAll(mailbox.getContacts());
        if (mail.getRecipients() != null) {
            for (Contact contact : mail.getRecipients()) {
                if (contact.getId() == null) {
                    contacts.add(contact);
                }
            }
        }
        dataModel.put("contacts", contacts);

        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getHTMLTemplateRepresentation("mail_" + mail.getStatus()
                + ".html", dataModel);
    }

    /**
     * Update the underlying mail according to the given representation. If the
     * mail is intended to be sent, send it to all of its recipients.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);
        List<String> mailAddresses = new ArrayList<String>();

        for (Parameter parameter : form.subList("recipients")) {
            mailAddresses.add(parameter.getValue());
        }

        List<String> tags = null;
        if (form.getFirstValue("tags") != null) {
            tags = new ArrayList<String>(Arrays.asList(form.getFirstValue(
                    "tags").split(" ")));
        }

        getObjectsFacade().updateMail(mailbox, mail,
                form.getFirstValue("status"), form.getFirstValue("subject"),
                form.getFirstValue("message"), mailAddresses, tags);

        // Detect if the mail is to be sent.
        if (Mail.STATUS_SENDING.equalsIgnoreCase(mail.getStatus())) {
            mail.setSendingDate(new Date());
            // Loop on the list of recipients and post to their mailbox.
            boolean success = true;
            if (mail.getRecipients() != null) {
                Client client = new Client(Protocol.HTTP);
                Form form2 = new Form();
                form2.add("status", Mail.STATUS_RECEIVING);
                form2.add("senderAddress", getRequest().getRootRef()
                        + "/mailboxes/" + mailbox.getId());
                form2.add("senderName", mailbox.getSenderName());

                form2.add("subject", mail.getSubject());
                form2.add("message", mail.getMessage());
                form2.add("sendingDate", mail.getSendingDate().toString());
                for (Contact recipient : mail.getRecipients()) {
                    form2.add("recipient", recipient.getMailAddress() + "$"
                            + recipient.getName());
                }

                // Send the mail to every recipient
                StringBuilder builder = new StringBuilder();
                Response response;
                Request request = new Request();
                request.setMethod(Method.POST);
                request.setEntity(form2.getWebRepresentation());

                for (Contact contact : mail.getRecipients()) {
                    request.setResourceRef(contact.getMailAddress());
                    response = client.handle(request);
                    // Error when sending the mail.
                    if (!response.getStatus().isSuccess()) {
                        success = false;
                        builder.append(contact.getName());
                        builder.append("\t");
                        builder.append(response.getStatus());
                    }
                }
                if (success) {
                    // if the mail has been successfully sent to every
                    // recipient.
                    mail.setStatus(Mail.STATUS_SENT);
                    getObjectsFacade().updateMail(mailbox, mail);
                    getResponse().redirectSeeOther(
                            getRequest().getResourceRef());
                } else {
                    // At least one error has been encountered.
                    Map<String, Object> dataModel = new TreeMap<String, Object>();
                    dataModel.put("currentUser", getCurrentUser());
                    dataModel.put("mailbox", mailbox);
                    dataModel.put("mail", mail);
                    dataModel.put("resourceRef", getRequest().getResourceRef());
                    dataModel.put("rootRef", getRequest().getRootRef());
                    dataModel.put("message", builder.toString());
                    getResponse().setEntity(
                            getHTMLTemplateRepresentation("mail_"
                                    + mail.getStatus() + ".html", dataModel));
                }
            } else {
                // Still a draft
                mail.setStatus(Mail.STATUS_DRAFT);
                getObjectsFacade().updateMail(mailbox, mail);
                getResponse().redirectSeeOther(getRequest().getResourceRef());
            }
        } else {
            getResponse().redirectSeeOther(getRequest().getResourceRef());
        }

    }
}
