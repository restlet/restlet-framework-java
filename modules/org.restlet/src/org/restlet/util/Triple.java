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

package org.restlet.util;

import org.restlet.engine.util.SystemUtils;

/**
 * Relationship between three typed objects.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 *            The first object's type.
 * @param <U>
 *            The second object's type.
 * @param <V>
 *            The third object's type.
 */
public class Triple<T, U, V> {

    /** The first object. */
    private volatile T first;

    /** The second object. */
    private volatile U second;

    /** The third object. */
    private volatile V third;

    /**
     * Constructor.
     * 
     * @param first
     *            The first object.
     * @param second
     *            The second object.
     * @param third
     *            The third object.
     */
    public Triple(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = (this == other);

        if (!result && (other instanceof Triple)) {
            Triple<?, ?, ?> triple = (Triple<?, ?, ?>) other;

            if (((triple.getFirst() == null) && (getFirst() == null))
                    || ((getFirst() != null) && getFirst().equals(
                            triple.getFirst()))) {
                if (((triple.getSecond() == null) && (getSecond() == null))
                        || ((getSecond() != null) && getSecond().equals(
                                triple.getSecond()))) {
                    result = (((triple.getThird() == null) && (getThird() == null)) || ((getThird() != null) && getThird()
                            .equals(triple.getThird())));
                }
            }
        }

        return result;
    }

    /**
     * Returns the first object.
     * 
     * @return The first object.
     */
    public T getFirst() {
        return first;
    }

    /**
     * Returns the second object.
     * 
     * @return The second object.
     */
    public U getSecond() {
        return second;
    }

    /**
     * Returns the third object.
     * 
     * @return The third object.
     */
    public V getThird() {
        return third;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getFirst(), getSecond(), getThird());
    }

    /**
     * Sets the first object.
     * 
     * @param first
     *            The first object.
     */
    public void setFirst(T first) {
        this.first = first;
    }

    /**
     * Sets the second object.
     * 
     * @param second
     *            The second object.
     */
    public void setSecond(U second) {
        this.second = second;
    }

    /**
     * Sets the third object.
     * 
     * @param third
     *            The third object.
     */
    public void setThird(V third) {
        this.third = third;
    }

    @Override
    public String toString() {
        return "(" + getFirst() + "," + getSecond() + "," + getThird() + ")";
    }
}
