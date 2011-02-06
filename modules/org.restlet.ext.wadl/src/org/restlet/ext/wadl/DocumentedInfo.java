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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * Superclass of WADL elements that supports dcumentation.
 * 
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
        super();
        getDocumentations().add(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public DocumentedInfo(List<DocumentationInfo> documentations) {
        super();
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
     * Returns the list of namespaces used in the documentation elements as a
     * map. The key is the URI of the namespace and the value, the prefix.
     * 
     * @return The list of namespaces used in the documentation elements as a
     *         map.
     */
    public Map<String, String> resolveNamespaces() {
        Map<String, String> result = new HashMap<String, String>();
        for (DocumentationInfo documentationInfo : getDocumentations()) {
            if (documentationInfo.getMixedContent() != null) {
                resolveNamespaces(documentationInfo.getMixedContent(), result);
            }
        }
        return result;
    }

    /**
     * Completes the given map of namespaces with the namespaces of the given
     * node.
     * 
     * @param node
     *            The node to analyse.
     * @param namespaces
     *            the map of namespaces to complete.
     */
    private void resolveNamespaces(Node node, Map<String, String> namespaces) {
        if (node.getNamespaceURI() != null) {
            namespaces.put(node.getNamespaceURI(), node.getPrefix());
        }
        if (node.getChildNodes() != null) {
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                resolveNamespaces(node.getChildNodes().item(i), namespaces);
            }
        }
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

    /**
     * Completes the given map of namespaces with the namespaces used in the
     * documentation elements. The key is the URI of the namespace and the
     * value, the prefix.
     * 
     * @param namespaces
     *            The given map of namespaces to complete.
     */
    public abstract void updateNamespaces(Map<String, String> namespaces);
}
