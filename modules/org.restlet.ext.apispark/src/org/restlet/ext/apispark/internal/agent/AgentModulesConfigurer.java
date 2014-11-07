package org.restlet.ext.apispark.internal.agent;

import org.restlet.Restlet;
import org.restlet.routing.Filter;

import java.util.Collections;
import java.util.List;

/**
 * Assemble all agent modules together and expose {@link #getNext()} and
 * {@link #setNext(Restlet)} methods for collaborating with {@link Filter}.
 *
 * @author Manuel Boillod
 */
public class AgentModulesConfigurer {

    /**
     * If filter list is empty, we can not assign the next element to the last filter,
     * so we have to store the next element in this attribute.
     */
    private Restlet nextFilter;

    /**
     * List of agent module.
     * Each module should extends {@link Filter}.
     */
    private List<Filter> filters;

    public AgentModulesConfigurer(List<Filter> filters) {
        this.filters = Collections.unmodifiableList(filters);

        //chain filters together
        Filter lastFilter = null;
        for (Filter filter : filters) {
            if (lastFilter != null) {
                lastFilter.setNext(filter);
            }
            lastFilter = filter;
        }

    }

    /**
     * Returns the next filter to call.
     * If the filters list is not empty, this is the first filter,
     * otherwise get it from the {@link #nextFilter} attribute.
     */
    public Restlet getNext() {
        return filters.isEmpty() ? nextFilter : filters.get(0);
    }

    /**
     * Set the next element to call after all filters.
     * If the filters list is not empty, set the next element to the last filter,
     * otherwise store it in the {@link #nextFilter} attribute.
     */
    public void setNext(Restlet next) {
        if (filters.isEmpty()) {
            this.nextFilter = next;
        } else {
            filters.get(filters.size() - 1).setNext(next);
        }
    }


}
