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

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.atom.internal.ServiceContentReader;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Representation;
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
     * The base reference used to resolve relative references found within the
     * scope of the xml:base attribute.
     */
    private volatile Reference baseReference;

    /**
     * The client HTTP dispatcher.
     */
    private volatile Restlet clientDispatcher;

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
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     */
    public Service(Client clientDispatcher) {
        super(new MediaType("***"));
        setNamespaceAware(true);
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
    public Service(Client clientDispatcher, String serviceUri)
            throws IOException {
        this(clientDispatcher, serviceUri, clientDispatcher.handle(
                new Request(Method.GET, serviceUri)).getEntity());
    }

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
                .getClientDispatcher()
                .handle(new Request(Method.GET, serviceUri)).getEntity());
    }

    /**
     * Constructor.
     * 
     * @param xmlService
     *            The XML introspection document.
     * @throws IOException
     */
    public Service(Representation xmlService) throws IOException {
        this(null, null, xmlService);
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
    public Service(Restlet clientDispatcher, String serviceUri,
            Representation xmlService) throws IOException {
        super(xmlService);
        setNamespaceAware(true);
        this.clientDispatcher = clientDispatcher;
        this.reference = (serviceUri == null) ? null
                : new Reference(serviceUri);
        parse(new ServiceContentReader(this));
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
     * Deletes a resource.
     * 
     * @param uri
     *            The resource URI.
     * @return The result status.
     */
    public Status deleteResource(String uri) {
        return getClientDispatcher().handle(new Request(Method.DELETE, uri))
                .getStatus();
    }

    /**
     * Returns the base reference used to resolve relative references found
     * within the scope of the xml:base attribute.
     * 
     * @return The base reference used to resolve relative references found
     *         within the scope of the xml:base attribute.
     */
    public Reference getBaseReference() {
        return baseReference;
    }

    /**
     * Returns the client HTTP dispatcher.
     * 
     * @return The client HTTP dispatcher.
     */
    public Restlet getClientDispatcher() {
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
        return getClientDispatcher().handle(new Request(Method.GET, uri))
                .getEntity();
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
     * Sets the base reference used to resolve relative references found within
     * the scope of the xml:base attribute.
     * 
     * @param baseReference
     *            The base reference used to resolve relative references found
     *            within the scope of the xml:base attribute.
     */
    public void setBaseReference(Reference baseReference) {
        this.baseReference = baseReference;
    }

    /**
     * Sets the client HTTP dispatcher.
     * 
     * @param clientDispatcher
     *            The client HTTP dispatcher.
     */
    public void setClientDispatcher(Client clientDispatcher) {
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
        return getClientDispatcher().handle(
                new Request(Method.PUT, uri, updatedRepresentation))
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
