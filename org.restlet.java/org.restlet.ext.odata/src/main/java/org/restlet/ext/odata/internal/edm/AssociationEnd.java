/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.odata.internal.edm;

/**
 * Represents one entity implied in an association.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb399734.aspx">Association
 *      Element (SSDL)</a>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class AssociationEnd {

    /** The cardinality of the relation. */
    private String multiplicity;

    /** The role of this entity relatively to this association. */
    private final String role;

    /** The type of this entity. */
    private EntityType type;

    /**
     * Constructor.
     * 
     * @param role
     *            The name of the role.
     */
    public AssociationEnd(String role) {
        super();
        this.role = role;
    }

    /**
     * Returns the cardinality of the relation.
     * 
     * @return The cardinality of the relation.
     */
    public String getMultiplicity() {
        return multiplicity;
    }

    /**
     * Returns the role of this entity relatively to this association.
     * 
     * @return The role of this entity relatively to this association.
     */
    public String getRole() {
        return role;
    }

    /**
     * Returns the type of this entity.
     * 
     * @return The type of this entity.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Returns true if the cardinality says that this is a one to many or many
     * to many relation.
     * 
     * @return
     */
    public boolean isToMany() {
        return "*".equals(getMultiplicity());
    }

    /**
     * Sets the cardinality of the relation.
     * 
     * @param multiplicity
     *            The cardinality of the relation.
     */
    public void setMultiplicity(String multiplicity) {
        this.multiplicity = multiplicity;
    }

    /**
     * Sets the type of this entity.
     * 
     * @param type
     *            The type of this entity.
     */
    public void setType(EntityType type) {
        this.type = type;
    }

}
