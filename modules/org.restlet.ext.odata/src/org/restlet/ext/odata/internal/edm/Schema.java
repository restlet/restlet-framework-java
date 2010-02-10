/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.odata.internal.edm;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a set of entity types and associations.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399276.aspx">Schema
 *      Element (CSDL)</a>
 */
public class Schema {

    /** The list of defined associations. */
    private List<Association> associations;

    /** The namespace of this schema. */
    private Namespace namespace;

    /** The list of referenced namespaces. */
    private List<Namespace> referencedNamespaces;

    /** The list of defined types. */
    private List<EntityType> types;

    /**
     * Returns the list of defined associations.
     * 
     * @return The list of defined associations.
     */
    public List<Association> getAssociations() {
        if (associations == null) {
            associations = new ArrayList<Association>();
        }
        return associations;
    }

    /**
     * Returns the namespace of this schema.
     * 
     * @return The namespace of this schema.
     */
    public Namespace getNamespace() {
        return namespace;
    }

    /**
     * Returns the list of referenced namespaces.
     * 
     * @return The list of referenced namespaces.
     */
    public List<Namespace> getReferencedNamespaces() {
        if (referencedNamespaces == null) {
            referencedNamespaces = new ArrayList<Namespace>();
        }
        return referencedNamespaces;
    }

    /**
     * Returns the list of defined types.
     * 
     * @return The list of defined types.
     */
    public List<EntityType> getTypes() {
        if (types == null) {
            types = new ArrayList<EntityType>();
        }
        return types;
    }

    /**
     * Sets the list of defined associations.
     * 
     * @param associations
     *            The list of defined associations.
     */
    public void setAssociations(List<Association> associations) {
        this.associations = associations;
    }

    /**
     * Sets the namespace of this schema.
     * 
     * @param namespace
     *            The namespace of this schema.
     */
    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    /**
     * Sets the list of referenced namespaces.
     * 
     * @param referencedNamespaces
     *            The list of referenced namespaces.
     */
    public void setReferencedNamespaces(List<Namespace> referencedNamespaces) {
        this.referencedNamespaces = referencedNamespaces;
    }

    /**
     * Sets the list of defined types.
     * 
     * @param types
     *            The list of defined types.
     */
    public void setTypes(List<EntityType> types) {
        this.types = types;
    }

}
