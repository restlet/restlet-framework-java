/*
 * Copyright 2005-2008 Noelios Technologies.
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
import org.xml.sax.helpers.AttributesImpl;

/**
 * Identifies the agent used to generate a feed, for debugging and other
 * purposes.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Generator {

    /** Human-readable name for the generating agent. */
    private volatile String name;

    /** Reference of the generating agent. */
    private volatile Reference uri;

    /** Version of the generating agent. */
    private volatile String version;

    /**
     * Constructor.
     */
    public Generator() {
        this.uri = null;
        this.version = null;
        this.name = null;
    }

    /**
     * Returns the human-readable name for the generating agent.
     * 
     * @return The human-readable name for the generating agent.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the reference of the generating agent.
     * 
     * @return The reference of the generating agent.
     */
    public Reference getUri() {
        return this.uri;
    }

    /**
     * Returns the version of the generating agent.
     * 
     * @return The version of the generating agent.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the human-readable name for the generating agent.
     * 
     * @param name
     *            The human-readable name for the generating agent.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the reference of the generating agent.
     * 
     * @param uri
     *            The reference of the generating agent.
     */
    public void setUri(Reference uri) {
        this.uri = uri;
    }

    /**
     * Sets the version of the generating agent.
     * 
     * @param version
     *            The version of the generating agent.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();

        if ((getUri() != null) && (getUri().toString() != null)) {
            attributes.addAttribute("", "uri", null, "atomURI", getUri()
                    .toString());
        }

        if (getVersion() != null) {
            attributes.addAttribute("", "version", null, "text", getVersion());
        }

        if (getName() != null) {
            writer.dataElement(ATOM_NAMESPACE, "generator", null, attributes,
                    getName());
        } else {
            writer.emptyElement(ATOM_NAMESPACE, "generator", null, attributes);
        }
    }

}
