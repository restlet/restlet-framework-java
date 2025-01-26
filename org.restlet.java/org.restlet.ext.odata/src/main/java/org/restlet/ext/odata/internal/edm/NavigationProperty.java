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
 * Represents an association between two properties.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb399562.aspx">Navigation
 *      Properties (EDM)</a>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class NavigationProperty extends NamedObject {
    /** The start entity of the association. */
    private AssociationEnd fromRole;

    /** The association. */
    private Association relationship;

    /** The target entity of the association. */
    private AssociationEnd toRole;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of this navigation property.
     */
    public NavigationProperty(String name) {
        super(name);
    }

    /**
     * Returns the start entity of the association.
     * 
     * @return The start entity of the association.
     */
    public AssociationEnd getFromRole() {
        return fromRole;
    }

    /**
     * Returns the association.
     * 
     * @return The association.
     */
    public Association getRelationship() {
        return relationship;
    }

    /**
     * Returns the target entity of the association.
     * 
     * @return The target entity of the association.
     */
    public AssociationEnd getToRole() {
        return toRole;
    }

    /**
     * Sets the start entity of the association.
     * 
     * @param fromRole
     *            The start entity of the association.
     */
    public void setFromRole(AssociationEnd fromRole) {
        this.fromRole = fromRole;
    }

    /**
     * Sets the association.
     * 
     * @param relationship
     *            The association.
     */
    public void setRelationship(Association relationship) {
        this.relationship = relationship;
    }

    /**
     * Sets the target entity of the association.
     * 
     * @param toRole
     *            The target entity of the association.
     */
    public void setToRole(AssociationEnd toRole) {
        this.toRole = toRole;
    }

}
