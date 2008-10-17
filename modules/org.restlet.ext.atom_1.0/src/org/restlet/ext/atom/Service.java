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
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.SaxRepresentation;
import org.restlet.util.XmlWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents an Atom introspection document.
 * 
 * @author Jerome Louvel
 */
public class Service extends SaxRepresentation {
    // --------------------
    // Content reader part
    // --------------------
    private class ContentReader extends DefaultHandler {
        private final static int IN_COLLECTION = 3;

        private final static int IN_MEMBER_TYPE = 4;

        private final static int IN_NONE = 0;

        private final static int IN_SERVICE = 1;

        private final static int IN_WORKSPACE = 2;

        private StringBuilder contentBuffer = null;

        private Collection currentCollection = null;

        private Service currentService = null;

        private Workspace currentWorkspace = null;

        private int state = IN_NONE;

        public ContentReader(Service service) {
            this.currentService = service;
        }

        /**
         * Receive notification of character data.
         * 
         * @param ch
         *            The characters from the XML document.
         * @param start
         *            The start position in the array.
         * @param length
         *            The number of characters to read from the array.
         */
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if (this.state == IN_MEMBER_TYPE) {
                this.contentBuffer.append(ch, start, length);
            }
        }

        /**
         * Receive notification of the end of a document.
         */
        @Override
        public void endDocument() throws SAXException {
            this.state = IN_NONE;
            this.currentWorkspace = null;
            this.currentCollection = null;
            this.contentBuffer = null;
        }

        /**
         * Receive notification of the end of an element.
         * 
         * @param uri
         *            The Namespace URI, or the empty string if the element has
         *            no Namespace URI or if Namespace processing is not being
         *            performed.
         * @param localName
         *            The local name (without prefix), or the empty string if
         *            Namespace processing is not being performed.
         * @param qName
         *            The qualified XML name (with prefix), or the empty string
         *            if qualified names are not available.
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (localName.equalsIgnoreCase("service")) {
                this.state = IN_NONE;
            } else if (localName.equalsIgnoreCase("workspace")) {
                if (this.state == IN_WORKSPACE) {
                    getWorkspaces().add(this.currentWorkspace);
                    this.currentWorkspace = null;
                    this.state = IN_SERVICE;
                }
            } else if (localName.equalsIgnoreCase("collection")) {
                if (this.state == IN_COLLECTION) {
                    this.currentWorkspace.getCollections().add(
                            this.currentCollection);
                    this.currentCollection = null;
                    this.state = IN_WORKSPACE;
                }
            } else if (localName.equalsIgnoreCase("member-type")) {
                if (this.state == IN_MEMBER_TYPE) {
                    final String memberType = this.contentBuffer.toString();

                    if (memberType.equalsIgnoreCase("entry")) {
                        this.currentCollection.setMemberType(MemberType.ENTRY);
                    } else if (memberType.equalsIgnoreCase("media")) {
                        this.currentCollection.setMemberType(MemberType.MEDIA);
                    }

                    this.state = IN_COLLECTION;
                }
            }
        }

        /**
         * Receive notification of the beginning of a document.
         */
        @Override
        public void startDocument() throws SAXException {
            this.state = IN_NONE;
            this.currentWorkspace = null;
            this.currentCollection = null;
            this.contentBuffer = null;
        }

        /**
         * Receive notification of the beginning of an element.
         * 
         * @param uri
         *            The Namespace URI, or the empty string if the element has
         *            no Namespace URI or if Namespace processing is not being
         *            performed.
         * @param localName
         *            The local name (without prefix), or the empty string if
         *            Namespace processing is not being performed.
         * @param qName
         *            The qualified name (with prefix), or the empty string if
         *            qualified names are not available.
         * @param attrs
         *            The attributes attached to the element. If there are no
         *            attributes, it shall be an empty Attributes object. The
         *            value of this object after startElement returns is
         *            undefined.
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attrs) throws SAXException {
            if (uri.equalsIgnoreCase(APP_NAMESPACE)) {
                if (localName.equalsIgnoreCase("service")) {
                    this.state = IN_SERVICE;
                } else if (localName.equalsIgnoreCase("workspace")) {
                    if (this.state == IN_SERVICE) {
                        this.currentWorkspace = new Workspace(
                                this.currentService, attrs.getValue("title"));
                        this.state = IN_WORKSPACE;
                    }
                } else if (localName.equalsIgnoreCase("collection")) {
                    if (this.state == IN_WORKSPACE) {
                        this.currentCollection = new Collection(
                                this.currentWorkspace, attrs.getValue("title"),
                                attrs.getValue("href"));
                        this.state = IN_COLLECTION;
                    }
                } else if (localName.equalsIgnoreCase("member-type")) {
                    if (this.state == IN_COLLECTION) {
                        this.contentBuffer = new StringBuilder();
                        this.state = IN_MEMBER_TYPE;
                    }
                }
            }
        }
    }

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
        this(new Client(Protocol.HTTP), serviceUri);
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
        this(new Client(Protocol.HTTP), serviceUri, xmlService);
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
        parse(new ContentReader(this));
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
