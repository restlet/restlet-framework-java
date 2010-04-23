/**
 * Copyright 2005-2010 Noelios Technologies.
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

import java.util.Date;
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
        String result = "Object";
        if (getName().endsWith("Binary")) {
            result = "byte[]";
        } else if (getName().endsWith("Boolean")) {
            result = "boolean";
        } else if (getName().endsWith("DateTime")) {
            result = "Date";
        } else if (getName().endsWith("DateTimeOffset")) {
            result = "Date";
        } else if (getName().endsWith("Time")) {
            result = "long";
        } else if (getName().endsWith("Decimal")) {
            result = "double";
        } else if (getName().endsWith("Single")) {
            result = "float";
        } else if (getName().endsWith("Double")) {
            result = "double";
        } else if (getName().endsWith("Guid")) {
            result = "String";
        } else if (getName().endsWith("Int16")) {
            result = "short";
        } else if (getName().endsWith("Int32")) {
            result = "int";
        } else if (getName().endsWith("Int64")) {
            result = "long";
        } else if (getName().endsWith("Byte")) {
            result = "byte";
        } else if (getName().endsWith("String")) {
            result = "String";
        }

        return result;
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
        Class<?> result = Object.class;
        if (getName().endsWith("Binary")) {
            result = byte[].class;
        } else if (getName().endsWith("Boolean")) {
            result = Boolean.class;
        } else if (getName().endsWith("DateTime")) {
            result = Date.class;
        } else if (getName().endsWith("DateTimeOffset")) {
            result = Date.class;
        } else if (getName().endsWith("Time")) {
            result = Long.class;
        } else if (getName().endsWith("Decimal")) {
            result = Double.class;
        } else if (getName().endsWith("Single")) {
            result = Float.class;
        } else if (getName().endsWith("Double")) {
            result = Double.class;
        } else if (getName().endsWith("Guid")) {
            result = String.class;
        } else if (getName().endsWith("Int16")) {
            result = Short.class;
        } else if (getName().endsWith("Int32")) {
            result = Integer.class;
        } else if (getName().endsWith("Int64")) {
            result = Long.class;
        } else if (getName().endsWith("Byte")) {
            result = Byte.class;
        } else if (getName().endsWith("String")) {
            result = String.class;
        }

        return result;
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
