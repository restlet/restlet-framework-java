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

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.util.List;

import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Allows inclusion of grammars by reference.
 * 
 * @author Jerome Louvel
 */
public class IncludeInfo extends DocumentedInfo {

    /** URI for the referenced definitions. */
    private Reference targetRef;

    public IncludeInfo() {
        super();
    }

    public IncludeInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    public IncludeInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    public IncludeInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the URI of the referenced definition.
     * 
     * @return The URI of the referenced definition.
     */
    public Reference getTargetRef() {
        return this.targetRef;
    }

    /**
     * Sets the URI of the referenced definition.
     * 
     * @param href
     *            The URI of the referenced definition.
     */
    public void setTargetRef(Reference href) {
        this.targetRef = href;
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
        if ((getTargetRef() != null) && (getTargetRef().toString() != null)) {
            attributes.addAttribute("", "href", null, "xs:anyURI",
                    getTargetRef().toString());
        }

        if (getDocumentations().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "include", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "include", null, attributes);
            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }
            writer.endElement(APP_NAMESPACE, "include");
        }
    }
}
