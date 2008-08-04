/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
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
 * Resource for a list of contacts.
 */
public class ContactsResource extends BaseResource {

    /** The parent mailbox. */
    private Mailbox mailbox;

    /** The list of contacts. */
    private List<Contact> contacts;

    /**
     * The list of all mailboxes hosted on this server. It helps for the
     * creation of contacts.
     */
    private List<Mailbox> hostedMailboxes;

    public ContactsResource(Context context, Request request, Response response) {
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
                this.contacts = this.mailbox.getContacts();
                this.hostedMailboxes = getObjectsFacade().getMailboxes();
                getVariants().add(new Variant(MediaType.TEXT_HTML));
            }
        } else {
            // Anonymous access
            setModifiable(false);
        }
    }

    /**
     * Accept the representation of a new contact, and create it.
     */
    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);
        Contact contact = new Contact();
        contact.setMailAddress(form.getFirstValue("mailAddress"));
        contact.setName(form.getFirstValue("name"));

        contact = getObjectsFacade().createContact(this.mailbox, contact);

        getResponse().redirectSeeOther(
                getChildReference(getRequest().getResourceRef(), contact
                        .getId()));
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
        dataModel.put("contacts", this.contacts);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());
        dataModel.put("hostedMailboxes", this.hostedMailboxes);

        return getHTMLTemplateRepresentation("contacts.html", dataModel);
    }

}
