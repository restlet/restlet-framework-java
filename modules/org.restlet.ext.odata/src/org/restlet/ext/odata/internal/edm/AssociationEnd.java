/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.ext.odata.internal.edm;

/**
 * Represents one entity implied in an association.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb399734.aspx">Association
 *      Element (SSDL)</a>
 */
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
