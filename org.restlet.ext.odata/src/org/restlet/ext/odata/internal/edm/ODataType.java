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
import java.util.Set;
import java.util.TreeSet;

/**
 * Super class of complex and entity types.
 * 
 * @author Thierry Boileau
 */
public class ODataType extends NamedObject implements Comparable<ODataType> {

    /** Is this type abstract? */
    private boolean abstractType;

    /** The parent type this type inherits from. */
    private ODataType baseType;

    /** The list of complex properties. */
    private List<ComplexProperty> complexProperties;

    /** The list of properties. */
    private List<Property> properties;

    /** The schema. */
    private Schema schema;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of this entity type.
     */
    public ODataType(String name) {
        super(name);
    }

    /**
     * Compares this object with the specified object for order. The comparison
     * is based on the computed full class name
     */
    public int compareTo(ODataType o) {
        if (o == null) {
            return 1;
        }
        int result = 0;

        String s1 = getFullClassName();
        String s2 = o.getFullClassName();
        if (s1 != null) {
            result = s1.compareTo(s2);
        } else if (s2 != null) {
            result = -1 * s2.compareTo(s1);
        }
        return result;
    }

    /**
     * Returns the parent type this type inherits from.
     * 
     * @return The parent type this type inherits from.
     */
    public ODataType getBaseType() {
        return baseType;
    }

    /**
     * Returns the Java class name related to this entity type.
     * 
     * @return The Java class name related to this entity type.
     */
    public String getClassName() {
        return getNormalizedName().substring(0, 1).toUpperCase()
                + getNormalizedName().substring(1);
    }

    /**
     * Returns the list of complex properties.
     * 
     * @return The list of complex properties.
     */
    public List<ComplexProperty> getComplexProperties() {
        if (complexProperties == null) {
            complexProperties = new ArrayList<ComplexProperty>();
        }
        return complexProperties;
    }

    /**
     * Returns the package name related to this entity type.
     * 
     * @return The package name related to this entity type.
     */
    public String getFullClassName() {
        return getPackageName() + "." + getClassName();
    }

    /**
     * Returns the set of imported Java classes.
     * 
     * @return The set of imported Java classes.
     */
    public Set<String> getImportedJavaClasses() {
        Set<String> result = new TreeSet<String>();

        for (Property property : getProperties()) {
            if (property.getType() != null) {
                result.addAll(property.getType().getImportedJavaClasses());
            }
        }

        for (ComplexProperty property : getComplexProperties()) {
            if (property.getComplexType() != null
                    && property.getComplexType().getSchema() != null) {
                if (!property.getComplexType().getSchema().equals(getSchema())) {
                    result.add(property.getComplexType().getFullClassName());
                }
            }
        }

        return result;
    }

    /**
     * Returns the set of imported entity types. By default, returns an empty
     * set.
     * 
     * @return The set of imported entity types.
     */
    public Set<ODataType> getImportedTypes() {
        return new TreeSet<ODataType>();
    }

    /**
     * Returns the package name related to this entity type.
     * 
     * @return The package name related to this entity type.
     */
    public String getPackageName() {
        return TypeUtils.getPackageName(getSchema());
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
     * Returns true if this type is abstract.
     * 
     * @return True if this type is abstract
     */
    public boolean isAbstractType() {
        return abstractType;
    }

    /**
     * Indicates if this type is abstract
     * 
     * @param abstractType
     *            True if this type is abstract
     */
    public void setAbstractType(boolean abstractType) {
        this.abstractType = abstractType;
    }

    /**
     * Sets the parent type this type inherits from.
     * 
     * @param baseType
     *            The parent type this type inherits from.
     */
    public void setBaseType(ODataType baseType) {
        this.baseType = baseType;
    }

    /**
     * Sets the list of complex properties.
     * 
     * @param complexProperties
     *            The list of complex properties.
     */
    public void setComplexProperties(List<ComplexProperty> complexProperties) {
        this.complexProperties = complexProperties;
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
