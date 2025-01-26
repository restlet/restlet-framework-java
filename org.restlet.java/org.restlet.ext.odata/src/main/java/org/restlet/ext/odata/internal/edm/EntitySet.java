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
 * Represents a set of entities, inside a EntityContainer. The type of these
 * entities is defined inside the current schema.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb387139.aspx">EntitySet
 *      Element (EntityContainer CSDL)</a>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class EntitySet extends NamedObject {

    /** The type of contained entities. */
    private EntityType type;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the entity.
     */
    public EntitySet(String name) {
        super(name);
    }

    /**
     * Returns the type of contained entities.
     * 
     * @return The type of contained entities.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Sets the type of contained entities.
     * 
     * @param type
     *            The type of contained entities.
     */
    public void setType(EntityType type) {
        this.type = type;
    }
}
