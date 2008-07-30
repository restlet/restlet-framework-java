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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.example.book.restlet.ch8.objects.ObjectsException;
import org.restlet.example.book.restlet.ch8.objects.User;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a list of mailboxes.
 */
public class MailboxesResource extends BaseResource {

    /** The list of application's mailboxes. */
    private List<Mailbox> mailboxes;

    /** The list of users (creation form). */
    private List<User> users;

    public MailboxesResource(Context context, Request request, Response response) {
        super(context, request, response);

        if (getCurrentUser() != null) {
            // Authenticated access.
            if (getCurrentUser().isAdministrator()) {
                this.mailboxes = getObjectsFacade().getMailboxes();
                this.users = getObjectsFacade().getUsers();
            } else {
                this.mailboxes = getObjectsFacade().getMailboxes(
                        getCurrentUser());
            }
        }

        // Let anybody post new mails to this mailbox.
        setModifiable(true);

        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    /**
     * Accept the representation of a new mailbox, and create it.
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);

        Mailbox mailbox = new Mailbox();
        System.out.println(Reference.decode(form.getFirstValue("nickname")));
        mailbox.setNickname(form.getFirstValue("nickname"));
        final User owner = getObjectsFacade().getUserById(
                form.getFirstValue("ownerId"));
        mailbox.setOwner(owner);
        mailbox.setSenderName(owner.getFirstName() + " " + owner.getLastName());
        try {
            mailbox = getObjectsFacade().createMailbox(mailbox);
            getResponse().redirectSeeOther(getRequest().getResourceRef());
        } catch (final ObjectsException e) {
            final Map<String, Object> dataModel = new TreeMap<String, Object>();
            dataModel.put("currentUser", getCurrentUser());
            dataModel.put("mailboxes", this.mailboxes);
            dataModel.put("users", this.users);
            dataModel.put("resourceRef", getRequest().getResourceRef());
            dataModel.put("rootRef", getRequest().getRootRef());
            dataModel.put("ownerId", form.getFirstValue("ownerId"));
            dataModel.put("nickname", form.getFirstValue("nickname"));
            dataModel.put("errorMessage", e.getMessage());

            getResponse().setEntity(
                    getHTMLTemplateRepresentation("mailboxes.html", dataModel));
        }
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailboxes", this.mailboxes);
        dataModel.put("users", this.users);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getHTMLTemplateRepresentation("mailboxes.html", dataModel);
    }

}
