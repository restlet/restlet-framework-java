/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.wadl;

import static org.restlet.ext.wadl.WadlRepresentation.APP_NAMESPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Describes the grammars used by representation descriptions. This is
 * especially useful to formally describe XML representations using XML Schema
 * or Relax NG standards.
 * 
 * @author Jerome Louvel
 */
public class GrammarsInfo extends DocumentedInfo {

    /** Definitions of data format descriptions to be included by reference. */
    private List<IncludeInfo> includes;

    /**
     * Constructor.
     */
    public GrammarsInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public GrammarsInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public GrammarsInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public GrammarsInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the list of include elements.
     * 
     * @return The list of include elements.
     */
    public List<IncludeInfo> getIncludes() {
        // Lazy initialization with double-check.
        List<IncludeInfo> i = this.includes;
        if (i == null) {
            synchronized (this) {
                i = this.includes;
                if (i == null) {
                    this.includes = i = new ArrayList<IncludeInfo>();
                }
            }
        }
        return i;
    }

    /**
     * Sets the list of include elements.
     * 
     * @param includes
     *            The list of include elements.
     */
    public void setIncludes(List<IncludeInfo> includes) {
        this.includes = includes;
    }

    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        for (final IncludeInfo includeInfo : getIncludes()) {
            includeInfo.updateNamespaces(namespaces);
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

        if (getDocumentations().isEmpty() && getIncludes().isEmpty()) {
            writer.emptyElement(APP_NAMESPACE, "grammars");
        } else {
            writer.startElement(APP_NAMESPACE, "grammars");

            for (final DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (final IncludeInfo includeInfo : getIncludes()) {
                includeInfo.writeElement(writer);
            }

            writer.endElement(APP_NAMESPACE, "grammars");
        }
    }

}
