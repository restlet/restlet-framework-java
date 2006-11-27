/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.atom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Client;
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
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Service extends SaxRepresentation {
	/** Atom Publishing Protocol namespace. */
	public static final String NAMESPACE = "http://purl.org/atom/app#";

	/**
	 * The HTTP client connector.
	 */
	private Client client;

	/**
	 * The reference.
	 */
	private Reference reference;

	/**
	 * The list of workspaces.
	 */
	private List<Workspace> workspaces;

	/**
	 * Constructor.
	 * 
	 * @param client
	 *            The HTTP client connector.
	 */
	public Service(Client client) {
		super(new MediaType("***"));
		this.client = client;
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
	 * @param client
	 *            The HTTP client connector.
	 * @param serviceUri
	 *            The service URI.
	 * @throws IOException
	 */
	public Service(Client client, String serviceUri) throws IOException {
		this(client, serviceUri, client.get(serviceUri).getEntity());
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
	 * @param client
	 *            The HTTP client connector.
	 * @param serviceUri
	 *            The service URI.
	 * @param xmlService
	 *            The XML introspection document.
	 * @throws IOException
	 */
	public Service(Client client, String serviceUri, Representation xmlService)
			throws IOException {
		super(xmlService);
		this.client = client;
		this.reference = new Reference(serviceUri);
		parse(new ContentReader(this));
	}

	/**
	 * Returns the HTTP client connector.
	 * 
	 * @return The HTTP client connector.
	 */
	public Client getClient() {
		return this.client;
	}

	/**
	 * Sets the HTTP client connector.
	 * 
	 * @param client
	 *            The HTTP client connector.
	 */
	public void setClient(Client client) {
		this.client = client;
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
	 * Sets the hypertext reference.
	 * 
	 * @param ref
	 *            The hypertext reference.
	 */
	public void setReference(Reference ref) {
		this.reference = ref;
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
	 * Retrieves a resource representation.
	 * 
	 * @param uri
	 *            The resource URI.
	 * @return The resource representation.
	 */
	public Representation getResource(String uri) {
		return getClient().get(uri).getEntity();
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
		return getClient().put(uri, updatedRepresentation).getStatus();
	}

	/**
	 * Deletes a resource.
	 * 
	 * @param uri
	 *            The resource URI.
	 * @return The result status.
	 */
	public Status deleteResource(String uri) {
		return getClient().delete(uri).getStatus();
	}

	/**
	 * Writes the representation to a XML writer.
	 * 
	 * @param writer
	 *            The XML writer to write to.
	 * @throws IOException
	 */
	public void write(XmlWriter writer) throws IOException {
		try {
			writer.setDataFormat(true);
			writer.setIndentStep(3);
			writer.startDocument();
			writer.startElement(NAMESPACE, "service");

			// for(Workspace ws : getWorkspaces())
			// {
			// writer.startElement(NAMESPACE, "workspace");
			// // ...
			// writer.endElement(NAMESPACE, "workspace");
			// }

			writer.endElement(NAMESPACE, "service");
			writer.endDocument();
		} catch (SAXException se) {
			throw new IOException("Couldn't write the service representation: "
					+ se.getMessage());
		}
	}

	// --------------------
	// Content reader part
	// --------------------
	private class ContentReader extends DefaultHandler {
		private final static int IN_NONE = 0;

		private final static int IN_SERVICE = 1;

		private final static int IN_WORKSPACE = 2;

		private final static int IN_COLLECTION = 3;

		private final static int IN_MEMBER_TYPE = 4;

		private int state = IN_NONE;

		private Service currentService = null;

		private Workspace currentWorkspace = null;

		private Collection currentCollection = null;

		private StringBuilder contentBuffer = null;

		public ContentReader(Service service) {
			this.currentService = service;
		}

		/**
		 * Receive notification of the beginning of a document.
		 */
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
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if (uri.equalsIgnoreCase(NAMESPACE)) {
				if (localName.equalsIgnoreCase("service")) {
					state = IN_SERVICE;
				} else if (localName.equalsIgnoreCase("workspace")) {
					if (state == IN_SERVICE) {
						currentWorkspace = new Workspace(this.currentService,
								attrs.getValue("title"));
						state = IN_WORKSPACE;
					}
				} else if (localName.equalsIgnoreCase("collection")) {
					if (state == IN_WORKSPACE) {
						currentCollection = new Collection(currentWorkspace,
								attrs.getValue("title"), attrs.getValue("href"));
						state = IN_COLLECTION;
					}
				} else if (localName.equalsIgnoreCase("member-type")) {
					if (state == IN_COLLECTION) {
						contentBuffer = new StringBuilder();
						state = IN_MEMBER_TYPE;
					}
				}
			}
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
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (state == IN_MEMBER_TYPE) {
				contentBuffer.append(ch, start, length);
			}
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
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (localName.equalsIgnoreCase("service")) {
				state = IN_NONE;
			} else if (localName.equalsIgnoreCase("workspace")) {
				if (state == IN_WORKSPACE) {
					getWorkspaces().add(currentWorkspace);
					currentWorkspace = null;
					state = IN_SERVICE;
				}
			} else if (localName.equalsIgnoreCase("collection")) {
				if (state == IN_COLLECTION) {
					currentWorkspace.getCollections().add(currentCollection);
					currentCollection = null;
					state = IN_WORKSPACE;
				}
			} else if (localName.equalsIgnoreCase("member-type")) {
				if (state == IN_MEMBER_TYPE) {
					String memberType = contentBuffer.toString();

					if (memberType.equalsIgnoreCase("entry")) {
						currentCollection.setMemberType(MemberType.ENTRY);
					} else if (memberType.equalsIgnoreCase("media")) {
						currentCollection.setMemberType(MemberType.MEDIA);
					}

					state = IN_COLLECTION;
				}
			}
		}

		/**
		 * Receive notification of the end of a document.
		 */
		public void endDocument() throws SAXException {
			this.state = IN_NONE;
			this.currentWorkspace = null;
			this.currentCollection = null;
			this.contentBuffer = null;
		}
	}

}
