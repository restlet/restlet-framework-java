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

    private List<DocumentationInfo> documentations;

    private String identifier;

    private Method name;

    private RequestInfo request;

    private ResponseInfo response;

    private Reference targetRef;

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

    public String getIdentifier() {
        return identifier;
    }

    public Method getName() {
        return name;
    }

    public RequestInfo getRequest() {
        return request;
    }

    public ResponseInfo getResponse() {
        return response;
    }

    public Reference getTargetRef() {
        return targetRef;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setName(Method name) {
        this.name = name;
    }

    public void setRequest(RequestInfo request) {
        this.request = request;
    }

    public void setResponse(ResponseInfo response) {
        this.response = response;
    }

    public void setTargetRef(Reference targetRef) {
        this.targetRef = targetRef;
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

        if (getName() != null && getName().toString() != null) {
            attributes.addAttribute("", "name", null, "xs:NMTOKEN", getName()
                    .toString());
        }
        if (getTargetRef() != null && getTargetRef().toString() != null) {
            attributes.addAttribute("", "href", null, "xs:anyURI",
                    getTargetRef().toString());
        }

        writer.startElement("", "method", null, attributes);

        if (getDocumentations() != null) {
            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }
        }
        if (getRequest() != null) {
            getRequest().writeElement(writer);
        }

        if (getResponse() != null) {
            getResponse().writeElement(writer);
        }

        writer.endElement("", "method");
    }

}
