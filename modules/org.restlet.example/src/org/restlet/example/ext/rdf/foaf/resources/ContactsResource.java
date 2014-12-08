/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.ext.rdf.foaf.resources;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.example.ext.rdf.foaf.objects.Contact;
import org.restlet.example.ext.rdf.foaf.objects.User;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Resource for a list of contacts.
 */
public class ContactsResource extends BaseResource {

    /** The list of contacts. */
    private List<Contact> contacts;

    /** The parent user. */
    private User user;

    /**
     * Accept the representation of a new contact, and create it.
     */
    @Post
    public void acceptContact(Representation entity) throws ResourceException {
        Form form = new Form(entity);
        Contact contact = new Contact();
        contact.setFirstName(form.getFirstValue("firstName"));
        contact.setLastName(form.getFirstValue("lastName"));
        contact.setImage(form.getFirstValue("image"));
        contact.setNickname(form.getFirstValue("nickname"));
        contact.setFoafUri(form.getFirstValue("foafUri"));

        contact = getObjectsFacade().createContact(this.user, contact);
        getResponse().redirectSeeOther(
                getChildReference(getRequest().getResourceRef(),
                        contact.getId()));
    }

    @Override
    protected void doInit() throws ResourceException {
        // Get user thanks to its ID taken from the resource's URI.
        final String userId = (String) getRequestAttributes().get("userId");
        this.user = getObjectsFacade().getUserById(userId);
        if (user != null) {
            this.contacts = this.user.getContacts();
        } else {
            setExisting(false);
        }
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Get
    public Representation toHtml(Variant variant) throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("user", this.user);
        dataModel.put("contacts", this.contacts);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getTemplateRepresentation("contacts.html", dataModel,
                MediaType.TEXT_HTML);
    }

}
