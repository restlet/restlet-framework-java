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
 * Root of a WADL description document.
 * 
 * @author Jerome Louvel
 */
public class ApplicationInfo extends DocumentedInfo {

    /** List of faults (representations that denote an error condition). */
    private List<FaultInfo> faults;

    /** Container for definitions of the format of data exchanged. */
    private GrammarsInfo grammars;

    /** List of methods. */
    private List<MethodInfo> methods;

    /** List of representations. */
    private List<RepresentationInfo> representations;

    /** Resources provided by the application. */
    private ResourcesInfo resources;

    /**
     * Describes a set of methods that define the behavior of a type of
     * resource.
     */
    private List<ResourceTypeInfo> resourceTypes;

    public ApplicationInfo() {
        super();
    }

    public ApplicationInfo(DocumentationInfo documentation) {
        super(documentation);
    }

    public ApplicationInfo(List<DocumentationInfo> documentations) {
        super(documentations);
    }

    public ApplicationInfo(String documentation) {
        super(documentation);
    }

    /**
     * Returns the list of fault elements.
     * 
     * @return The list of fault elements.
     */
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
     * sets the list of representation elements.
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
     * sets the list of resource type elements.
     * 
     * @param resourceTypes
     *            The list of resource type elements.
     */
    public void setResourceTypes(List<ResourceTypeInfo> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *            The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
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
