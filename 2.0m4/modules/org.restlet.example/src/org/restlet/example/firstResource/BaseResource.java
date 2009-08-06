package org.restlet.example.firstResource;

import java.util.concurrent.ConcurrentMap;

import org.restlet.resource.ServerResource;

/**
 * Base resource class that supports common behaviours or attributes shared by
 * all resources.
 * 
 */
public abstract class BaseResource extends ServerResource {

    /**
     * Returns the map of items managed by this application.
     * 
     * @return the map of items managed by this application.
     */
    protected ConcurrentMap<String, Item> getItems() {
        return ((FirstResourceApplication) getApplication()).getItems();
    }
}
