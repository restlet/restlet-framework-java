package org.restlet.example.book.restlet.misc;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.resource.ServerResource;

public class TemplateServer extends ServerResource {

    public static void main(String[] args) throws Exception {
        new Server(Protocol.HTTP, 8182, TemplateResource.class).start();
    }
}
