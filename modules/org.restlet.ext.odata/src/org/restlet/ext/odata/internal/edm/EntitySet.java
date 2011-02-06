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
