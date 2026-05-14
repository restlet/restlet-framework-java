/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

/**
 * String couple between a name and a value.
 *
 * @author Jerome Louvel
 */
public interface NamedValue<V> {

    /**
     * Returns the name of this parameter.
     *
     * @return The name of this parameter.
     */
    String getName();

    /**
     * Returns the value.
     *
     * @return The value.
     */
    V getValue();

    /**
     * Sets the value.
     *
     * @param value The value.
     */
    void setValue(V value);
}
