/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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
import org.restlet.example.book.restlet.ch8.objects.Contact;
import org.restlet.example.book.restlet.ch8.objects.Mailbox;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a user's contact.
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

        if (getCurrentUser() != null) {
            // Authenticated access.
            setModifiable(true);
            // Get the contact and its parent mailbox thanks to their IDs taken
            // from the resource's URI.
            final String mailboxId = Reference.decode((String) request
                    .getAttributes().get("mailboxId"));
            this.mailbox = getObjectsFacade().getMailboxById(mailboxId);

            if (this.mailbox != null) {
                final String contactId = (String) request.getAttributes().get(
                        "contactId");
                this.contact = getObjectsFacade().getContactById(contactId);

                if (this.contact != null) {
                    this.hostedMailboxes = getObjectsFacade().getMailboxes();
                    getVariants().add(new Variant(MediaType.TEXT_HTML));
                }
            }
        } else {
            // Anonymous access.
            setModifiable(false);
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
        getObjectsFacade().deleteContact(this.mailbox, this.contact);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("currentUser", getCurrentUser());
        dataModel.put("mailbox", this.mailbox);
        dataModel.put("contact", this.contact);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());
        dataModel.put("hostedMailboxes", this.hostedMailboxes);

        return getHTMLTemplateRepresentation("contact.html", dataModel);
    }

    /**
     * Update the underlying contact according to the given representation.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);
        this.contact.setMailAddress(form.getFirstValue("mailAddress"));
        this.contact.setName(form.getFirstValue("name"));

        getObjectsFacade().updateContact(this.mailbox, this.contact);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

}
