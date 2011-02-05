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

package org.restlet.ext.odata.internal.edm;

/**
 * Represents an association between two properties.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb399562.aspx">Navigation
 *      Properties (EDM)</a>
 */
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
