/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.dataservices.internal.edm;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an entity type, without "key", i.e. identifier.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb738466.aspx">Complex
 *      Type (EDM)</a>
 */
public class ComplexType extends NamedObject {

    /** The list of complex types this type inherits from. */
    private List<ComplexType> complexTypes;

    /** The parent complex type. */
    private ComplexType parentComplexType;

    /** The list of properties. */
    private List<Property> properties;

    /** The schema. */
    private Schema schema;

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
     * Returns the parent complex type.
     * 
     * @return The parent complex type.
     */
    public ComplexType getParentComplexType() {
        return parentComplexType;
    }

    /**
     * Returns the list of properties.
     * 
     * @return The list of properties.
     */
    public List<Property> getProperties() {
        if (properties == null) {
            properties = new ArrayList<Property>();
        }
        return properties;
    }

    /**
     * Returns the schema.
     * 
     * @return The schema.
     */
    public Schema getSchema() {
        return schema;
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

    /**
     * Sets the parent complex type.
     * 
     * @param parentComplexType
     *            The parent complex type.
     */
    public void setParentComplexType(ComplexType parentComplexType) {
        this.parentComplexType = parentComplexType;
    }

    /**
     * Sets the list of properties.
     * 
     * @param properties
     *            The list of properties.
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Sets the schema.
     * 
     * @param schema
     *            The schema.
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

}
