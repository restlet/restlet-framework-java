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
 * Represents a set of entities, inside a EntityContainer. The type of these
 * entities is defined inside the current schema.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb387139.aspx">EntitySet
 *      Element (EntityContainer CSDL)</a>
 */
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
