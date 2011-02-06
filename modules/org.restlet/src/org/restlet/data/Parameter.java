/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.data;

import java.io.IOException;

import org.restlet.util.Couple;

/**
 * Multi-usage parameter. Note that the name and value properties are thread
 * safe, stored in volatile members.
 * 
 * @author Jerome Louvel
 */
public class Parameter extends Couple<String, String> implements
        Comparable<Parameter> {

    /**
     * Default constructor.
     */
    public Parameter() {
        this(null, null);
    }

    /**
     * Creates a parameter.
     * 
     * @param name
     *            The parameter name buffer.
     * @param value
     *            The parameter value buffer (can be null).
     * @return The created parameter.
     * @throws IOException
     */
    public static Parameter create(CharSequence name, CharSequence value) {
        if (value != null) {
            return new Parameter(name.toString(), value.toString());
        }
        return new Parameter(name.toString(), null);
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
        super(name, value);
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

    /**
     * Returns the name of this parameter.
     * 
     * @return The name of this parameter.
     */
    public String getName() {
        return getFirst();
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public String getValue() {
        return getSecond();
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            The name.
     */
    public void setName(String name) {
        setFirst(name);
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The value.
     */
    public void setValue(String value) {
        setSecond(value);
    }

}
