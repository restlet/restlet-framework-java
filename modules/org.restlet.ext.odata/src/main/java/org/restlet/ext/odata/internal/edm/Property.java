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

import org.restlet.data.MediaType;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;

/**
 * Represents a property of an EntityType.
 * 
 * @author Thierry Boileau
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399546.aspx">Property
 *      Element (EntityType CSDL)</a>
 */
public class Property extends NamedObject {

    /** True if this property should be used for optimistic concurrency checks. */
    private boolean concurrent;

    /** The default value. */
    private String defaultValue;

    /** The access level modifier of the getter method. */
    private String getterAccess;

    /** The media type stored in the content. */
    private MediaType mediaType;

    /** True if this property is not mandatory. */
    private boolean nullable;

    /** The access level modifier of the setter method. */
    private String setterAccess;

    /** The type of the property. */
    private Type type;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of this property.
     */
    public Property(String name) {
        super(name);
    }

    /**
     * Returns the default value.
     * 
     * @return The default value.
     */

    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns the access level modifier of the getter method.
     * 
     * @return The access level modifier of the getter method.
     */

    public String getGetterAccess() {
        return getterAccess;
    }

    /**
     * Returns the media type stored in the content.
     * 
     * @return The media type stored in the content.
     */
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * Returns the property name used as a class member.
     * 
     * @return The property name used as a class member.
     */
    public String getPropertyName() {
        String result = super.getNormalizedName();
        if (ReflectUtils.isReservedWord(result)) {
            result = "_" + result;
        }

        return result;
    }

    /**
     * Returns the access level modifier of the setter method.
     * 
     * @return The access level modifier of the setter method.
     */

    public String getSetterAccess() {
        return setterAccess;
    }

    /**
     * Returns the type of the property.
     * 
     * @return The type of the property.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns true if this property should be used for optimistic concurrency
     * checks.
     * 
     * @return True if this property should be used for optimistic concurrency
     *         checks.
     */
    public boolean isConcurrent() {
        return concurrent;
    }

    /**
     * Returns true if this property is not mandatory.
     * 
     * @return True if this property is not mandatory.
     */

    public boolean isNullable() {
        return nullable;
    }

    /**
     * Indicates if this property should be used for optimistic concurrency
     * checks.
     * 
     * @param concurrent
     *            True if this property should be used for optimistic
     *            concurrency checks.
     */
    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    /**
     * Sets the default value.
     * 
     * @param defaultValue
     *            The default value.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the access level modifier of the getter method.
     * 
     * @param getterAccess
     *            The access level modifier of the getter method.
     */
    public void setGetterAccess(String getterAccess) {
        this.getterAccess = getterAccess;
    }

    /**
     * Sets the media type stored in the content.
     * 
     * @param mediaType
     *            The media type stored in the content.
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Sets true if this property is not mandatory.
     * 
     * @param nullable
     *            True if this property is not mandatory.
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Sets the access level modifier of the setter method.
     * 
     * @param setterAccess
     *            The access level modifier of the setter method.
     */
    public void setSetterAccess(String setterAccess) {
        this.setterAccess = setterAccess;
    }

    /**
     * Sets the type of the property.
     * 
     * @param type
     *            The type of the property.
     */
    public void setType(Type type) {
        this.type = type;
    }

}
