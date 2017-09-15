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

package org.restlet.ext.rdf.internal.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to handle properties that have a scope such as the base URI, the
 * xml:lang property.
 * 
 * @param <E>
 *            The type of the property.
 * @author Thierry Boileau
 */
class ScopedProperty<E> {
    private int[] depths;

    private int size;

    private List<E> values;

    /**
     * Constructor.
     */
    public ScopedProperty() {
        super();
        this.depths = new int[10];
        this.values = new ArrayList<E>();
        this.size = 0;
    }

    /**
     * Constructor.
     * 
     * @param value
     *            Value.
     */
    public ScopedProperty(E value) {
        this();
        add(value);
        incrDepth();
    }

    /**
     * Add a new value.
     * 
     * @param value
     *            The value to be added.
     */
    public void add(E value) {
        this.values.add(value);
        if (this.size == this.depths.length) {
            int[] temp = new int[2 * this.depths.length];
            System.arraycopy(this.depths, 0, temp, 0, this.depths.length);
            this.depths = temp;
        }
        this.size++;
        this.depths[size - 1] = 0;
    }

    /**
     * Decrements the depth of the current value, and remove it if necessary.
     */
    public void decrDepth() {
        if (this.size > 0) {
            this.depths[size - 1]--;
            if (this.depths[size - 1] < 0) {
                this.size--;
                this.values.remove(size);
            }
        }
    }

    /**
     * Returns the current value.
     * 
     * @return The current value.
     */
    public E getValue() {
        if (this.size > 0) {
            return this.values.get(this.size - 1);
        }
        return null;
    }

    /**
     * Increments the depth of the current value.
     */
    public void incrDepth() {
        if (this.size > 0) {
            this.depths[size - 1]++;
        }
    }
}
