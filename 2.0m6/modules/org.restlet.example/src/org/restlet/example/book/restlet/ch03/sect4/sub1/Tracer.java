package org.restlet.example.book.restlet.ch03.sect4.sub1;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;

/**
 * Restlet that returns common request properties to the client.
 */
public class Tracer extends Restlet {

    public Tracer(Context context) {
        super(context);
    }

    @Override
    public void handle(Request request, Response response) {
        String entity = "Method       : " + request.getMethod()
                + "\nResource URI : " 
                + request.getResourceRef()
                + "\nIP address   : " 
                + request.getClientInfo().getAddress()
                + "\nAgent name   : " 
                + request.getClientInfo().getAgentName()
                + "\nAgent version: "
                + request.getClientInfo().getAgentVersion();
        response.setEntity(entity, MediaType.TEXT_PLAIN);
    }

}
