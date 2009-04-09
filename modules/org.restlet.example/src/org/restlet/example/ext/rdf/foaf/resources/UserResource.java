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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.example.ext.rdf.foaf.objects.Contact;
import org.restlet.example.ext.rdf.foaf.objects.User;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.RdfXmlRepresentation;
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
        final Form form = new Form(entity);
        this.user.setFirstName(form.getFirstValue("firstName"));
        this.user.setLastName(form.getFirstValue("lastName"));
        this.user.setImage(form.getFirstValue("image"));
        getObjectsFacade().updateUser(this.user);
        getResponse().redirectSeeOther(getRequest().getResourceRef());
    }

    /**
     * Generate the FOAF representation of this resource.
     */
    @Get("foaf")
    public Representation toFoaf() throws ResourceException {
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("user", this.user);
        dataModel.put("contacts", this.contacts);
        dataModel.put("resourceRef", getRequest().getResourceRef());
        dataModel.put("rootRef", getRequest().getRootRef());

        Reference RDF_SYNTAX = new Reference(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        Reference foaf = new Reference("http://xmlns.com/foaf/0.1/");
        Graph graph = new Graph();
        Reference userRef = new Reference(getRequest().getResourceRef()
                .toString()
                + ".foaf");
        graph.add(userRef, new Reference(RDF_SYNTAX, "type"), new Reference(
                foaf, "Person"));
        graph.add(userRef, new Reference(foaf, "name"), new Literal(user
                .getFirstName()
                + " " + user.getLastName()));
        graph.add(userRef, new Reference(foaf, "givenname"), new Literal(user
                .getFirstName()));
        graph.add(userRef, new Reference(foaf, "firstName"), new Literal(user
                .getFirstName()));
        graph.add(userRef, new Reference(foaf, "family_name"), new Literal(user
                .getLastName()));
        graph.add(userRef, new Reference(foaf, "img"), new Literal(user
                .getImage()));
        graph.add(userRef, new Reference(foaf, "homepage"), getRequest()
                .getResourceRef());

        for (Contact contact : this.user.getContacts()) {
            Reference contactRef = new Reference(getRequest().getResourceRef()
                    .toString()
                    + "contacts/" + contact.getId() + ".foaf");
            graph.add(userRef, new Reference(foaf, "knows"), contactRef);
            graph.add(contactRef, new Reference(RDF_SYNTAX, "type"),
                    new Reference(foaf, "Person"));
            graph.add(contactRef, new Reference(foaf, "name"), new Literal(
                    contact.getFirstName() + " " + contact.getLastName()));
            graph.add(contactRef, new Reference(foaf, "givenname"),
                    new Literal(contact.getFirstName()));
            graph.add(contactRef, new Reference(foaf, "firstName"),
                    new Literal(contact.getFirstName()));
            graph.add(contactRef, new Reference(foaf, "family_name"),
                    new Literal(contact.getLastName()));
            graph.add(contactRef, new Reference(foaf, "nickname"), new Literal(
                    contact.getNickname()));
            graph.add(contactRef, new Reference(foaf, "img"), new Literal(
                    contact.getImage()));
        }

        // return getTemplateRepresentation("user.foaf", dataModel,
        // MediaType.APPLICATION_RDF_XML);
        return new RdfXmlRepresentation(graph);
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
