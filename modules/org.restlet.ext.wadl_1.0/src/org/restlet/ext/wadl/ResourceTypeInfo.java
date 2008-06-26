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

import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes a reusable type of resources.
 * 
 * @author Jerome Louvel
 */
public class ResourceTypeInfo {

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /** Identifier for that element. */
    private String identifier;

    /** List of supported methods. */
    private List<MethodInfo> methods;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

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

        if (getDocumentations().isEmpty() && getMethods().isEmpty()
                && getParameters().isEmpty()) {
            writer.emptyElement("", "resource_type", null, attributes);
        } else {
            writer.startElement("", "resource_type", null, attributes);
            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (MethodInfo methodInfo : getMethods()) {
                methodInfo.writeElement(writer);
            }

            for (ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            writer.endElement("", "resource_type");
        }
    }

}
