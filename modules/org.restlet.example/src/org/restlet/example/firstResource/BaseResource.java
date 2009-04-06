package org.restlet.example.firstResource;

import java.util.Map;

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
    protected Map<String, Item> getItems() {
        return ((FirstResourceApplication) getApplication()).getItems();
    }

    /**
     * Put the given item into the storage device, if it is not already
     * registered. Returns the added item in this case, null otherwise.
     * 
     * @param itemName
     *            Name of the item.
     * @param itemDescription
     *            Description of the item.
     * @return Returns the added item in this case, null if the item cannot be
     *         added.
     */
    protected synchronized Item putIfAbsent(String itemName,
            String itemDescription) {
        Item result = null;
        if (!getItems().containsKey(itemName)) {
            result = new Item(itemName, itemDescription);
            getItems().put(itemName, result);
        }
        return result;
    }
}
