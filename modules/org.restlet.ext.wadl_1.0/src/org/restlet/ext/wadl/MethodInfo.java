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

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Describes the expected requests and responses of a resource method.
 * 
 * @author Jerome Louvel
 */
public class MethodInfo {

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /** Identifier for the method. */
    private String identifier;

    /** Name of the method. */
    private Method name;

    /** Describes the input to the method. */
    private RequestInfo request;

    /** Describes the output of the method. */
    private ResponseInfo response;

    /** Reference to a method definition element. */
    private Reference targetRef;

    /**
     * Return the list of documentation elements.
     * 
     * @return The list of documentation elements.
     */
    public List<DocumentationInfo> getDocumentations() {
        // Lazy initialization with double-check.
        List<DocumentationInfo> d = this.documentations;
        if (d == null) {
            synchronized (this) {
                d = this.documentations;
                if (d == null) {
                    this.documentations = d = new ArrayList<DocumentationInfo>();
                }
            }
        }
        return d;
    }

    /**
     * Returns the identifier for the method.
     * 
     * @return The identifier for the method.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the name of the method.
     * 
     * @return The name of the method.
     */

    public Method getName() {
        return this.name;
    }

    /**
     * Returns the input to the method.
     * 
     * @return The input to the method.
     */
    public RequestInfo getRequest() {
        return this.request;
    }

    /**
     * Returns the output of the method.
     * 
     * @return The output of the method.
     */
    public ResponseInfo getResponse() {
        return this.response;
    }

    /**
     * Returns the reference to a method definition element.
     * 
     * @return The reference to a method definition element.
     */
    public Reference getTargetRef() {
        return this.targetRef;
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
     * Sets the identifier for the method.
     * 
     * @param identifier
     *            The identifier for the method.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Sets the name of the method.
     * 
     * @param name
     *            The name of the method.
     */
    public void setName(Method name) {
        this.name = name;
    }

    /**
     * Sets the input to the method.
     * 
     * @param request
     *            The input to the method.
     */
    public void setRequest(RequestInfo request) {
        this.request = request;
    }

    /**
     * Setst the output of the method.
     * 
     * @param response
     *            The output of the method.
     */
    public void setResponse(ResponseInfo response) {
        this.response = response;
    }

    /**
     * Sets the reference to a method definition element.
     * 
     * @param targetRef
     *            The reference to a method definition element.
     */
    public void setTargetRef(Reference targetRef) {
        this.targetRef = targetRef;
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
        if ((getIdentifier() != null) && !getIdentifier().equals("")) {
            attributes.addAttribute("", "id", null, "xs:ID", getIdentifier());
        }

        if ((getName() != null) && (getName().toString() != null)) {
            attributes.addAttribute("", "name", null, "xs:NMTOKEN", getName()
                    .toString());
        }
        if ((getTargetRef() != null) && (getTargetRef().toString() != null)) {
            attributes.addAttribute("", "href", null, "xs:anyURI",
                    getTargetRef().toString());
        }

        if (getDocumentations().isEmpty() && (getRequest() == null)
                && (getResponse() == null)) {
            writer.emptyElement(APP_NAMESPACE, "method", null, attributes);
        } else {
            writer.startElement(APP_NAMESPACE, "method", null, attributes);

            if (getDocumentations() != null) {
                for (final DocumentationInfo documentationInfo : getDocumentations()) {
                    documentationInfo.writeElement(writer);
                }
            }
            if (getRequest() != null) {
                getRequest().writeElement(writer);
            }

            if (getResponse() != null) {
                getResponse().writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "method");
        }
    }

}
