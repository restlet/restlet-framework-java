/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.example.ext.rdf.foaf.resources;

import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.example.ext.rdf.foaf.objects.Contact;
import org.restlet.example.ext.rdf.foaf.objects.User;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

/**
 * Resource for a user's contact.
 */
public class ContactResource extends UserResource {

    /** The parent user. */
    private User user;

    /** The contact represented by this resource. */
    private Contact contact;

    public ContactResource(Context context, Request request, Response response) {
        super(context, request, response);

        // Get user thanks to its ID taken from the resource's URI.
        final String userId = Reference.decode((String) request.getAttributes()
                .get("userId"));
        this.user = getObjectsFacade().getUserById(userId);

        if (user != null) {
            // Get the contact and its parent mailbox thanks to their IDs taken
            // from the resource's URI.
            final String contactId = (String) request.getAttributes().get(
                    "contactId");
            this.contact = getObjectsFacade().getContactById(contactId);
        }
        getVariants().add(new Variant(MediaType.TEXT_HTML));
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
        getObjectsFacade().deleteContact(this.user, this.contact);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("user", this.user);
        dataModel.put("contact", this.contact);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getTemplateRepresentation("contact.html", dataModel,
                MediaType.TEXT_HTML);
    }

    /**
     * Update the underlying contact according to the given representation.
     */
    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        final Form form = new Form(entity);
        this.contact.setFirstName(form.getFirstValue("firstName"));
        this.contact.setLastName(form.getFirstValue("lastName"));
        this.contact.setImage(form.getFirstValue("image"));
        this.contact.setNickname(form.getFirstValue("nickname"));

        getObjectsFacade().updateContact(this.user, this.contact);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

}
