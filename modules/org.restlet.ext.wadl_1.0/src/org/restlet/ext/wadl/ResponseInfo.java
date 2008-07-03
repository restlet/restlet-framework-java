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

import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Describes the properties of a response associated to a parent method.
 * 
 * @author Jerome Louvel
 */
public class ResponseInfo {

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /** List of faults (representations that denote an error condition). */
    private List<FaultInfo> faults;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** List of representations. */
    private List<RepresentationInfo> representations;

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
     * Returns the list of faults (representations that denote an error
     * condition).
     * 
     * @return The list of faults (representations that denote an error
     *         condition).
     */
    public List<FaultInfo> getFaults() {
        // Lazy initialization with double-check.
        List<FaultInfo> f = this.faults;
        if (f == null) {
            synchronized (this) {
                f = this.faults;
                if (f == null)
                    this.faults = f = new ArrayList<FaultInfo>();
            }
        }
        return f;
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
     * Returns the list of representations
     * 
     * @return The list of representations
     */
    public List<RepresentationInfo> getRepresentations() {
        // Lazy initialization with double-check.
        List<RepresentationInfo> r = this.representations;
        if (r == null) {
            synchronized (this) {
                r = this.representations;
                if (r == null)
                    this.representations = r = new ArrayList<RepresentationInfo>();
            }
        }
        return r;
    }

    /**
     * Sets the list of documentation elements.
     * 
     * @param doc
     *            The list of documentation elements.
     */
    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    /**
     * Sets the list of faults (representations that denote an error condition).
     * 
     * @param faults
     *            The list of faults (representations that denote an error
     *            condition).
     */
    public void setFaults(List<FaultInfo> faults) {
        this.faults = faults;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *            The list of parameters.
     */
    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the list of representations
     * 
     * @param representations
     *            The list of representations
     */
    public void setRepresentations(List<RepresentationInfo> representations) {
        this.representations = representations;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {

        if (getDocumentations().isEmpty() && getFaults().isEmpty()
                && getParameters().isEmpty() && getRepresentations().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "response");
        } else {
            writer.startElement(APP_NAMESPACE, "response");

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            for (RepresentationInfo representationInfo : getRepresentations()) {
                representationInfo.writeElement(writer);
            }

            for (FaultInfo faultInfo : getFaults()) {
                faultInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "response");
        }
    }
}
