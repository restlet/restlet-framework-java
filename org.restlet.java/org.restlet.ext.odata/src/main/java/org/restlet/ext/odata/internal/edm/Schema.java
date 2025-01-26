/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class Schema {

    /** The list of defined associations. */
    private List<Association> associations;

    /** The list of defined complex types. */
    private List<ComplexType> complexTypes;

    /** The list of defined entity types. */
    private List<EntityType> entityTypes;

    /** The namespace of this schema. */
    private Namespace namespace;

    /** The list of referenced namespaces. */
    private List<Namespace> referencedNamespaces;

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
     * Returns the list of defined complex types.
     * 
     * @return The list of defined complex types.
     */
    public List<ComplexType> getComplexTypes() {
        if (complexTypes == null) {
            complexTypes = new ArrayList<ComplexType>();
        }
        return complexTypes;
    }

    /**
     * Returns the list of defined entity types.
     * 
     * @return The list of defined entity types.
     */
    public List<EntityType> getEntityTypes() {
        if (entityTypes == null) {
            entityTypes = new ArrayList<EntityType>();
        }
        return entityTypes;
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
     * Sets the list of defined associations.
     * 
     * @param associations
     *            The list of defined associations.
     */
    public void setAssociations(List<Association> associations) {
        this.associations = associations;
    }

    /**
     * Sets the list of defined complex types.
     * 
     * @param complexTypes
     *            The list of defined complex types.
     */
    public void setComplexTypes(List<ComplexType> complexTypes) {
        this.complexTypes = complexTypes;
    }

    /**
     * Sets the list of defined entity types.
     * 
     * @param types
     *            The list of defined entity types.
     */
    public void setEntityTypes(List<EntityType> types) {
        this.entityTypes = types;
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

}
