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

package org.restlet.ext.rdf;

import java.util.Objects;

import org.restlet.engine.util.SystemUtils;

/**
 * Relationship between two typed objects.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 *            The first object's type.
 * @param <U>
 *            The second object's type.
 */
public class Couple<T, U> {

    /** The first object. */
    private volatile T first;

    /** The second object. */
    private volatile U second;

    /**
     * Constructor.
     * 
     * @param first
     *            The first object.
     * @param second
     *            The second object.
     */
    public Couple(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Couple)) {
            return false;
        }

        Couple<?, ?> that = (Couple<?, ?>) other;

        return Objects.equals(getFirst(), that.getFirst())
                && Objects.equals(getSecond(), that.getSecond());
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getFirst(), getSecond());
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

    @Override
    public String toString() {
        return "(" + getFirst() + "," + getSecond() + ")";
    }
}
