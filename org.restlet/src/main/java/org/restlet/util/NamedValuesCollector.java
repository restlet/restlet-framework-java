/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects named values inside a given map. Only the values related to the existing keys will be
 * added.
 *
 * <p>In case of multiple values for the same name, the map will contain a list of values.
 */
public class NamedValuesCollector {

    private final Map<String, Object> collector;

    /**
     * Constructor.
     *
     * @param mapToUpdate The map to update
     */
    public NamedValuesCollector(final Map<String, Object> mapToUpdate) {
        this.collector = mapToUpdate;
    }

    /**
     * Constructor.
     *
     * @param names The list of names to collect.
     */
    public NamedValuesCollector(final String... names) {
        this.collector = new HashMap<>();
        for (String name : names) {
            collector.put(name, null);
        }
    }

    @SuppressWarnings("unchecked")
    public void collect(final NamedValue<String> parameter) {
        if (collector.containsKey(parameter.getName())) {
            Object currentValue = collector.get(parameter.getName());

            if (currentValue != null) {
                List<Object> values;

                if (currentValue instanceof List) { // Multiple values already found for this entry
                    values = (List<Object>) currentValue;
                } else {
                    // Second value found for this entry
                    // Create a list of values
                    values = new ArrayList<>();
                    values.add(currentValue);
                    collector.put(parameter.getName(), values);
                }

                values.add(
                        parameter.getValue() == null ? Series.EMPTY_VALUE : parameter.getValue());
            } else {
                collector.put(
                        parameter.getName(),
                        parameter.getValue() == null ? Series.EMPTY_VALUE : parameter.getValue());
            }
        }
    }

    /**
     * Returns either the map transmitted to the constructor or a new map containing the collected
     * values.
     *
     * @return Either the map transmitted to the constructor or a new map containing the collected
     *     values
     */
    public Map<String, Object> getCollectedValues() {
        return collector;
    }
}
