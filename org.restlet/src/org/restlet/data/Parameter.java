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

import java.io.IOException;
import java.util.Objects;

import org.restlet.engine.util.SystemUtils;
import org.restlet.util.NamedValue;

/**
 * Multi-usage parameter. Note that the name and value properties are thread
 * safe, stored in volatile members.
 * 
 * @author Jerome Louvel
 */
public class Parameter implements Comparable<Parameter>, NamedValue<String> {

    /** The first object. */
    private volatile String name;

    /** The second object. */
    private volatile String value;

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
        } else {
            return new Parameter(name.toString(), null);
        }
    }

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

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.NamedValue#compareTo(org.restlet.data.Parameter)
     */
    public int compareTo(Parameter o) {
        return getName().compareTo(o.getName());
    }

    /**
     * Encodes the parameter into the target buffer.
     * 
     * @param buffer
     *            The target buffer.
     * @param characterSet
     *            The character set to use.
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
     * Encodes the parameter as a string.
     * 
     * @param characterSet
     *            The character set to use.
     * @return The encoded string?
     * @throws IOException
     */
    public String encode(CharacterSet characterSet) throws IOException {
        StringBuilder sb = new StringBuilder();
        encode(sb, characterSet);
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Parameter)) {
            return false;
        }

        Parameter that = (Parameter) obj;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getValue(), that.getValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.NamedValue#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.NamedValue#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName(), getValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.NamedValue#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.NamedValue#setValue(java.lang.String)
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + getName() + "=" + getValue() + "]";
    }

}
