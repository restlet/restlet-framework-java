package org.restlet.example.firstResourceApplication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class FirstResourceApplication extends Application {

    /** The list of items is persisted in memory. */
    private final Map<String, Item> items;

    public FirstResourceApplication(Context context) {
        super(context);
        // We make sure that this attribute will support concurrent access.
        items = new ConcurrentHashMap<String, Item>();
    }

    public FirstResourceApplication() {
        super();
        // We make sure that this attribute will support concurrent access.
        items = new ConcurrentHashMap<String, Item>();
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createRoot() {
        // Create a router Restlet that defines routes.
        Router router = new Router(getContext());

        // Defines a route for the resource "list of items"
        router.attach("/items", ItemsResource.class);
        // Defines a route for the resource "item"
        router.attach("/items/{itemName}", ItemResource.class);

        return router;
    }

    /**
     * Returns the list of registered items.
     * 
     * @return the list of registered items.
     */
    public Map<String, Item> getItems() {
        return items;
    }
}
