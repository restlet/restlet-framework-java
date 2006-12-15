/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * List of entries where each entry is named. It is differen from a standard Map
 * because the same named entry can occur several times and the order matter.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public interface Series<E extends Series.Entry> extends List<E> {
    /**
     * A named series entry.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    public interface Entry {
        /**
         * Returns the name of this parameter.
         * 
         * @return The name of this parameter.
         */
        public String getName();

        /**
         * Returns the value.
         * 
         * @return The value.
         */
        public String getValue();

        /**
         * Sets the value.
         * 
         * @param value
         *            The value.
         */
        public void setValue(String value);
    }

    /**
     * A marker for empty values to differentiate from non existing values
     * (null).
     */
    public static final Object EMPTY_VALUE = new Object();

    /**
     * Creates then adds a parameter at the end of the list.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     * @return True (as per the general contract of the Collection.add method).
     */
    public boolean add(String name, String value);

    /**
     * Copies the parameters whose name is a key in the given map.<br/> If a
     * matching parameter is found, its value is put in the map.<br/> If
     * multiple values are found, a list is created and set in the map.
     * 
     * @param params
     *            The map controlling the copy.
     */
    public void copyTo(Map<String, Object> params);

    /**
     * Returns the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return The first parameter found with the given name.
     */
    public E getFirst(String name);

    /**
     * Returns the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return The first parameter found with the given name.
     */
    public E getFirst(String name, boolean ignoreCase);

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return The value of the first parameter found with the given name.
     */
    public String getFirstValue(String name);

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return The value of the first parameter found with the given name.
     */
    public String getFirstValue(String name, boolean ignoreCase);

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @param defaultValue
     *            The default value to return if no matching parameter found.
     * @return The value of the first parameter found with the given name or the
     *         default value.
     */
    public String getFirstValue(String name, boolean ignoreCase,
            String defaultValue);

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @param defaultValue
     *            The default value to return if no matching parameter found.
     * @return The value of the first parameter found with the given name or the
     *         default value.
     */
    public String getFirstValue(String name, String defaultValue);

    /**
     * Returns the set of parameter names (case sensitive).
     * 
     * @return The set of parameter names.
     */
    public Set<String> getNames();

    /**
     * Returns the values of the parameters with a given name. If multiple
     * parameters with the same name are found, all values are concatenated and
     * separated by a comma (like for HTTP message headers).
     * 
     * @param name
     *            The parameter name (case insensitive).
     * @return The values of the parameters with a given name.
     */
    public String getValues(String name);

    /**
     * Returns the parameter values with a given name. If multiple parameters
     * with the same name are found, all values are concatenated and separated
     * by the given separator.
     * 
     * @param name
     *            The parameter name.
     * @param separator
     *            The separator character.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return The sequence of values.
     */
    public String getValues(String name, String separator, boolean ignoreCase);

    /**
     * Removes all the parameters with a given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return True if the list changed.
     */
    public boolean removeAll(String name);

    /**
     * Removes all the parameters with a given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return True if the list changed.
     */
    public boolean removeAll(String name, boolean ignoreCase);

    /**
     * Replaces the value of the first parameter with the given name and removes
     * all other parameters with the same name.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The value to set.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return The parameter set or added.
     */
    public E set(String name, String value, boolean ignoreCase);

    /**
     * Returns a view of the portion of this list between the specified
     * fromIndex, inclusive, and toIndex, exclusive.
     * 
     * @param fromIndex
     *            The start position.
     * @param toIndex
     *            The end position (exclusive).
     * @return The sub-list.
     */
    public Series<E> subList(int fromIndex, int toIndex);

    /**
     * Returns a list of all the values associated to the parameter name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return The list of values.
     */
    public Series<E> subList(String name);

    /**
     * Returns a list of all the values associated to the parameter name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return The list of values.
     */
    public Series<E> subList(String name, boolean ignoreCase);

}