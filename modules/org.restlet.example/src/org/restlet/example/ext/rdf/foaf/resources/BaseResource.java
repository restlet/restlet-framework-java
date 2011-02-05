/**
 * Copyright 2005-2011 Noelios Technologies.
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

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.example.ext.rdf.foaf.Application;
import org.restlet.example.ext.rdf.foaf.objects.Contact;
import org.restlet.example.ext.rdf.foaf.objects.ObjectsFacade;
import org.restlet.example.ext.rdf.foaf.objects.User;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.RdfRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

import freemarker.template.Configuration;

/**
 * Base resource class that supports common behaviours or attributes shared by
 * all resources.
 */
public class BaseResource extends ServerResource {

    static final String RDF_SYNTAX_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

    /**
     * Returns the FOAF representation of a user.
     * 
     * @param user
     *            The user.
     * @param userRef
     *            Its URI.
     * @return The FOAF representation of a user.
     */
    protected Representation getFoafRepresentation(User user, Reference userRef) {
        Graph graph = new Graph();
        addFoaf(graph, user, userRef);
        return new RdfRepresentation(graph, MediaType.TEXT_XML);
    }

    /**
     * Returns the FOAF representation of a contact.
     * 
     * @param contact
     *            The contact.
     * @param contactRef
     *            Its URI.
     * @return The FOAF representation of a contact.
     */
    protected Representation getFoafRepresentation(Contact contact,
            Reference contactRef) {
        Graph graph = new Graph();
        addFoaf(graph, contact, contactRef);
        return new RdfRepresentation(graph, MediaType.TEXT_XML);
    }

    /**
     * Completes the given set of links with the links due to the contact.
     * 
     * @param graph
     *            The graph to complete.
     * @param contact
     *            The contact.
     * @param contactRef
     *            Its URI.
     */
    private void addFoaf(Graph graph, Contact contact, Reference contactRef) {
        addFoaf(graph, (User) contact, contactRef);
        addFoafProperty(graph, contactRef, "nickname", contact.getNickname());
    }

    /**
     * Completes the given set of links with the links due to the user.
     * 
     * @param graph
     *            The graph to complete.
     * @param user
     *            The user.
     * @param contactRef
     *            Its URI.
     */
    private void addFoaf(Graph graph, User user, Reference userRef) {
        addLink(graph, userRef, RDF_SYNTAX_NS + "type", FOAF_NS + "Person");
        addFoafProperty(graph, userRef, "name", user.getFirstName() + " "
                + user.getLastName());
        addFoafProperty(graph, userRef, "givenname", user.getFirstName());
        addFoafProperty(graph, userRef, "firstName", user.getFirstName());
        addFoafProperty(graph, userRef, "family_name", user.getLastName());
        addFoafProperty(graph, userRef, "img", user.getImage());
        addLink(graph, userRef, FOAF_NS + "homepage", userRef);

        for (Contact contact : user.getContacts()) {
            Reference contactRef = null;
            if (contact.getFoafUri() != null) {
                contactRef = new Reference(contact.getFoafUri());
            } else {
                contactRef = new Reference(userRef + "/contacts/"
                        + contact.getId());
            }
            addLink(graph, userRef, FOAF_NS + "knows", contactRef);
            addFoaf(graph, contact, contactRef);
        }
    }

    private void addFoafProperty(Graph graph, Reference subject,
            String predicate, String object) {
        graph.add(subject, new Reference(FOAF_NS + predicate), new Literal(
                object));
    }

    private void addLink(Graph graph, Reference subject, String predicate,
            Reference object) {
        graph.add(subject, new Reference(predicate), object);
    }

    private void addLink(Graph graph, Reference subject, String predicate,
            String object) {
        graph.add(subject, new Reference(predicate), new Reference(object));
    }

    /**
     * Returns the reference of a resource according to its id and the reference
     * of its "parent".
     * 
     * @param parentRef
     *            parent reference.
     * @param childId
     *            id of this resource
     * @return the reference object of the child resource.
     */
    protected Reference getChildReference(Reference parentRef, String childId) {
        if (parentRef.getIdentifier().endsWith("/")) {
            return new Reference(parentRef.getIdentifier() + childId);
        }

        return new Reference(parentRef.getIdentifier() + "/" + childId);
    }

    /**
     * Returns the Freemarker's configuration object used for the generation of
     * all HTML representations.
     * 
     * @return the Freemarker's configuration object.
     */
    private Configuration getFmcConfiguration() {
        final Application application = (Application) getApplication();
        return application.getFmc();
    }

    /**
     * Gives access to the Objects layer.
     * 
     * @return a facade.
     */
    protected ObjectsFacade getObjectsFacade() {
        final Application application = (Application) getApplication();
        return application.getObjectsFacade();
    }

    /**
     * Returns a templated representation dedicated to HTML content.
     * 
     * @param templateName
     *            the name of the template.
     * @param dataModel
     *            the collection of data processed by the template engine.
     * @param mediaType
     *            The media type of the representation.
     * @return the representation.
     */
    protected Representation getTemplateRepresentation(String templateName,
            Map<String, Object> dataModel, MediaType mediaType) {
        // The template representation is based on Freemarker.
        return new TemplateRepresentation(templateName, getFmcConfiguration(),
                dataModel, mediaType);
    }
}
