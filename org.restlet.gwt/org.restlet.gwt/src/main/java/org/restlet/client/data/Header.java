/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.data;

import java.util.Objects;

import org.restlet.client.engine.util.SystemUtils;
import org.restlet.client.util.NamedValue;

/**
 * Represents an HTTP header.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
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
