/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.atom;

import static org.restlet.ext.atom.Feed.ATOM_NAMESPACE;

import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Element that describes a person, corporation, or similar entity (hereafter,
 * 'person').
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Person {

    /**
     * Email address associated with the person.
     */
    private volatile String email;

    /**
     * Human-readable name.
     */
    private volatile String name;

    /**
     * IRI associated with the person.
     */
    private volatile Reference uri;

    /**
     * Constructor.
     */
    public Person() {
        this(null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param uri
     *            The URI reference.
     * @param email
     *            The email address.
     */
    public Person(String name, Reference uri, String email) {
        this.name = name;
        this.uri = uri;
        this.email = email;
    }

    /**
     * Returns the email address associated with the person.
     * 
     * @return The email address associated with the person.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Returns the human-readable name.
     * 
     * @return The human-readable name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the IRI associated with the person.
     * 
     * @return The IRI associated with the person.
     */
    public Reference getUri() {
        return this.uri;
    }

    /**
     * Sets the email address.
     * 
     * @param email
     *            The email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the human-readable name.
     * 
     * @param name
     *            The human-readable name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the IRI associated with the person.
     * 
     * @param uri
     *            The IRI associated with the person.
     */
    public void setUri(Reference uri) {
        this.uri = uri;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @param localName
     *            The local name of the element.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer, String localName)
            throws SAXException {
        writer.startElement(ATOM_NAMESPACE, localName);

        if (getEmail() != null) {
            writer.dataElement(ATOM_NAMESPACE, "email", getEmail());
        }
        if (getName() != null) {
            writer.dataElement(ATOM_NAMESPACE, "name", getName());
        }
        if ((getUri() != null) && (getUri().toString() != null)) {
            writer.dataElement(ATOM_NAMESPACE, "uri", getUri().toString());
        }

        writer.endElement(ATOM_NAMESPACE, localName);
    }

}
