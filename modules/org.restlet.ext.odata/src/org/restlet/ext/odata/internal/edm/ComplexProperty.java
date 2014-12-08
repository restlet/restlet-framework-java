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
 * Represents a complex property of an EntityType.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399546.aspx">Property
 *      Element (EntityType CSDL)</a>
 */
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
