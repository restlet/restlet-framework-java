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

import java.util.Set;
import java.util.TreeSet;

/**
 * Represents an EDM simple type.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399213.aspx">Simple
 *      Types (EDM)</a>
 */
public class Type extends NamedObject {

    /**
     * Constructor.
     * 
     * @param typeName
     *            The name of the type.
     */
    public Type(String typeName) {
        super(typeName);
    }

    /**
     * Returns the name of the corresponding Java class or scalar type.
     * 
     * @return The name of the corresponding Java class or scalar type.
     */
    public String getClassName() {
        return TypeUtils.toJavaTypeName(getName());
    }

    /**
     * Returns the set of imported Java classes.
     * 
     * @return The set of imported Java classes.
     */
    public Set<String> getImportedJavaClasses() {
        Set<String> result = new TreeSet<String>();

        if (getName().endsWith("DateTime")) {
            result.add(getJavaClass().getName());
        } else if (getName().endsWith("DateTimeOffset")) {
            result.add(getJavaClass().getName());
        }

        return result;
    }

    /**
     * Returns the corresponding Java class.
     * 
     * @return The corresponding Java class.
     */
    public Class<?> getJavaClass() {
        return TypeUtils.toJavaClass(getName());
    }

    /**
     * Returns the class name of the corresponding Java class. Returns null for
     * a scalar type.
     * 
     * @return The class name of the corresponding Java class.
     */
    public String getPackageName() {
        String result = null;
        if (getName().endsWith("DateTime")) {
            result = "java.util";
        } else if (getName().endsWith("DateTimeOffset")) {
            result = "java.util";
        }

        return result;
    }

}
