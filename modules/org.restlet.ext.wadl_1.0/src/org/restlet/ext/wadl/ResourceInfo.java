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
import java.util.Iterator;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes a class of closely related resources.
 * 
 * @author Jerome Louvel
 */
public class ResourceInfo {
    /** List of child resources. */
    private List<ResourceInfo> childResources;

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /** Identifier for that element. */
    private String identifier;

    /** List of supported methods. */
    private List<MethodInfo> methods;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** URI template for the identifier of the resource. */
    private String path;

    /** Media type for the query component of the resource URI. */
    private MediaType queryType;

    /** List of references to resource type elements. */
    private List<Reference> type;

    /**
     * Returns the list of child resources.
     * 
     * @return The list of child resources.
     */
    public List<ResourceInfo> getChildResources() {
        // Lazy initialization with double-check.
        List<ResourceInfo> r = this.childResources;
        if (r == null) {
            synchronized (this) {
                r = this.childResources;
                if (r == null)
                    this.childResources = r = new ArrayList<ResourceInfo>();
            }
        }
        return r;
    }

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
     * Returns the identifier for that element.
     * 
     * @return The identifier for that element.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the list of supported methods.
     * 
     * @return The list of supported methods.
     */
    public List<MethodInfo> getMethods() {
        // Lazy initialization with double-check.
        List<MethodInfo> m = this.methods;
        if (m == null) {
            synchronized (this) {
                m = this.methods;
                if (m == null)
                    this.methods = m = new ArrayList<MethodInfo>();
            }
        }
        return m;
    }

    /**
     * Returns the list of parameters.
     * 
     * @return The list of parameters.
     */
    public List<ParameterInfo> getParameters() {
        // Lazy initialization with double-check.
        List<ParameterInfo> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null)
                    this.parameters = p = new ArrayList<ParameterInfo>();
            }
        }
        return p;
    }

    /**
     * Returns the URI template for the identifier of the resource.
     * 
     * @return The URI template for the identifier of the resource.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the media type for the query component of the resource URI.
     * 
     * @return The media type for the query component of the resource URI.
     */
    public MediaType getQueryType() {
        return queryType;
    }

    /**
     * Returns the list of references to resource type elements.
     * 
     * @return The list of references to resource type elements.
     */
    public List<Reference> getType() {
        // Lazy initialization with double-check.
        List<Reference> t = this.type;
        if (t == null) {
            synchronized (this) {
                t = this.type;
                if (t == null)
                    this.type = t = new ArrayList<Reference>();
            }
        }
        return t;
    }

    /**
     * Sets the list of child resources.
     * 
     * @param resources
     *                The list of child resources.
     */
    public void setChildResources(List<ResourceInfo> resources) {
        this.childResources = resources;
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
     * Sets the identifier for that element.
     * 
     * @param identifier
     *                The identifier for that element.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the list of supported methods.
     * 
     * @param methods
     *                The list of supported methods.
     */
    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *                The list of parameters.
     */
    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the URI template for the identifier of the resource.
     * 
     * @param path
     *                The URI template for the identifier of the resource.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the media type for the query component of the resource URI.
     * 
     * @param queryType
     *                The media type for the query component of the resource
     *                URI.
     */
    public void setQueryType(MediaType queryType) {
        this.queryType = queryType;
    }

    /**
     * Sets the list of references to resource type elements.
     * 
     * @param type
     *                The list of references to resource type elements.
     */
    public void setType(List<Reference> type) {
        this.type = type;
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
        if (getIdentifier() != null && !getIdentifier().equals("")) {
            attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
        }

        if (getPath() != null && !getPath().equals("")) {
            attributes.addAttribute("", "path", null, "xs:string", getPath());
        }

        if (getQueryType() != null) {
            attributes.addAttribute("", "queryType", null, "xs:string",
                    getQueryType().getMainType());
        }
        if (getType() != null && !getType().isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Iterator<Reference> iterator = getType().iterator(); iterator
                    .hasNext();) {
                Reference reference = iterator.next();
                builder.append(reference.toString());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            attributes.addAttribute("", "type", null, "xs:string", builder
                    .toString());
        }

        if (getChildResources().isEmpty() && getDocumentations().isEmpty()
                && getMethods().isEmpty() && getParameters().isEmpty()) {
            writer.emptyElement("", "resource", null, attributes);
        } else {
            writer.startElement("", "resource", null, attributes);

            for (ResourceInfo resourceInfo : getChildResources()) {
                resourceInfo.writeElement(writer);
            }

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (MethodInfo methodInfo : getMethods()) {
                methodInfo.writeElement(writer);
            }

            for (ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            writer.endElement("", "resource");
        }
    }

}
