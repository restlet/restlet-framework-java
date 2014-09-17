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
 * Root of a APISpark description document.
 * 
 * @author Jerome Louvel
 */
public class ApplicationInfo extends DocumentedInfo {

    /** List of methods. */
    private List<MethodInfo> methods;

    /** Name. */
    private String name;

    /** List of representations. */
    private List<RepresentationInfo> representations;

    /** Resources provided by the application. */
    private ResourcesInfo resources;

    /**
     * Describes a set of methods that define the behavior of a type of
     * resource.
     */
    private List<ResourceTypeInfo> resourceTypes;

    /** The version of the Application. */
    private String version;

    /**
     * Constructor.
     */
    public ApplicationInfo() {
        super();
    }

    public ApplicationInfo(String description, String name) {
        super(description, name);
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

    public String getName() {
        return name;
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
     * Returns the version of the Application.
     * 
     * @return The version of the Application.
     */
    public String getVersion() {
        return version;
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

    public void setName(String name) {
        this.name = name;
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

    /**
     * Sets the version of the Application.
     * 
     * @param version
     *            The version of the Application.
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
