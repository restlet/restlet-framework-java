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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a kind of entity type, without "key", i.e. identifier.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb738466.aspx">Complex
 *      Type (EDM)</a>
 */
public class ComplexType extends ODataType {

    /** The list of complex types this type inherits from. */
    private List<ComplexType> complexTypes;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of this type.
     */
    public ComplexType(String name) {
        super(name);
    }

    /**
     * Returns the parent type this type inherits from.
     * 
     * @return The parent type this type inherits from.
     */
    @Override
    public ComplexType getBaseType() {
        return (ComplexType) super.getBaseType();
    }

    /**
     * Returns the list of complex types this type inherits from.
     * 
     * @return The list of complex types this type inherits from.
     */
    public List<ComplexType> getComplexTypes() {
        if (complexTypes == null) {
            complexTypes = new ArrayList<ComplexType>();
        }
        return complexTypes;
    }

    /**
     * Sets the list of complex types this type inherits from.
     * 
     * @param complexTypes
     *            The list of complex types this type inherits from.
     */
    public void setComplexTypes(List<ComplexType> complexTypes) {
        this.complexTypes = complexTypes;
    }

}
