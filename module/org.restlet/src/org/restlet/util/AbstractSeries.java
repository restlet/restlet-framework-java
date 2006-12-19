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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.Parameter;

/**
 * Modifiable list of entries with many helper methods. Note that this class
 * implements the org.restlet.util.Series interface using the E interface as the
 * template type. This allows you to use an instance of this class as any other
 * java.util.List, in particular all the helper methods in
 * java.util.Collections.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @see java.util.Collections
 * @see java.util.List
 */
public abstract class AbstractSeries<E extends Parameter> extends
        WrapperList<E> implements Series<E> {
    /**
     * Constructor.
     */
    public AbstractSeries() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity
     *            The initial list capacity.
     */
    public AbstractSeries(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor.
     * 
     * @param delegate
     *            The delegate list.
     */
    public AbstractSeries(List<E> delegate) {
        super(delegate);
    }

    /**
     * Creates a new entry.
     * 
     * @param name
     *            The name of the entry.
     * @param value
     *            The value of the entry.
     * @return A new entry.
     */
    public abstract E createEntry(String name, String value);

    /**
     * Creates a new series.
     * 
     * @param delegate
     *            Optional delegate series.
     * @return A new series.
     */
    public abstract Series<E> createSeries(List<E> delegate);

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#add(java.lang.String, java.lang.String)
     */
    public boolean add(String name, String value) {
        return add(createEntry(name, value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#copyTo(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void copyTo(Map<String, Object> params) {
        Parameter param;
        Object currentValue = null;
        for (Iterator<E> iter = iterator(); iter.hasNext();) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getFirst(java.lang.String)
     */
    public E getFirst(String name) {
        return getFirst(name, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getFirst(java.lang.String, boolean)
     */
    public E getFirst(String name, boolean ignoreCase) {
        for (E param : this) {
            if (equals(param.getName(), name, ignoreCase)) {
                return param;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getFirstValue(java.lang.String)
     */
    public String getFirstValue(String name) {
        return getFirstValue(name, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getFirstValue(java.lang.String, boolean)
     */
    public String getFirstValue(String name, boolean ignoreCase) {
        return getFirstValue(name, ignoreCase, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getFirstValue(java.lang.String, boolean,
     *      java.lang.String)
     */
    public String getFirstValue(String name, boolean ignoreCase,
            String defaultValue) {
        String result = defaultValue;
        Parameter param = getFirst(name, ignoreCase);

        if (param != null) {
            result = param.getValue();
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getFirstValue(java.lang.String,
     *      java.lang.String)
     */
    public String getFirstValue(String name, String defaultValue) {
        return getFirstValue(name, false, defaultValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getNames()
     */
    public Set<String> getNames() {
        Set<String> result = new HashSet<String>();

        for (Parameter param : this) {
            result.add(param.getName());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getValues(java.lang.String)
     */
    public String getValues(String name) {
        return getValues(name, ",", true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#getValues(java.lang.String, java.lang.String,
     *      boolean)
     */
    public String getValues(String name, String separator, boolean ignoreCase) {
        String result = null;
        StringBuilder sb = null;

        for (E param : this) {
            if (param.getName().equalsIgnoreCase(name)) {
                if (sb == null) {
                    if (result == null) {
                        result = param.getValue();
                    } else {
                        sb = new StringBuilder();
                        sb.append(result).append(separator).append(
                                param.getValue());
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

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#removeAll(java.lang.String)
     */
    public boolean removeAll(String name) {
        return removeAll(name, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#removeAll(java.lang.String, boolean)
     */
    public boolean removeAll(String name, boolean ignoreCase) {
        boolean changed = false;
        Parameter param = null;

        for (Iterator<E> iter = iterator(); iter.hasNext();) {
            param = iter.next();
            if (equals(param.getName(), name, ignoreCase)) {
                iter.remove();
                changed = true;
            }
        }

        return changed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#set(java.lang.String, java.lang.String,
     *      boolean)
     */
    public E set(String name, String value, boolean ignoreCase) {
        E result = null;
        E param = null;
        boolean found = false;

        for (Iterator<E> iter = iterator(); iter.hasNext();) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#subList(int, int)
     */
    @Override
    public Series<E> subList(int fromIndex, int toIndex) {
        return createSeries(getDelegate().subList(fromIndex, toIndex));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#subList(java.lang.String)
     */
    public Series<E> subList(String name) {
        return subList(name, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.data.PList#subList(java.lang.String, boolean)
     */
    public Series<E> subList(String name, boolean ignoreCase) {
        Series<E> result = createSeries(null);

        for (E param : this) {
            if (equals(param.getName(), name, ignoreCase)) {
                result.add(param);
            }
        }

        return result;
    }

}
