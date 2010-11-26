package org.restlet.example.book.restlet.ch08.sec5.sec5;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Resource that illustrates how to access to several properties of the current
 * request.
 */
public class TraceServerResource extends ServerResource {

    @Get
    public String toString() {
        // Build the result Representation
        StringBuilder result = new StringBuilder();

        result.append(" - Resource URI: ");
        result.append(getReference());
        result.append("\n - Query part of the URI: ");
        result.append(getQuery());
        result.append("\n - Client IP: ");
        result.append(getClientInfo().getAddress());
        result.append("\n - Client agent: ");
        result.append(getClientInfo().getAgentName());

        return result.toString();
    }
}
