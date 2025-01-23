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
 * Represents a complex property of an EntityType.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399546.aspx">Property
 *      Element (EntityType CSDL)</a>
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class ComplexProperty extends Property {

    /** The type of the property. */
    private ComplexType type;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of this property.
     */
    public ComplexProperty(String name) {
        super(name);
    }

    /**
     * Returns the type of the property.
     * 
     * @return The type of the property.
     */
    public ComplexType getComplexType() {
        return type;
    }

    /**
     * Sets the type of the property.
     * 
     * @param type
     *            The type of the property.
     */
    public void setComplexType(ComplexType type) {
        this.type = type;
    }

}
