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

/**
 * Describes the grammars used by representation descriptions. This is
 * especially useful to formally describe XML representations using XML Schema
 * or Relax NG standards.
 * 
 * @author Jerome Louvel
 */
public class GrammarsInfo {

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /** Definitions of data format descriptions to be included by reference. */
    private List<IncludeInfo> includes;

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

    public List<IncludeInfo> getIncludes() {
        // Lazy initialization with double-check.
        List<IncludeInfo> i = this.includes;
        if (i == null) {
            synchronized (this) {
                i = this.includes;
                if (i == null)
                    this.includes = i = new ArrayList<IncludeInfo>();
            }
        }
        return i;
    }

    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    public void setIncludes(List<IncludeInfo> includes) {
        this.includes = includes;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {

        if (getDocumentations().isEmpty() && getIncludes().isEmpty()) {
            writer.emptyElement("", "grammars");
        } else {
            writer.startElement("", "grammars");

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (IncludeInfo includeInfo : getIncludes()) {
                includeInfo.writeElement(writer);
            }

            writer.endElement("", "grammars");
        }
    }
}
