package org.restlet.example.book.restlet.ch9.resources;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 * Base resource class that supports common behaviours or attributes.
 * 
 */
public class BaseResource extends Resource {

    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

}
