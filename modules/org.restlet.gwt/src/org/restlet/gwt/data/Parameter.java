/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.data;

import java.io.IOException;

import org.restlet.gwt.util.Engine;

/**
 * Multi-usage parameter. Note that the name and value properties are thread
 * safe, stored in volatile members.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
     * Encodes the parameter using the standard URI encoding mechanism.
     * 
     * @param characterSet
     *            The supported character encoding.
     * @return The encoded string.
     * @throws IOException
     */
    public String encode(CharacterSet characterSet) throws Exception {
        final StringBuilder sb = new StringBuilder();
        encode(sb, characterSet);
        return sb.toString();
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
    public void encode(StringBuilder buffer, CharacterSet characterSet)
            throws Exception {
        if (getName() != null) {
            buffer.append(Reference.encode(getName(), characterSet));

            if (getValue() != null) {
                buffer.append('=');
                buffer.append(Reference.encode(getValue(), characterSet));
            }
        }
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
        return Engine.hashCode(getName(), getValue());
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
