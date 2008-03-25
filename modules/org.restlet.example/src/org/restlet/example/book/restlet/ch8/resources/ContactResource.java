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
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.book.restlet.ch8.objects.Contact;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a user's contact.
 * 
 */
public class ContactResource extends BaseResource {

    /** The contact represented by this resource. */
    private Contact contact;

    /** The parent mailbox. */
    private Mailbox mailbox;

    /**
     * The list of all mailboxes hosted on this server. It helps for the
     * creation of contacts.
     */
    private List<Mailbox> hostedMailboxes;

    public ContactResource(Context context, Request request, Response response) {
        super(context, request, response);
        // Get the contact and its parent mailbox thanks to their IDs taken from
        // the resource's URI.
        String mailboxId = (String) request.getAttributes().get("mailboxId");
        mailbox = getObjectsFacade().getMailboxById(mailboxId);

        if (mailbox != null) {
            String contactId = (String) request.getAttributes()
                    .get("contactId");
            contact = getObjectsFacade().getContactById(contactId);

            if (contact != null) {
                hostedMailboxes = getObjectsFacade().getMailboxes();
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

    /**
     * Remove this resource.
     */
    @Override
    public void removeRepresentations() throws ResourceException {
        getObjectsFacade().deleteContact(mailbox, contact);
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
        dataModel.put("contact", contact);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());
        dataModel.put("hostedMailboxes", hostedMailboxes);

        TemplateRepresentation representation = new TemplateRepresentation(
                "contact.html", getFmcConfiguration(), dataModel, variant
                        .getMediaType());

        return representation;
    }

    /**
     * Update the underlying contact according to the given representation.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        Form form = new Form(entity);
        contact.setMailAddress(form.getFirstValue("mailAddress"));
        contact.setName(form.getFirstValue("name"));

        getObjectsFacade().updateContact(mailbox, contact);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

}
