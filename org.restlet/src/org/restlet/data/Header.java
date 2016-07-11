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

package org.restlet.data;

import java.util.Objects;

import org.restlet.engine.util.SystemUtils;
import org.restlet.util.NamedValue;

/**
 * Represents an HTTP header.
 * 
 * @author Jerome Louvel
 */
public class Header implements NamedValue<String> {

    /** The name. */
    private volatile String name;

    /** The value. */
    private volatile String value;

    /**
     * Default constructor.
     */
    public Header() {
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The header name.
     * @param value
     *            The header value.
     */
    public Header(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Header)) {
            return false;
        }

        Header that = (Header) obj;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getValue(), that.getValue());
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public String getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName(), getValue());
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + getName() + ": " + getValue() + "]";
    }

}
