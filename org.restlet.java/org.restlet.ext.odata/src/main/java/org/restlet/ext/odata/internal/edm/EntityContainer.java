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
 * Represents a set of entities and associations defined on a schema.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399557.aspx">Entity
 *      Containers (EDM)</a>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class EntityContainer extends NamedObject {
    /** The list of associations. */
    private List<AssociationSet> associations;

    /** Is this container the default one for a WCF Data Service? */
    private boolean defaultEntityContainer;

    /** The list of entities. */
    private List<EntitySet> entities;

    /** The container this container inherits from. */
    private EntityContainer extended;

    /** The list of function imports. */
    private List<FunctionImport> functionImports;

    /** The schema. */
    private Schema schema;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of this entity container.
     */
    public EntityContainer(String name) {
        super(name);
    }

    /**
     * Returns the list of associations.
     * 
     * @return The list of associations.
     */
    public List<AssociationSet> getAssociations() {
        if (associations == null) {
            associations = new ArrayList<AssociationSet>();
        }
        return associations;
    }

    /**
     * Returns the list of entities.
     * 
     * @return The list of entities.
     */
    public List<EntitySet> getEntities() {
        if (entities == null) {
            entities = new ArrayList<EntitySet>();
        }
        return entities;
    }

    /**
     * Returns the container this container inherits from.
     * 
     * @return The container this container inherits from.
     */
    public EntityContainer getExtended() {
        return extended;
    }

    /**
     * Returns the list of function imports.
     * 
     * @return The list of function imports.
     */
    public List<FunctionImport> getFunctionImports() {
        if (functionImports == null) {
            functionImports = new ArrayList<FunctionImport>();
        }
        return functionImports;
    }

    /**
     * Returns the schema.
     * 
     * @return The schema.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Return true if this container is the default one for a WCF data service.
     * 
     * @return True if this container is the default one for a WCF data service.
     */
    public boolean isDefaultEntityContainer() {
        return defaultEntityContainer;
    }

    /**
     * Sets the list of associations.
     * 
     * @param associations
     *            The list of associations.
     */
    public void setAssociations(List<AssociationSet> associations) {
        this.associations = associations;
    }

    /**
     * Indicates if this container is the default one for a WCF data service.
     * 
     * @param defaultEntityContainer
     *            True if this container is the default one for a WCF data
     *            service.
     */
    public void setDefaultEntityContainer(boolean defaultEntityContainer) {
        this.defaultEntityContainer = defaultEntityContainer;
    }

    /**
     * Sets the list of entities.
     * 
     * @param entities
     *            The list of entities.
     */
    public void setEntities(List<EntitySet> entities) {
        this.entities = entities;
    }

    /**
     * Sets the container this container inherits from.
     * 
     * @param extended
     *            The container this container inherits from.
     */
    public void setExtended(EntityContainer extended) {
        this.extended = extended;
    }

    /**
     * Sets the list of function imports.
     * 
     * @param functionImports
     *            The list of function imports.
     */
    public void setFunctionImports(List<FunctionImport> functionImports) {
        this.functionImports = functionImports;
    }

    /**
     * Sets the schema.
     * 
     * @param schema
     *            The schema.
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
