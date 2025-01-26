/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
 * @deprecated Will be removed in next major release.
 */
@Deprecated
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
