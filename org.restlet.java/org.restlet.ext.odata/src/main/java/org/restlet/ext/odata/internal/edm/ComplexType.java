/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
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
 * @deprecated Will be removed in next major release.
 */
@Deprecated
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
