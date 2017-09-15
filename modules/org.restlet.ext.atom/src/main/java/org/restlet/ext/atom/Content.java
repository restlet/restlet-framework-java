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

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Representation;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Either contains or links to the content of the entry.
 * 
 * @author Jerome Louvel
 */
public class Content {

    /** Reference to the external representation. */
    private volatile Reference externalRef;

    /** Expected media type of the external content. */
    private volatile MediaType externalType;

    /** Representation for inline content. */
    private volatile Representation inlineContent;

    /** Must the content be encoded or not? */
    private boolean toEncode;

    /**
     * Constructor.
     */
    public Content() {
        this.inlineContent = null;
        this.externalRef = null;
        this.externalType = null;
        this.toEncode = true;
    }

    /**
     * Returns the reference to the external representation.
     * 
     * @return The reference to the external representation.
     */
    public Reference getExternalRef() {
        return this.externalRef;
    }

    /**
     * Returns the expected media type of the external content.
     * 
     * @return The expected media type of the external content.
     */
    public MediaType getExternalType() {
        return this.externalType;
    }

    /**
     * Returns the representation for inline content.
     * 
     * @return The representation for inline content.
     */
    public Representation getInlineContent() {
        return this.inlineContent;
    }

    /**
     * Indicates if the content is available externally.
     * 
     * @return True if the content is available externally.
     */
    public boolean isExternal() {
        return (this.externalRef != null);
    }

    /**
     * Indicates if the content is available inline.
     * 
     * @return True if the content is available inline.
     */
    public boolean isInline() {
        return (this.inlineContent != null);
    }

    /**
     * Returns true if the content is to be encoded.
     * 
     * @return True if the content is to be encoded.
     */
    public boolean isToEncode() {
        return toEncode;
    }

    /**
     * Sets the reference to the external representation.
     * 
     * @param externalRef
     *            The reference to the external representation.
     */
    public void setExternalRef(Reference externalRef) {
        this.externalRef = externalRef;
    }

    /**
     * Sets the expected media type of the external content.
     * 
     * @param externalType
     *            The expected media type of the external content.
     */
    public void setExternalType(MediaType externalType) {
        this.externalType = externalType;
    }

    /**
     * Sets the representation for inline content.
     * 
     * @param inlineContent
     *            The representation for inline content.
     */
    public void setInlineContent(Representation inlineContent) {
        this.inlineContent = inlineContent;
    }

    /**
     * Indicates if the content is to be encoded.
     * 
     * @param toEncode
     *            True if the content is to be encoded.
     */
    public void setToEncode(boolean toEncode) {
        this.toEncode = toEncode;
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
        String strContent = null;

        if (getInlineContent() != null) {
            final MediaType mediaType = getInlineContent().getMediaType();
            String type = null;

            if ((mediaType != null) && (mediaType.getSubType() != null)) {
                if (mediaType.getSubType().contains("xhtml")) {
                    type = "xhtml";
                } else if (mediaType.getSubType().contains("html")) {
                    type = "html";
                } else {
                    type = mediaType.getName();
                }
            }

            if (type == null) {
                type = "text";
            }

            attributes.addAttribute("", "type", null, "text", type);

            try {
                strContent = getInlineContent().getText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if ((getExternalType() != null)
                    && (getExternalType().toString() != null)) {
                attributes.addAttribute("", "type", null, "atomMediaType",
                        getExternalType().toString());
            }

            if ((getExternalRef() != null)
                    && (getExternalRef().toString() != null)) {
                attributes.addAttribute("", "src", null, "atomURI",
                        getExternalRef().toString());
            }
        }

        if (strContent == null) {
            writer.emptyElement(ATOM_NAMESPACE, "content", null, attributes);
        } else {
            if (isToEncode()) {
                writer.dataElement(ATOM_NAMESPACE, "content", null, attributes,
                        strContent);
            } else {
                writer.startElement(ATOM_NAMESPACE, "content", null, attributes);
                try {
                    writer.getWriter().write(strContent);
                } catch (IOException e) {
                    throw new SAXException(e);
                }
                writer.endElement(ATOM_NAMESPACE, "content", null);
            }
        }
    }
}
