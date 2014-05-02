/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.info;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of APISpark elements that supports documentation.
 * 
 * @author Thierry Boileau.
 */
public abstract class DocumentedInfo {

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /**
     * Constructor.
     */
    public DocumentedInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public DocumentedInfo(DocumentationInfo documentation) {
        getDocumentations().add(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public DocumentedInfo(List<DocumentationInfo> documentations) {
        this.documentations = documentations;
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public DocumentedInfo(String documentation) {
        this(new DocumentationInfo(documentation));
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
                if (d == null) {
                    this.documentations = d = new ArrayList<DocumentationInfo>();
                }
            }
        }
        return d;
    }

    /**
     * Set the list of documentation elements with a single element.
     * 
     * @param documentationInfo
     *            A single documentation element.
     */
    public void setDocumentation(DocumentationInfo documentationInfo) {
        getDocumentations().clear();
        getDocumentations().add(documentationInfo);
    }

    /**
     * Set the list of documentation elements with a single element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public void setDocumentation(String documentation) {
        getDocumentations().clear();
        getDocumentations().add(new DocumentationInfo(documentation));
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
}
