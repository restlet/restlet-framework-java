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

import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch9.objects.Mail;
import org.restlet.example.book.restlet.ch9.objects.Mailbox;
import org.restlet.ext.freemarker.TemplateRepresentation;
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

    /** The container of this mail. */
    private Mailbox mailbox;

    public MailResource(Context context, Request request, Response response) {
        super(context, request, response);
        String mailboxId = (String) request.getAttributes().get("mailboxId");
        mailbox = getDAOFactory().getMailboxDAO().getMailboxById(mailboxId);

        if (mailbox != null) {
            String mailId = (String) request.getAttributes().get("mailId");
            mail = getDAOFactory().getMailDAO().getMailById(mailId);

            if (mail != null) {
                getVariants().add(new Variant(MediaType.TEXT_HTML));
            }
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        getDAOFactory().getMailDAO().deleteMail(mail);
        // TODO revenir à la première page de l'application
        getResponse().redirectSeeOther("");
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", mailbox);
        dataModel.put("mail", mail);
        dataModel.put("resourceRef", getRequest().getResourceRef());

        TemplateRepresentation representation = new TemplateRepresentation(
                "mail.html", getFmcConfiguration(), dataModel, variant
                        .getMediaType());

        return representation;
    }

    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);
        mail.setMessage(form.getFirstValue("message"));
        // TODO comment figurer la liste des destinataires?
        // mail.setRecipients(form.getFirstValue("recipients"));
        // TODO comment gérer le statut? + la date d'envoi?
        mail.setStatus(form.getFirstValue("status"));
        // TODO comment gérer le statut?
        mail.setSubject(form.getFirstValue("subject"));
        // TODO comment gérer les tags?
        // mail.setTags(form.getFirstValue("tags"));

        getDAOFactory().getMailDAO().updateMail(mail);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

}
