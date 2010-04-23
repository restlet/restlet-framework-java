package org.restlet.example.ext.wadl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.WadlApplication;

public class FirstResourceApplication extends WadlApplication {

    public static void main(String[] args) throws Exception {
        // Create a new Component.
        Component component = new Component();

        // Add a new HTTP server listening on port 8182.
        component.getServers().add(Protocol.HTTP, 8182);

        // Attach the sample application.
        component.getDefaultHost().attach("/firstResource",
                new FirstResourceApplication());

        // Start the component.
        component.start();
    }

    /** The list of items is persisted in memory. */
    private final ConcurrentMap<String, Item> items = new ConcurrentHashMap<String, Item>();

    @Override
    public Restlet createRoot() {
        // Create a router Restlet that defines routes.
        Router router = new Router(getContext());

        // Defines a route for the resource "list of items"
        router.attach("/items", ItemsResource.class);
        // Defines a route for the resource "item"
        router.attach("/items/{itemName}", ItemResource.class);

        return router;
    }

    @Override
    public ApplicationInfo getApplicationInfo(Request request, Response response) {
        ApplicationInfo result = super.getApplicationInfo(request, response);

        DocumentationInfo docInfo = new DocumentationInfo(
                "This sample application shows how to generate online documentation.");
        docInfo.setTitle("First resource sample application.");
        result.setDocumentation(docInfo);

        return result;
    }

    /**
     * Returns the list of registered items.
     * 
     * @return the list of registered items.
     */
    public ConcurrentMap<String, Item> getItems() {
        return items;
    }
}
