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

/**
 * Defines a class or type of entity inside a schema. Note that composite keys
 * are not supported.
 * 
 * @author Thierry Boileau
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/bb399206.aspx">EntityType
 *      Element (CSDL)</a>
 */
public class EntityType extends ODataType {

    /** The list of associations. */
    private List<NavigationProperty> associations;

    /** Is this type a blob? */
    private boolean blob;

    /**
     * The entity's member that stores the resource reference able to update the
     * blob value.
     */
    private Property blobValueEditRefProperty;

    /** The property of the entity that stores the blob reference. */
    private Property blobValueRefProperty;

    /** The list of properties that identifies an instance of this type. */
    private List<Property> keys;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of this entity type.
     */
    public EntityType(String name) {
        super(name);
    }

    /**
     * Returns the list of associations.
     * 
     * @return The list of associations.
     */
    public List<NavigationProperty> getAssociations() {
        if (associations == null) {
            associations = new ArrayList<NavigationProperty>();
        }
        return associations;
    }

    /**
     * Returns the parent type this type inherits from.
     * 
     * @return The parent type this type inherits from.
     */
    @Override
    public EntityType getBaseType() {
        return (EntityType) super.getBaseType();
    }

    /**
     * Returns the entity's member that stores the resource reference able to
     * update the blob value.
     * 
     * @return The entity's member that stores the resource reference able to
     *         update the blob value.
     */
    public Property getBlobValueEditRefProperty() {
        return blobValueEditRefProperty;
    }

    /**
     * Returns the name of the entity property that stores the blob reference.
     * 
     * @return The name of the entity property that stores the blob reference.
     */
    public Property getBlobValueRefProperty() {
        return blobValueRefProperty;
    }

    /**
     * Returns the set of imported Java classes.
     * 
     * @return The set of imported Java classes.
     */
    public Set<String> getImportedJavaClasses() {
        Set<String> result = super.getImportedJavaClasses();

        for (NavigationProperty property : getAssociations()) {
            if (property.getToRole().isToMany()) {
                result.add("java.util.List");
                break;
            }
        }

        return result;
    }

    /**
     * Returns the set of imported entity types.
     * 
     * @return The set of imported entity types.
     */
    @Override
    public Set<ODataType> getImportedTypes() {
        Set<ODataType> result = super.getImportedTypes();

        for (NavigationProperty property : getAssociations()) {
            result.add(property.getToRole().getType());
        }
        return result;
    }

    /**
     * Returns the "keys" property.
     * 
     * @return The "keys" property.
     */
    public List<Property> getKeys() {
        return keys;
    }

    /**
     * Returns true if this type a blob, that is to say it represents binary
     * data.
     * 
     * @return True if this type a blob, that is to say it represents binary
     *         data.
     */
    public boolean isBlob() {
        return blob;
    }

    /**
     * Sets the list of associations.
     * 
     * @param associations
     *            The list of associations.
     */
    public void setAssociations(List<NavigationProperty> associations) {
        this.associations = associations;
    }

    /**
     * Indicates if this type a blob, that is to say it represents binary data.
     * 
     * @param media
     *            True if this type a blob, that is to say it represents binary
     *            data.
     */
    public void setBlob(boolean blob) {
        this.blob = blob;
    }

    /**
     * Sets the entity's member that stores the resource reference able to
     * update the blob value.
     * 
     * @param blobValueEditRefProperty
     *            The entity's member that stores the resource reference able to
     *            update the blob value.
     */
    public void setBlobValueEditRefProperty(Property blobValueEditRefProperty) {
        this.blobValueEditRefProperty = blobValueEditRefProperty;
    }

    /**
     * Sets the name of the entity property that stores the blob reference.
     * 
     * @param blobValueRefProperty
     *            The name of the entity property that stores the blob
     *            reference.
     */
    public void setBlobValueRefProperty(Property blobValueRefProperty) {
        this.blobValueRefProperty = blobValueRefProperty;
    }

    /**
     * Sets the "keys" property.
     * 
     * @param keys
     *            The "keys" property.
     */
    public void setKeys(List<Property> keys) {
        this.keys = keys;
    }

}
