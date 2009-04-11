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

package org.restlet.gae.data;

import java.io.IOException;

import org.restlet.gae.engine.util.SystemUtils;

/**
 * Multi-usage parameter. Note that the name and value properties are thread
 * safe, stored in volatile members.
 * 
 * @author Jerome Louvel
 */
public class Parameter implements Comparable<Parameter> {
    /** The name. */
    private volatile String name;

    /** The value. */
    private volatile String value;

    /**
     * Default constructor.
     */
    public Parameter() {
        this(null, null);
    }

    /**
     * Preferred constructor.
     * 
     * @param name
     *            The name.
     * @param value
     *            The value.
     */
    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Compares this object with the specified object for order.
     * 
     * @param o
     *            The object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     */
    public int compareTo(Parameter o) {
        return getName().compareTo(o.getName());
    }

    /**
     * Encodes the parameter and appends the result to the given buffer. Uses
     * the standard URI encoding mechanism.
     * 
     * @param buffer
     *            The buffer to append.
     * @param characterSet
     *            The supported character encoding
     * @throws IOException
     */
    public void encode(Appendable buffer, CharacterSet characterSet)
            throws IOException {
        if (getName() != null) {
            buffer.append(Reference.encode(getName(), characterSet));

            if (getValue() != null) {
                buffer.append('=');
                buffer.append(Reference.encode(getValue(), characterSet));
            }
        }
    }

    /**
     * Encodes the parameter using the standard URI encoding mechanism.
     * 
     * @param characterSet
     *            The supported character encoding.
     * @return The encoded string.
     * @throws IOException
     */
    public String encode(CharacterSet characterSet) throws IOException {
        final StringBuilder sb = new StringBuilder();
        encode(sb, characterSet);
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // if obj isn't a parameter or is null don't evaluate further
            if (obj instanceof Parameter) {
                final Parameter that = (Parameter) obj;

                if (this.name != null) {
                    // compare names taking care of nulls
                    result = (this.name.equals(that.name));
                } else {
                    result = (that.name == null);
                }

                if (result) {
                    // if names are equal test the values
                    if (this.value != null) {
                        // compare values taking care of nulls
                        result = (this.value.equals(that.value));
                    } else {
                        result = (that.value == null);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the name of this parameter.
     * 
     * @return The name of this parameter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public String getValue() {
        return this.value;
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

    /**
     * Returns a string with the name and value of the parameter.
     * 
     * @return A string with the name and value of the parameter.
     */
    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }
}
