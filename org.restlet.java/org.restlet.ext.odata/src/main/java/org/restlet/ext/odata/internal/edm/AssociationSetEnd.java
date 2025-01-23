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
 * Represents one entity implied in an AssociationSet.
 * 
 * @author Thierry Boileau
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class AssociationSetEnd {

    /** The role of this entity relatively to this association. */
    private final String role;

    /** The type of this entity. */
    private EntitySet type;

    /**
     * Constructor.
     * 
     * @param role
     *            The name of the role.
     */
    public AssociationSetEnd(String role) {
        super();
        this.role = role;
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
    public EntitySet getType() {
        return type;
    }

    /**
     * Sets the type of this entity.
     * 
     * @param type
     *            The type of this entity.
     */
    public void setType(EntitySet type) {
        this.type = type;
    }
}
