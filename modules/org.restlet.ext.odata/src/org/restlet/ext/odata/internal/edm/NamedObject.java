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

import org.restlet.ext.odata.internal.reflect.ReflectUtils;

/**
 * Base class for all EDM concepts that have a name.
 * 
 * @author Thierry Boileau
 */
public class NamedObject {

    /** The name of the EDM concept. */
    private final String name;

    /** The name's value as a valid Java identifier. */
    private final String normalizedName;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the entity.
     */
    public NamedObject(String name) {
        super();
        this.name = name;
        this.normalizedName = ReflectUtils.normalize(name);
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name following the the java naming rules.
     * 
     * @see <a
     *      href="http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#40625">
     *      Identifiers</a>
     * @return The name following the the java naming rules.
     */
    public String getNormalizedName() {
        return normalizedName;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof NamedObject) {
            NamedObject object = (NamedObject) obj;
            result = object.getName().equals(this.name);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass() + " " + this.name;
    }

}
