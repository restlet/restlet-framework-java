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
 * Describes the root resources of an application.
 * 
 * @author Jerome Louvel
 */
public class ResourcesInfo {

    private Reference baseRef;

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    private List<ResourceInfo> resources;

    public Reference getBaseRef() {
        return baseRef;
    }

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

    public List<ResourceInfo> getResources() {
        // Lazy initialization with double-check.
        List<ResourceInfo> r = this.resources;
        if (r == null) {
            synchronized (this) {
                r = this.resources;
                if (r == null)
                    this.resources = r = new ArrayList<ResourceInfo>();
            }
        }
        return r;
    }

    public void setBaseRef(Reference baseRef) {
        this.baseRef = baseRef;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setResources(List<ResourceInfo> resources) {
        this.resources = resources;
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
        if (getBaseRef() != null) {
            attributes.addAttribute("", "base", null, "xs:anyURI", getBaseRef()
                    .toString());
        }

        if (getDocumentations().isEmpty() && getResources().isEmpty()) {
            writer.emptyElement("", "resources", null, attributes);
        } else {
            writer.startElement("", "resources", null, attributes);

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (ResourceInfo resourceInfo : getResources()) {
                resourceInfo.writeElement(writer);
            }
            writer.endElement("", "resources");
        }
    }

}
