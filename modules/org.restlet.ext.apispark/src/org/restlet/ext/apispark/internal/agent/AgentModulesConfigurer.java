package org.restlet.ext.apispark.internal.agent;

import java.util.Collections;
import java.util.List;

import org.restlet.Restlet;
import org.restlet.routing.Filter;

/**
 * Assembles all agent modules together and exposes {@link #getNext()} and
 * {@link #setNext(Restlet)} methods for collaborating with {@link Filter}.
 * 
 * @author Manuel Boillod
 */
public class AgentModulesConfigurer {

    /** List of agent module. Each module should extends {@link Filter}. */
    private List<Filter> filters;

    /**
     * If filter list is empty, we can not assign the next element to the last
     * filter, so we have to store the next element in this attribute.
     */
    private Restlet next;

    public AgentModulesConfigurer(List<Filter> filters) {
        this.filters = Collections.unmodifiableList(filters);

        // chain filters together
        Filter lastFilter = null;
        for (Filter filter : filters) {
            if (lastFilter != null) {
                lastFilter.setNext(filter);
            }
            lastFilter = filter;
        }

    }

    /**
     * Returns the next filter to call. If the filters list is not empty, this
     * is the first filter, otherwise get it from the {@link #next} attribute.
     */
    public Restlet getNext() {
        return filters.isEmpty() ? next : filters.get(0);
    }

    /**
     * Set the next element to call after all filters. If the filters list is
     * not empty, set the next element to the last filter, otherwise stores it
     * in the {@link #next} attribute.
     */
    public void setNext(Restlet next) {
        if (filters.isEmpty()) {
            this.next = next;
        } else {
            filters.get(filters.size() - 1).setNext(next);
        }
    }

}
