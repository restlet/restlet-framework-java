package org.restlet.example.firstResource;

import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 * Base resource class that supports common behaviours or attributes shared by
 * all resources.
 * 
 */
public abstract class BaseResource extends Resource {

    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    /**
     * Returns the map of items managed by this application.
     * 
     * @return the map of items managed by this application.
     */
    protected ConcurrentMap<String, Item> getItems() {
        return ((FirstResourceApplication) getApplication()).getItems();
    }
}
