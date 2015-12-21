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

package org.restlet.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Context;

/**
 * Modifiable list of entries with many helper methods. Note that this class
 * uses the Parameter class as the template type. This allows you to use an
 * instance of this class as any other java.util.List, in particular all the
 * helper methods in java.util.Collections.
 * 
 * @author Jerome Louvel
 * @param <T>
 *            The contained type
 * @see org.restlet.data.Parameter
 * @see java.util.Collections
 * @see java.util.List
 */
// [ifndef gwt] line
public class Series<T extends NamedValue<String>> extends WrapperList<T> {
    // [ifdef gwt] uncomment
    // public abstract class Series<T extends NamedValue<String>> extends
    // WrapperList<T>
    // {
    // [enddef]
    /**
     * A marker for empty values to differentiate from non existing values
     * (null).
     */
    public static final Object EMPTY_VALUE = new Object();

    // [ifndef gwt] method
    /**
     * Returns an unmodifiable view of the specified series. Attempts to call a
     * modification method will throw an UnsupportedOperationException.
     * 
     * @param series
     *            The series for which an unmodifiable view should be returned.
     * @return The unmodifiable view of the specified series.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Series<? extends NamedValue> unmodifiableSeries(
            final Series<? extends NamedValue> series) {
        return new Series(series.entryClass,
                java.util.Collections.unmodifiableList(series.getDelegate()));
    }

    /** The entry class. */
    private final Class<T> entryClass;

    /**
     * Constructor.
     */
    public Series(Class<T> entryClass) {
        super();
        this.entryClass = entryClass;
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity
     *            The initial list capacity.
     */
    public Series(Class<T> entryClass, int initialCapacity) {
        super(initialCapacity);
        this.entryClass = entryClass;
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *            The delegate list.
     */
    public Series(Class<T> entryClass, List<T> delegate) {
        super(delegate);
        this.entryClass = entryClass;
    }

    /**
     * Creates then adds a parameter at the end of the list.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     * @return True (as per the general contract of the Collection.add method).
     */
    public boolean add(String name, String value) {
        return add(createEntry(name, value));
    }

    /**
     * Copies the parameters whose name is a key in the given map.<br>
     * If a matching parameter is found, its value is put in the map.<br>
     * If multiple values are found, a list is created and set in the map.
     * 
     * @param params
     *            The map controlling the copy.
     */
    @SuppressWarnings("unchecked")
    public void copyTo(Map<String, Object> params) {
        NamedValue<String> param;
        Object currentValue = null;

        for (Iterator<T> iter = iterator(); iter.hasNext();) {
            param = iter.next();

            if (params.containsKey(param.getName())) {
                currentValue = params.get(param.getName());

                if (currentValue != null) {
                    List<Object> values = null;

                    if (currentValue instanceof List) {
                        // Multiple values already found for this entry
                        values = (List<Object>) currentValue;
                    } else {
                        // Second value found for this entry
                        // Create a list of values
                        values = new ArrayList<Object>();
                        values.add(currentValue);
                        params.put(param.getName(), values);
                    }

                    if (param.getValue() == null) {
                        values.add(Series.EMPTY_VALUE);
                    } else {
                        values.add(param.getValue());
                    }
                } else {
                    if (param.getValue() == null) {
                        params.put(param.getName(), Series.EMPTY_VALUE);
                    } else {
                        params.put(param.getName(), param.getValue());
                    }
                }
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Creates a new entry.
     * 
     * @param name
     *            The name of the entry.
     * @param value
     *            The value of the entry.
     * @return A new entry.
     */
    public T createEntry(String name, String value) {
        try {
            return this.entryClass.getConstructor(String.class, String.class)
                    .newInstance(name, value);
        } catch (Exception e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to create a series entry", e);
            return null;
        }
    }

    // [ifdef gwt] uncomment
    // /**
    // * Creates a new entry.
    // *
    // * @param name
    // * The name of the entry.
    // * @param value
    // * The value of the entry.
    // * @return A new entry.
    // */
    // public abstract T createEntry(String name, String value);
    // [enddef]

    // [ifdef gwt] uncomment
    // /**
    // * Creates a new series.
    // *
    // * @param delegate
    // * Optional delegate series.
    // * @return A new series.
    // */
    // public abstract Series<T> createSeries(List<T> delegate);
    // [enddef]

    /**
     * Tests the equality of two string, potentially null, which a case
     * sensitivity flag.
     * 
     * @param value1
     *            The first value.
     * @param value2
     *            The second value.
     * @param ignoreCase
     *            Indicates if the test should be case insensitive.
     * @return True if both values are equal.
     */
    private boolean equals(String value1, String value2, boolean ignoreCase) {
        boolean result = (value1 == value2);

        if (!result) {
            if ((value1 != null) && (value2 != null)) {
                if (ignoreCase) {
                    result = value1.equalsIgnoreCase(value2);
                } else {
                    result = value1.equals(value2);
                }
            }
        }

        return result;
    }

    /**
     * Returns the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return The first parameter found with the given name.
     */
    public T getFirst(String name) {
        return getFirst(name, false);
    }

    /**
     * Returns the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case insensitive.
     * @return The first parameter found with the given name.
     */
    public T getFirst(String name, boolean ignoreCase) {
        for (T param : this) {
            if (equals(param.getName(), name, ignoreCase)) {
                return param;
            }
        }

        return null;
    }

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return The value of the first parameter found with the given name.
     */
    public String getFirstValue(String name) {
        return getFirstValue(name, false);
    }

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return The value of the first parameter found with the given name.
     */
    public String getFirstValue(String name, boolean ignoreCase) {
        return getFirstValue(name, ignoreCase, null);
    }

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @param defaultValue
     *            The default value to return if no matching parameter found or
     *            if the parameter has a null value.
     * @return The value of the first parameter found with the given name or the
     *         default value.
     */
    public String getFirstValue(String name, boolean ignoreCase,
            String defaultValue) {
        String result = defaultValue;
        NamedValue<String> param = getFirst(name, ignoreCase);

        if ((param != null) && (param.getValue() != null)) {
            result = param.getValue();
        }

        return result;
    }

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @param defaultValue
     *            The default value to return if no matching parameter found or
     *            if the parameter has a null value.
     * @return The value of the first parameter found with the given name or the
     *         default value.
     */
    public String getFirstValue(String name, String defaultValue) {
        return getFirstValue(name, false, defaultValue);
    }

    /**
     * Returns the set of parameter names (case sensitive).
     * 
     * @return The set of parameter names.
     */
    public Set<String> getNames() {
        Set<String> result = new HashSet<String>();

        for (NamedValue<String> param : this) {
            result.add(param.getName());
        }

        return result;
    }

    /**
     * Returns the values of the parameters with a given name. If multiple
     * parameters with the same name are found, all values are concatenated and
     * separated by a comma (like for HTTP message headers).
     * 
     * @param name
     *            The parameter name (case insensitive).
     * @return The values of the parameters with a given name.
     */
    public String getValues(String name) {
        return getValues(name, ",", true);
    }

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
    public String getValues(String name, String separator, boolean ignoreCase) {
        String result = null;
        StringBuilder sb = null;

        for (final T param : this) {
            if ((ignoreCase && param.getName().equalsIgnoreCase(name))
                    || param.getName().equals(name)) {
                if (sb == null) {
                    if (result == null) {
                        result = param.getValue();
                    } else {
                        sb = new StringBuilder();
                        sb.append(result).append(separator)
                                .append(param.getValue());
                    }
                } else {
                    sb.append(separator).append(param.getValue());
                }
            }
        }

        if (sb != null) {
            result = sb.toString();
        }

        return result;
    }

    /**
     * Returns an array of all the values associated to the given parameter
     * name.
     * 
     * @param name
     *            The parameter name to match (case sensitive).
     * @return The array of values.
     */
    public String[] getValuesArray(String name) {
        return getValuesArray(name, false);
    }

    /**
     * Returns an array of all the values associated to the given parameter
     * name.
     * 
     * @param name
     *            The parameter name to match.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @return The array of values.
     */
    public String[] getValuesArray(String name, boolean ignoreCase) {
        return getValuesArray(name, ignoreCase, null);
    }

    /**
     * Returns an array of all the values associated to the given parameter
     * name.
     * 
     * @param name
     *            The parameter name to match.
     * @param ignoreCase
     *            Indicates if the name comparison is case sensitive.
     * @param defaultValue
     *            The default value to return if no matching parameter found or
     *            if the parameter has a null value.
     * @return The array of values.
     */
    public String[] getValuesArray(String name, boolean ignoreCase,
            String defaultValue) {
        String[] result = null;
        List<T> params = subList(name, ignoreCase);

        if ((params.size() == 0) && (defaultValue != null)) {
            result = new String[1];
            result[0] = defaultValue;
        } else {
            result = new String[params.size()];

            for (int i = 0; i < params.size(); i++) {
                result[i] = params.get(i).getValue();
            }
        }

        return result;
    }

    /**
     * Returns an array of all the values associated to the given parameter
     * name.
     * 
     * @param name
     *            The parameter name to match.
     * @param defaultValue
     *            The default value to return if no matching parameter found or
     *            if the parameter has a null value.
     * @return The array of values.
     */
    public String[] getValuesArray(String name, String defaultValue) {
        return getValuesArray(name, false, defaultValue);
    }

    /**
     * Returns a map of name, value pairs. The order of the map keys is
     * respected based on the series order. When a name has multiple values,
     * only the first one is put in the map.
     * 
     * @return The map of name, value pairs.
     */
    public Map<String, String> getValuesMap() {
        Map<String, String> result = new LinkedHashMap<String, String>();

        for (NamedValue<String> param : this) {
            if (!result.containsKey(param.getName())) {
                result.put(param.getName(), param.getValue());
            }
        }

        return result;
    }

    /**
     * Removes all the parameters with a given name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return True if the list changed.
     */
    public boolean removeAll(String name) {
        return removeAll(name, false);
    }

    /**
     * Removes all the parameters with a given name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case insensitive.
     * @return True if the list changed.
     */
    public boolean removeAll(String name, boolean ignoreCase) {
        boolean changed = false;
        NamedValue<String> param = null;

        for (Iterator<T> iter = iterator(); iter.hasNext();) {
            param = iter.next();

            if (equals(param.getName(), name, ignoreCase)) {
                iter.remove();
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Removes from this list the first entry whose name equals the specified
     * name ignoring the case.
     * 
     * @param name
     *            The name of the entries to be removed (case sensitive).
     * @return false if no entry has been removed, true otherwise.
     */
    public boolean removeFirst(String name) {
        return removeFirst(name, false);
    }

    /**
     * Removes from this list the first entry whose name equals the specified
     * name ignoring the case or not.
     * 
     * @param name
     *            The name of the entries to be removed.
     * @param ignoreCase
     *            Indicates if the name comparison is case insensitive.
     * @return false if no entry has been removed, true otherwise.
     */
    public boolean removeFirst(String name, boolean ignoreCase) {
        boolean changed = false;
        NamedValue<String> param = null;

        for (final Iterator<T> iter = iterator(); iter.hasNext() && !changed;) {
            param = iter.next();
            if (equals(param.getName(), name, ignoreCase)) {
                iter.remove();
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Replaces the value of the first parameter with the given name and removes
     * all other parameters with the same name. The name matching is case
     * sensitive.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The value to set.
     * @return The parameter set or added.
     */
    public T set(String name, String value) {
        return set(name, value, false);
    }

    /**
     * Replaces the value of the first parameter with the given name and removes
     * all other parameters with the same name.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The value to set.
     * @param ignoreCase
     *            Indicates if the name comparison is case insensitive.
     * @return The parameter set or added.
     */
    public T set(String name, String value, boolean ignoreCase) {
        T result = null;
        T param = null;
        boolean found = false;

        for (final Iterator<T> iter = iterator(); iter.hasNext();) {
            param = iter.next();

            if (equals(param.getName(), name, ignoreCase)) {
                if (found) {
                    // Remove other entries with the same name
                    iter.remove();
                } else {
                    // Change the value of the first matching entry
                    found = true;
                    param.setValue(value);
                    result = param;
                }
            }
        }

        if (!found) {
            add(name, value);
        }

        return result;
    }

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
    @Override
    public Series<T> subList(int fromIndex, int toIndex) {
        // [ifndef gwt] instruction
        return new Series<T>(this.entryClass, getDelegate().subList(fromIndex,
                toIndex));
        // [ifdef gwt] instruction uncomment
        // return
        // createSeries(org.restlet.engine.util.ListUtils.copySubList(
        // getDelegate(), fromIndex, toIndex));
    }

    /**
     * Returns a list of all the values associated to the parameter name.
     * 
     * @param name
     *            The parameter name (case sensitive).
     * @return The list of values.
     */
    public Series<T> subList(String name) {
        return subList(name, false);
    }

    /**
     * Returns a list of all the values associated to the parameter name.
     * 
     * @param name
     *            The parameter name.
     * @param ignoreCase
     *            Indicates if the name comparison is case insensitive.
     * @return The list of values.
     */
    public Series<T> subList(String name, boolean ignoreCase) {
        // [ifndef gwt] instruction
        Series<T> result = new Series<T>(this.entryClass);
        // [ifdef gwt] instruction uncomment
        // Series<T> result = createSeries(null);

        for (T param : this) {
            if (equals(param.getName(), name, ignoreCase)) {
                result.add(param);
            }
        }

        return result;
    }

}
