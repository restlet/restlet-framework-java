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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes the root resources of an application.
 * 
 * @author Jerome Louvel
 */
public class ResourcesInfo extends DocumentedInfo {
    /** Base URI for each child resource identifier. */
    private Reference baseRef;

    /** List of child resources. */
    private List<ResourceInfo> resources;

    /**
     * Constructor.
     */
    public ResourcesInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ResourcesInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public ResourcesInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ResourcesInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the base URI for each child resource identifier.
     * 
     * @return The base URI for each child resource identifier.
     */
    public Reference getBaseRef() {
        return this.baseRef;
    }

    /**
     * Returns the list of child resources.
     * 
     * @return The list of child resources.
     */
    public List<ResourceInfo> getResources() {
        // Lazy initialization with double-check.
        List<ResourceInfo> r = this.resources;
        if (r == null) {
            synchronized (this) {
                r = this.resources;
                if (r == null) {
                    this.resources = r = new ArrayList<ResourceInfo>();
                }
            }
        }
        return r;
    }

    /**
     * Sets the base URI for each child resource identifier.
     * 
     * @param baseRef
     *            The base URI for each child resource identifier.
     */
    public void setBaseRef(Reference baseRef) {
        this.baseRef = baseRef;
    }

    /**
     * Sets the list of child resources.
     * 
     * @param resources
     *            The list of child resources.
     */
    public void setResources(List<ResourceInfo> resources) {
        this.resources = resources;
    }

    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        for (final ResourceInfo resourceInfo : getResources()) {
            resourceInfo.updateNamespaces(namespaces);
        }
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
        if (getBaseRef() != null) {
            attributes.addAttribute("", "base", null, "xs:anyURI", getBaseRef()
                    .toString());
        }

        if (getDocumentations().isEmpty() && getResources().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "resources", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "resources", null, attributes);

            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (final ResourceInfo resourceInfo : getResources()) {
                resourceInfo.writeElement(writer);
            }
            writer.endElement(APP_NAMESPACE, "resources");
        }
    }

}
