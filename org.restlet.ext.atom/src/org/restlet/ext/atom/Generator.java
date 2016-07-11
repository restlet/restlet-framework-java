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

import org.restlet.data.Reference;
import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Identifies the agent used to generate a feed, for debugging and other
 * purposes.
 * 
 * @author Jerome Louvel
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
        this(null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param name
     *            Human-readable name for the generating agent.
     * @param uri
     *            Reference of the generating agent.
     * @param version
     *            Version of the generating agent.
     */
    public Generator(String name, Reference uri, String version) {
        this.uri = uri;
        this.version = version;
        this.name = name;
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
