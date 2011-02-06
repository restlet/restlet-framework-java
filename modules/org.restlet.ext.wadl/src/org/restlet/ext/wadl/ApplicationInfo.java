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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.ext.xml.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Root of a WADL description document.
 * 
 * @author Jerome Louvel
 */
public class ApplicationInfo extends DocumentedInfo {

    /** List of faults (representations that denote an error condition). */
    @SuppressWarnings("deprecation")
    private List<FaultInfo> faults;

    /** Container for definitions of the format of data exchanged. */
    private GrammarsInfo grammars;

    /** List of methods. */
    private List<MethodInfo> methods;

    /**
     * Map of namespaces used in the WADL document. The key is the URI of the
     * namespace and the value, the prefix.
     */
    private Map<String, String> namespaces;

    /** List of representations. */
    private List<RepresentationInfo> representations;

    /** Resources provided by the application. */
    private ResourcesInfo resources;

    /**
     * Describes a set of methods that define the behavior of a type of
     * resource.
     */
    private List<ResourceTypeInfo> resourceTypes;

    /**
     * Constructor.
     */
    public ApplicationInfo() {
        super();
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ApplicationInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    /**
     * Constructor with a list of documentation elements.
     * 
     * @param documentations
     *            The list of documentation elements.
     */
    public ApplicationInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    /**
     * Constructor with a single documentation element.
     * 
     * @param documentation
     *            A single documentation element.
     */
    public ApplicationInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the list of fault elements.
     * 
     * @return The list of fault elements.
     */
    @SuppressWarnings("deprecation")
    public List<FaultInfo> getFaults() {
        // Lazy initialization with double-check.
        List<FaultInfo> f = this.faults;
        if (f == null) {
            synchronized (this) {
                f = this.faults;
                if (f == null) {
                    this.faults = f = new ArrayList<FaultInfo>();
                }
            }
        }
        return f;
    }

    /**
     * Returns the grammar elements.
     * 
     * @return The grammar elements.
     */
    public GrammarsInfo getGrammars() {
        return this.grammars;
    }

    /**
     * Returns the list of method elements.
     * 
     * @return The list of method elements.
     */
    public List<MethodInfo> getMethods() {
        // Lazy initialization with double-check.
        List<MethodInfo> m = this.methods;
        if (m == null) {
            synchronized (this) {
                m = this.methods;
                if (m == null) {
                    this.methods = m = new ArrayList<MethodInfo>();
                }
            }
        }
        return m;
    }

    /**
     * Returns the map of namespaces used in the WADL document.
     * 
     * @return The ap of namespaces used in the WADL document.
     */
    public Map<String, String> getNamespaces() {
        // Lazy initialization with double-check.
        Map<String, String> n = this.namespaces;
        if (n == null) {
            synchronized (this) {
                n = this.namespaces;
                if (n == null) {
                    this.namespaces = n = new HashMap<String, String>();
                }
            }
        }
        return n;
    }

    /**
     * Returns the list of representation elements.
     * 
     * @return The list of representation elements.
     */
    public List<RepresentationInfo> getRepresentations() {
        // Lazy initialization with double-check.
        List<RepresentationInfo> r = this.representations;
        if (r == null) {
            synchronized (this) {
                r = this.representations;
                if (r == null) {
                    this.representations = r = new ArrayList<RepresentationInfo>();
                }
            }
        }
        return r;
    }

    /**
     * Returns the resources root element.
     * 
     * @return The resources root element.
     */
    public ResourcesInfo getResources() {
        // Lazy initialization with double-check.
        ResourcesInfo r = this.resources;
        if (r == null) {
            synchronized (this) {
                r = this.resources;
                if (r == null) {
                    this.resources = r = new ResourcesInfo();
                }
            }
        }
        return r;
    }

    /**
     * Returns the list of resource type elements.
     * 
     * @return The list of resource type elements.
     */
    public List<ResourceTypeInfo> getResourceTypes() {
        // Lazy initialization with double-check.
        List<ResourceTypeInfo> rt = this.resourceTypes;
        if (rt == null) {
            synchronized (this) {
                rt = this.resourceTypes;
                if (rt == null) {
                    this.resourceTypes = rt = new ArrayList<ResourceTypeInfo>();
                }
            }
        }
        return rt;
    }

    /**
     * Sets the list of fault elements.
     * 
     * @param faults
     *            The list of documentation elements.
     */
    @SuppressWarnings("deprecation")
    public void setFaults(List<FaultInfo> faults) {
        this.faults = faults;
    }

    /**
     * Sets the grammars element.
     * 
     * @param grammars
     *            The grammars element.
     */
    public void setGrammars(GrammarsInfo grammars) {
        this.grammars = grammars;
    }

    /**
     * Sets the list of documentation elements.
     * 
     * @param methods
     *            The list of method elements.
     */
    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
    }

    /**
     * Sets the map of namespaces used in the WADL document. The key is the URI
     * of the namespace and the value, the prefix.
     * 
     * @param namespaces
     *            The map of namespaces used in the WADL document.
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Sets the list of representation elements.
     * 
     * @param representations
     *            The list of representation elements.
     */
    public void setRepresentations(List<RepresentationInfo> representations) {
        this.representations = representations;
    }

    /**
     * Sets the list of resource elements.
     * 
     * @param resources
     *            The list of resource elements.
     */
    public void setResources(ResourcesInfo resources) {
        this.resources = resources;
    }

    /**
     * Sets the list of resource type elements.
     * 
     * @param resourceTypes
     *            The list of resource type elements.
     */
    public void setResourceTypes(List<ResourceTypeInfo> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void updateNamespaces(Map<String, String> namespaces) {
        namespaces.putAll(resolveNamespaces());

        if (getGrammars() != null) {
            getGrammars().updateNamespaces(namespaces);
        }

        for (final MethodInfo methodInfo : getMethods()) {
            methodInfo.updateNamespaces(namespaces);
        }

        for (final RepresentationInfo representationInfo : getRepresentations()) {
            representationInfo.updateNamespaces(namespaces);
        }

        if (getResources() != null) {
            getResources().updateNamespaces(namespaces);
        }

        for (final ResourceTypeInfo resourceTypeInfo : getResourceTypes()) {
            resourceTypeInfo.updateNamespaces(namespaces);
        }

        for (final FaultInfo faultInfo : getFaults()) {
            faultInfo.updateNamespaces(namespaces);
        }
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    @SuppressWarnings("deprecation")
    public void writeElement(XmlWriter writer) throws SAXException {
        updateNamespaces(getNamespaces());

        for (String key : getNamespaces().keySet()) {
            writer.forceNSDecl(key, getNamespaces().get(key));
        }

        writer.startElement(APP_NAMESPACE, "application");

        for (final DocumentationInfo documentationInfo : getDocumentations()) {
            documentationInfo.writeElement(writer);
        }

        if (getGrammars() != null) {
            getGrammars().writeElement(writer);
        }

        for (final MethodInfo methodInfo : getMethods()) {
            methodInfo.writeElement(writer);
        }

        for (final RepresentationInfo representationInfo : getRepresentations()) {
            representationInfo.writeElement(writer);
        }

        if (getResources() != null) {
            getResources().writeElement(writer);
        }

        for (final ResourceTypeInfo resourceTypeInfo : getResourceTypes()) {
            resourceTypeInfo.writeElement(writer);
        }

        for (final FaultInfo faultInfo : getFaults()) {
            faultInfo.writeElement(writer);
        }

        writer.endElement(APP_NAMESPACE, "application");
    }

}
