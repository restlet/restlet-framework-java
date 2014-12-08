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
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

/**
 * Resource for a user.
 */
public class UserResource extends BaseResource {

    /** The list of contacts of this user. */
    private List<Contact> contacts;

    /** The user represented by this resource. */
    private User user;

    @Override
    protected void doInit() throws ResourceException {
        // Get user thanks to its ID taken from the resource's URI.
        final String userId = (String) getRequestAttributes().get("userId");
        this.user = getObjectsFacade().getUserById(userId);

        if (this.user != null) {
            this.contacts = this.user.getContacts();
        } else {
            setExisting(false);
        }
    }

    /**
     * Remove this resource.
     */
    @Delete
    public void removeUser() throws ResourceException {
        getObjectsFacade().deleteUser(this.user);
        getResponse().redirectSeeOther(
                getRequest().getResourceRef().getParentRef());
    }

    /**
     * Update the underlying user according to the given representation.
     */
    @Put
    public void storeUser(Representation entity) throws ResourceException {
        Form form = new Form(entity);
        this.user.setFirstName(form.getFirstValue("firstName"));
        this.user.setLastName(form.getFirstValue("lastName"));
        this.user.setImage(form.getFirstValue("image"));

        getObjectsFacade().updateUser(this.user);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

    /**
     * Generates the FOAF representation of this resource.
     */
    @Get("rdf")
    public Representation toFoaf() throws ResourceException {
        return getFoafRepresentation(this.user, getRequest().getResourceRef());
    }

    /**
     * Generate the HTML representation of this resource.
     */
    @Get("html")
    public Representation toHtml() throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("user", this.user);
        dataModel.put("contacts", this.contacts);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        return getTemplateRepresentation("user.html", dataModel,
                MediaType.TEXT_HTML);
    }
}
