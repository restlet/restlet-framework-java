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

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.atom.internal.ServiceContentReader;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Represents an Atom introspection document.
 * 
 * @author Jerome Louvel
 */
public class Service extends SaxRepresentation {
    /** Atom Publishing Protocol namespace. */
    public static final String APP_NAMESPACE = "http://www.w3.org/2007/app";

    /**
     * The client HTTP dispatcher.
     */
    private volatile Uniform clientDispatcher;

    /**
     * The reference.
     */
    private volatile Reference reference;

    /**
     * The list of workspaces.
     */
    private volatile List<Workspace> workspaces;

    /**
     * Constructor.
     * 
     * @param context
     *            The context from which the client dispatcher will be
     *            retrieved.
     * @param serviceUri
     *            The service URI.
     * @throws IOException
     */
    public Service(Context context, String serviceUri) throws IOException {
        this(context.getClientDispatcher(), serviceUri, context
                .getClientDispatcher().get(serviceUri).getEntity());
    }

    /**
     * Constructor.
     * 
     * @param serviceUri
     *            The service URI.
     * @throws IOException
     */
    public Service(String serviceUri) throws IOException {
        this(new Client(new Reference(serviceUri).getSchemeProtocol()),
                serviceUri);
    }

    /**
     * Constructor.
     * 
     * @param serviceUri
     *            The service URI.
     * @param xmlService
     *            The XML introspection document.
     * @throws IOException
     */
    public Service(String serviceUri, Representation xmlService)
            throws IOException {
        this(new Client(new Reference(serviceUri).getSchemeProtocol()),
                serviceUri, xmlService);
    }

    /**
     * Constructor.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     */
    public Service(Uniform clientDispatcher) {
        super(new MediaType("***"));
        this.clientDispatcher = clientDispatcher;
    }

    /**
     * Constructor.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     * @param serviceUri
     *            The service URI.
     * @throws IOException
     */
    public Service(Uniform clientDispatcher, String serviceUri)
            throws IOException {
        this(clientDispatcher, serviceUri, clientDispatcher.get(serviceUri)
                .getEntity());
    }

    /**
     * Constructor.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     * @param serviceUri
     *            The service URI.
     * @param xmlService
     *            The XML introspection document.
     * @throws IOException
     */
    public Service(Uniform clientDispatcher, String serviceUri,
            Representation xmlService) throws IOException {
        super(xmlService);
        this.clientDispatcher = clientDispatcher;
        this.reference = new Reference(serviceUri);
        parse(new ServiceContentReader(this));
    }

    /**
     * Deletes a resource.
     * 
     * @param uri
     *            The resource URI.
     * @return The result status.
     */
    public Status deleteResource(String uri) {
        return getClientDispatcher().delete(uri).getStatus();
    }

    /**
     * Returns the client HTTP dispatcher.
     * 
     * @return The client HTTP dispatcher.
     */
    public Uniform getClientDispatcher() {
        return this.clientDispatcher;
    }

    /**
     * Returns the hypertext reference.
     * 
     * @return The hypertext reference.
     */
    public Reference getReference() {
        return this.reference;
    }

    /**
     * Retrieves a resource representation.
     * 
     * @param uri
     *            The resource URI.
     * @return The resource representation.
     */
    public Representation getResource(String uri) {
        return getClientDispatcher().get(uri).getEntity();
    }

    /**
     * Returns the list of workspaces.
     * 
     * @return The list of workspaces.
     */
    public List<Workspace> getWorkspaces() {
        if (this.workspaces == null) {
            this.workspaces = new ArrayList<Workspace>();
        }

        return this.workspaces;
    }

    /**
     * Sets the client HTTP dispatcher.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     */
    public void setClientDispatcher(Uniform clientDispatcher) {
        this.clientDispatcher = clientDispatcher;
    }

    /**
     * Sets the hypertext reference.
     * 
     * @param ref
     *            The hypertext reference.
     */
    public void setReference(Reference ref) {
        this.reference = ref;
    }

    /**
     * Updates a resource representation.
     * 
     * @param uri
     *            The resource URI.
     * @return The resource representation.
     */
    public Status updateResource(String uri,
            Representation updatedRepresentation) {
        return getClientDispatcher().put(uri, updatedRepresentation)
                .getStatus();
    }

    /**
     * Writes the representation to a XML writer.
     * 
     * @param writer
     *            The XML writer to write to.
     * @throws IOException
     */
    @Override
    public void write(XmlWriter writer) throws IOException {
        try {
            writer.forceNSDecl(APP_NAMESPACE, "");
            writer.forceNSDecl(ATOM_NAMESPACE, "atom");
            writer.setDataFormat(true);
            writer.setIndentStep(3);
            writer.startDocument();
            writer.startElement(APP_NAMESPACE, "service");

            for (final Workspace workspace : getWorkspaces()) {
                workspace.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "service");
            writer.endDocument();
        } catch (SAXException se) {
            throw new IOException("Couldn't write the service representation: "
                    + se.getMessage());
        }
    }

}
