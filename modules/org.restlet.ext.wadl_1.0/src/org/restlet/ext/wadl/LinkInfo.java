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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Allows description of links between representations and resources.
 * 
 * @author Jerome Louvel
 */
public class LinkInfo {

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /**
     * Identifies the relationship of the resource identified by the link to the
     * resource whose representation the link is embedded in.
     */
    private String relationship;

    /**
     * Defines the capabilities of the resource that the link identifies.
     */
    private Reference resourceType;

    /**
     * Identifies the relationship of the resource whose representation the link
     * is embedded in to the resource identified by the link.
     */
    private String reverseRelationship;

    /**
     * Returns the list of documentation elements.
     * 
     * @return The list of documentation elements.
     */
    public List<DocumentationInfo> getDocumentations() {
        // Lazy initialization with double-check.
        List<DocumentationInfo> d = this.documentations;
        if (d == null) {
            synchronized (this) {
                d = this.documentations;
                if (d == null)
                    this.documentations = d = new ArrayList<DocumentationInfo>();
            }
        }
        return d;
    }

    /**
     * Returns the relationship attribute value.
     * 
     * @return The relationship attribute value.
     */
    public String getRelationship() {
        return relationship;
    }

    /**
     * Returns the reference to the resource type of the linked resource.
     * 
     * @return The reference to the resource type of the linked resource.
     */
    public Reference getResourceType() {
        return resourceType;
    }

    /**
     * Returns the reverse relationship attribute value.
     * 
     * @return The reverse relationship attribute value.
     */
    public String getReverseRelationship() {
        return reverseRelationship;
    }

    /**
     * Sets the list of documentation elements.
     * 
     * @param doc
     *                The list of documentation elements.
     */
    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    /**
     * Sets the relationship attribute value.
     * 
     * @param relationship
     *                The relationship attribute value.
     */
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    /**
     * The reference to the resource type of the linked resource.
     * 
     * @param resourceType
     *                The reference to the resource type of the linked resource.
     */
    public void setResourceType(Reference resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Sets the reverse relationship attribute value.
     * 
     * @param reverseRelationship
     *                The reverse relationship attribute value.
     */
    public void setReverseRelationship(String reverseRelationship) {
        this.reverseRelationship = reverseRelationship;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        if (getRelationship() != null && !getRelationship().equals("")) {
            attributes.addAttribute("", "rel", null, "xs:token",
                    getRelationship());
        }
        if (getReverseRelationship() != null
                && !getReverseRelationship().equals("")) {
            attributes.addAttribute("", "rev", null, "xs:token",
                    getReverseRelationship());
        }

        // TODO Prise en compte de ResourceType. Comme attribut?
        if (getResourceType() != null && getResourceType().toString() != null) {
            attributes.addAttribute("", "resource_type", null, "xs:anyURI",
                    getResourceType().toString());
        }

        if (getDocumentations().isEmpty()) {
            writer.emptyElement("", "link", null, attributes);
        } else {
            writer.startElement("", "link", null, attributes);

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            writer.endElement("link");
        }
    }

}
